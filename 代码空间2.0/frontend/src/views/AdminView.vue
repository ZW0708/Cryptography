<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { adminApi } from '@/api/admin'
import CipherSkeleton from '@/components/CipherSkeleton.vue'
import { useToast } from '@/composables/useToast'
import type { AdminLevelView, UserAdminView } from '@/types'

type Tab = 'levels' | 'users'

const auth = useAuthStore()
const router = useRouter()
const { toast } = useToast()

const tab = ref<Tab>('levels')
const loading = ref(false)

/* ---------------- 关卡 ---------------- */
const levels = ref<AdminLevelView[]>([])
const selectedId = ref<number | null>(null)
const form = ref({ title: '', introText: '', summaryCard: '' })
const saving = ref(false)

const selected = computed(() => levels.value.find((l) => l.id === selectedId.value) ?? null)

function pick(lv: AdminLevelView) {
  selectedId.value = lv.id
  form.value = { title: lv.title, introText: lv.introText, summaryCard: lv.summaryCard }
}

async function load() {
  if (!auth.isAdmin()) {
    router.replace('/levels')
    return
  }
  loading.value = true
  try {
    const { data } = await adminApi.listLevels()
    levels.value = data.data
    if (levels.value.length && selectedId.value == null) {
      pick(levels.value[0])
    }
  } catch (e: any) {
    toast(e.response?.data?.message ?? '加载关卡失败', 'error')
  } finally {
    loading.value = false
  }
}

async function save() {
  if (!selected.value) return
  saving.value = true
  try {
    const { data } = await adminApi.updateLevel(selected.value.id, { ...form.value })
    const updated = data.data
    const idx = levels.value.findIndex((l) => l.id === updated.id)
    if (idx >= 0) levels.value[idx] = updated
    toast('保存成功', 'success')
  } catch (e: any) {
    toast(e.response?.data?.message ?? '保存失败', 'error')
  } finally {
    saving.value = false
  }
}

/* ---------------- 用户 ---------------- */
const users = ref<UserAdminView[]>([])
const usersLoading = ref(false)

function switchTab(t: Tab) {
  tab.value = t
  if (t === 'users' && users.value.length === 0) {
    loadUsers()
  }
}

async function loadUsers() {
  usersLoading.value = true
  try {
    const { data } = await adminApi.listUsers()
    users.value = data.data
  } catch (e: any) {
    toast(e.response?.data?.message ?? '加载用户失败', 'error')
  } finally {
    usersLoading.value = false
  }
}

function replaceUser(u: UserAdminView) {
  const idx = users.value.findIndex((x) => x.id === u.id)
  if (idx >= 0) users.value[idx] = u
}

function onRoleSelect(u: UserAdminView, e: Event) {
  changeRole(u, (e.target as HTMLSelectElement).value)
}

async function changeRole(u: UserAdminView, role: string) {
  if (!role || role === u.role) return
  try {
    const { data } = await adminApi.updateUser(u.id, { role })
    replaceUser(data.data)
    toast(`已将 ${u.nickname} 设为 ${role}`, 'success')
  } catch (e: any) {
    toast(e.response?.data?.message ?? '修改角色失败', 'error')
    loadUsers()
  }
}

async function toggleLock(u: UserAdminView) {
  const next = !u.locked
  if (next && !window.confirm(`确定锁定账号 ${u.nickname}？锁定后该账号将无法登录。`)) return
  try {
    const { data } = await adminApi.updateUser(u.id, { locked: next })
    replaceUser(data.data)
    toast(next ? `已锁定 ${u.nickname}` : `已解锁 ${u.nickname}`, 'success')
  } catch (e: any) {
    toast(e.response?.data?.message ?? '操作失败', 'error')
  }
}

/* ---------------- 重置密码弹窗 ---------------- */
const resetTarget = ref<UserAdminView | null>(null)
const resetPwd = ref('')
const resetPwdErr = ref('')
const resetting = ref(false)

function openReset(u: UserAdminView) {
  resetTarget.value = u
  resetPwd.value = ''
  resetPwdErr.value = ''
}

function closeReset() {
  if (resetting.value) return
  resetTarget.value = null
}

async function confirmReset() {
  if (!resetTarget.value) return
  const pwd = resetPwd.value
  if (!/^.{6,64}$/.test(pwd) || !/[a-zA-Z]/.test(pwd) || !/\d/.test(pwd)) {
    resetPwdErr.value = '密码需 6-64 位且包含字母和数字'
    return
  }
  resetting.value = true
  try {
    const { data } = await adminApi.updateUser(resetTarget.value.id, { password: pwd })
    replaceUser(data.data)
    toast(`已重置 ${resetTarget.value.nickname} 的密码`, 'success')
    resetTarget.value = null
  } catch (e: any) {
    resetPwdErr.value = e.response?.data?.message ?? '重置密码失败'
  } finally {
    resetting.value = false
  }
}

function fmt(s: string) {
  if (!s) return '-'
  const d = new Date(s)
  return isNaN(d.getTime()) ? s : d.toLocaleString('zh-CN')
}

onMounted(load)
</script>

