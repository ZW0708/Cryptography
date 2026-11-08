import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * 密码学闯关 · Java 启动器
 *
 * 统一拉起 Spring Boot 后端与 Vite 前端，日志写入 logs/ 目录。
 * - 启动前自动清理上次残留的子进程（直接关窗口也不会导致端口被占）。
 * - 退出时回收子进程树（推荐在窗口内按回车退出；直接关窗口下次启动也会自愈）。
 *
 * 用法：
 *   右键 run.bat
 *   或：java -cp .tools/launcher Launcher
 */
public class Launcher {

    private static final Path ROOT = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
    private static final Path JDK = ROOT.resolve(".tools").resolve("jdk-17");
    private static final Path JAVA_EXE = JDK.resolve("bin").resolve("java.exe");
    private static final Path MVN_CMD = ROOT.resolve(".tools").resolve("maven").resolve("bin").resolve("mvn.cmd");
    private static final Path LOGS = ROOT.resolve("logs");

    private static Process backendProc;
    private static Process frontendProc;

    public static void main(String[] args) {
        System.out.println("========================================");
        System.out.println("       密码学闯关 · Java 启动器");
        System.out.println("========================================");

        boolean windows = isWindows();

        try {
            Files.createDirectories(LOGS);

            if (windows && !Files.exists(JAVA_EXE)) {
                System.err.println("[X] 未找到 .tools/jdk-17，请确认自包含环境存在");
                return;
            }

            // 关键：先清理上次残留的后端/前端进程，避免旧进程占住端口
            cleanupStale("backend");
            cleanupStale("frontend");

            if (portOpen(3306)) {
                System.out.println("[√] MySQL 3306 已就绪");
            } else {
                System.out.println("[!] 未检测到 MySQL 3306，后端可能启动失败，请先启动 MySQL");
            }

            backendProc = startBackend(windows);
            frontendProc = startFrontend(windows);

            startPidRecorder();

            Thread.sleep(2500);
            openBrowser("http://localhost:5173");

            System.out.println();
            System.out.println("前端: http://localhost:5173");
            System.out.println("后端: http://localhost:8080");
            System.out.println("文档: http://localhost:8080/swagger-ui.html");
            System.out.println("日志: " + LOGS);
            System.out.println();
            System.out.println("按回车退出并关闭前后端...");
            new BufferedReader(new InputStreamReader(System.in)).readLine();
        } catch (Exception e) {
            System.err.println("[X] 启动失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            shutdown();
        }
    }

    private static Process startBackend(boolean windows) throws IOException {
        Path backend = ROOT.resolve("backend");
        List<String> cmd = windows
            ? List.of("cmd.exe", "/c", MVN_CMD.toString(), "spring-boot:run")
            : List.of("mvn", "spring-boot:run");
        if (windows && !Files.exists(MVN_CMD)) {
            cmd = List.of("cmd.exe", "/c", "mvn", "spring-boot:run");
        }

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(backend.toFile());
        pb.environment().put("JAVA_HOME", JDK.toString());
        String oldPath = pb.environment().getOrDefault("PATH", "");
        pb.environment().put("PATH", JDK.resolve("bin") + ";" + oldPath);
        redirect(pb, LOGS.resolve("backend.log"));

        Process p = pb.start();
        System.out.println("[..] 后端启动中（首次编译较慢），日志: logs/backend.log");
        return p;
    }

    private static Process startFrontend(boolean windows) throws IOException {
        Path frontend = ROOT.resolve("frontend");
        if (!Files.exists(frontend.resolve("node_modules"))) {
            System.out.println("[..] 首次运行，安装前端依赖（npm install）...");
            runBlocking(windows
                ? List.of("cmd.exe", "/c", "npm", "install")
                : List.of("npm", "install"),
                frontend, LOGS.resolve("npm-install.log"));
        }

        List<String> cmd = windows
            ? List.of("cmd.exe", "/c", "npm", "run", "dev")
            : List.of("npm", "run", "dev");

        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(frontend.toFile());
        redirect(pb, LOGS.resolve("frontend.log"));

        Process p = pb.start();
        System.out.println("[..] 前端启动中，日志: logs/frontend.log");
        return p;
    }

    private static void runBlocking(List<String> cmd, Path dir, Path log) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        pb.directory(dir.toFile());
        redirect(pb, log);
        Process p = pb.start();
        try {
            p.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void redirect(ProcessBuilder pb, Path log) throws IOException {
        pb.redirectErrorStream(true);
        pb.redirectOutput(ProcessBuilder.Redirect.appendTo(log.toFile()));
    }

    /** 读取上次记录的子进程 PID，逐个强制结束（自愈：直接关窗口导致的残留）。 */
    private static void cleanupStale(String name) {
        if (!isWindows()) return;
        Path pidFile = LOGS.resolve(name + ".pids");
        if (!Files.exists(pidFile)) return;
        try {
            String content = Files.readString(pidFile).trim();
            if (!content.isEmpty()) {
                System.out.println("[..] 检测到上次残留的 " + name + " 进程，正在清理...");
                for (String pid : content.split(",")) {
                    List<String> cmd = List.of("cmd.exe", "/c", "taskkill", "/F", "/PID", pid.trim());
                    new ProcessBuilder(cmd).redirectErrorStream(true).start();
                }
                sleep(1200);
                System.out.println("[√] 残留进程已清理");
            }
            Files.deleteIfExists(pidFile);
        } catch (Exception ignored) {
            // 清理失败不阻塞启动（例如 PID 已不存在）
        }
    }

    /** 守护线程：定期把当前子进程树（含 mvn 派生的 java / node）PID 写盘，供下次启动清理。 */
    private static void startPidRecorder() {
        Thread recorder = new Thread(() -> {
            while (true) {
                savePids("backend", backendProc);
                savePids("frontend", frontendProc);
                sleep(5000);
            }
        });
        recorder.setDaemon(true);
        recorder.start();
    }

    private static void savePids(String name, Process p) {
        if (p == null || !p.isAlive()) return;
        try {
            StringBuilder sb = new StringBuilder(String.valueOf(p.pid()));
            p.descendants().forEach(h -> sb.append(",").append(h.pid()));
            Files.writeString(LOGS.resolve(name + ".pids"), sb.toString());
        } catch (Exception ignored) {
            // 记录失败不影响运行
        }
    }

    private static boolean portOpen(int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("127.0.0.1", port), 800);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().browse(URI.create(url));
            }
        } catch (Exception ignored) {
            // 无桌面环境时忽略
        }
    }

    private static void shutdown() {
        System.out.println("[..] 正在关闭前后端...");
        killProcess(backendProc);
        killProcess(frontendProc);
        sleep(800);
        killProcess(backendProc);
        killProcess(frontendProc);
        System.out.println("[√] 已退出（若直接关窗口，下次启动会自动清理残留）");
    }

    private static void killProcess(Process p) {
        if (p == null) return;
        try {
            p.descendants().forEach(ProcessHandle::destroy);
            p.destroy();
        } catch (Exception ignored) {
            // 忽略单个进程关闭失败
        }
        try {
            p.descendants().forEach(ProcessHandle::destroyForcibly);
            if (p.isAlive()) {
                p.destroyForcibly();
            }
        } catch (Exception ignored) {
            // 忽略单个进程强制关闭失败
        }
    }

    private static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}