<template>
  <div>
    <div class="row" style="margin-bottom: 12px">
      <h2 style="margin: 0">🛠 管理后台</h2>
      <span class="spacer" />
      <RouterLink to="/levels"><button class="secondary">返回关卡</button></RouterLink>
    </div>
    <p class="muted" style="font-size: 13px">
      仅管理员可访问。关卡可维护标题/介绍/总结卡；用户可以重置密码、锁定/解锁、调整角色。答案与密码哈希不在前端暴露。
    </p>

    <div class="row" style="margin-bottom: 12px">
      <button :class="{ secondary: tab !== 'levels' }" @click="switchTab('levels')">关卡管理</button>
      <button :class="{ secondary: tab !== 'users' }" @click="switchTab('users')">用户管理</button>
    </div>

    <!-- 关卡管理 -->
    <template v-if="tab === 'levels'">
      <CipherSkeleton v-if="loading" :rows="4" />

      <div v-else class="card admin-levels">
        <div class="level-nav">
          <h3 style="margin-top: 0">关卡列表</h3>
          <div
            v-for="lv in levels"
            :key="lv.id"
            class="level-item"
            :class="{ active: lv.id === selectedId }"
            @click="pick(lv)"
          >
            <strong>{{ lv.code }}</strong>
            <span class="muted level-diff">难度 {{ lv.difficulty }}</span>
            <div class="muted level-title">{{ lv.title }}</div>
          </div>
          <p v-if="!levels.length" class="muted">暂无关卡</p>
        </div>

        <div class="level-editor">
          <template v-if="selected">
            <h3 style="margin-top: 0">编辑：{{ selected.code }}</h3>

            <label>标题</label>
            <input v-model="form.title" placeholder="关卡标题" />

            <label>关卡介绍</label>
            <textarea v-model="form.introText" rows="4" placeholder="关卡引导文案" />

            <label>概念总结卡</label>
            <textarea v-model="form.summaryCard" rows="4" placeholder="通关后展示的知识总结" />

            <div style="margin-top: 14px">
              <button :disabled="saving" @click="save">{{ saving ? '保存中…' : '保存' }}</button>
            </div>
          </template>
          <p v-else class="muted">请从左侧选择关卡</p>
        </div>
      </div>
    </template>

    <!-- 用户管理 -->
    <template v-else>
      <div class="card">
        <div class="row" style="margin-bottom: 8px">
          <h3 style="margin: 0">用户账号</h3>
          <span class="spacer" />
          <button class="secondary sm" @click="loadUsers">刷新</button>
        </div>

        <CipherSkeleton v-if="usersLoading" :rows="4" />
        <div v-else class="table-scroll">
          <table class="data">
            <thead>
              <tr>
                <th>账号</th>
                <th>角色</th>
                <th>注册时间</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="u in users" :key="u.id">
                <td>{{ u.nickname }}</td>
                <td>
                  <select :value="u.role" class="role-select" @change="onRoleSelect(u, $event)">
                    <option value="USER">USER</option>
                    <option value="ADMIN">ADMIN</option>
                  </select>
                </td>
                <td class="muted">{{ fmt(u.createdAt) }}</td>
                <td>
                  <span :class="u.locked ? 'badge badge-danger' : 'badge badge-success'">
                    {{ u.locked ? '已锁定' : '正常' }}
                  </span>
                </td>
                <td>
                  <div class="row" style="gap: 8px">
                    <button class="secondary sm" @click="openReset(u)">重置密码</button>
                    <button class="secondary sm" @click="toggleLock(u)">{{ u.locked ? '解锁' : '锁定' }}</button>
                  </div>
                </td>
              </tr>
              <tr v-if="!users.length">
                <td colspan="5" class="muted center" style="padding: 16px">暂无用户</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>

    <!-- 重置密码弹窗 -->
    <div v-if="resetTarget" class="modal-mask" @click.self="closeReset">
      <div class="card modal">
        <h3 style="margin-top: 0">重置密码：{{ resetTarget.nickname }}</h3>
        <label>新密码（6-64 位，含字母 + 数字）</label>
        <input v-model="resetPwd" type="password" placeholder="输入新密码" @keyup.enter="confirmReset" />
        <p v-if="resetPwdErr" class="field-error">{{ resetPwdErr }}</p>
        <div class="row" style="margin-top: 16px; justify-content: flex-end">
          <button class="secondary" :disabled="resetting" @click="closeReset">取消</button>
          <button :disabled="resetting" @click="confirmReset">{{ resetting ? '提交中…' : '确认重置' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.admin-levels {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.level-nav {
  width: 230px;
  flex-shrink: 0;
}

.level-item {
  padding: 9px 11px;
  margin-bottom: 6px;
  border: 1px solid var(--border);
  border-radius: 8px;
  cursor: pointer;
  transition: background 0.15s, border-color 0.15s;
}

.level-item:hover {
  background: var(--surface-2);
}

.level-item.active {
  border-color: var(--primary);
  background: var(--primary-light);
  box-shadow: 0 0 14px rgba(62, 242, 176, 0.15);
}

.level-diff {
  float: right;
  font-size: 12px;
}

.level-title {
  font-size: 13px;
  margin-top: 2px;
}

.level-editor {
  flex: 1;
  min-width: 0;
}

.role-select {
  width: auto;
  padding: 4px 8px;
}

.table-scroll {
  overflow-x: auto;
}

.field-error {
  color: var(--danger);
  font-size: 13px;
  margin: 6px 0 0;
}

.modal-mask {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 16px;
}

.modal {
  width: 100%;
  max-width: 420px;
  margin: 0;
}

@media (max-width: 768px) {
  .admin-levels {
    flex-direction: column;
  }

  .level-nav {
    width: 100%;
  }
}
</style>