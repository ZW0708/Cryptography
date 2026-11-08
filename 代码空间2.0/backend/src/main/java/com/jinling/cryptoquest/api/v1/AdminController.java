package com.jinling.cryptoquest.api.v1;

import com.jinling.cryptoquest.common.ApiResponse;
import com.jinling.cryptoquest.model.dto.LevelAdminView;
import com.jinling.cryptoquest.model.dto.LevelUpdateRequest;
import com.jinling.cryptoquest.model.dto.UserAdminView;
import com.jinling.cryptoquest.model.dto.UserUpdateRequest;
import com.jinling.cryptoquest.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 管理端：关卡文案/概念总结卡维护 + 用户账号管理（admin 角色）。
 * 答案哈希与用户密码哈希均不在此暴露。
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/levels")
    public ApiResponse<List<LevelAdminView>> listLevels() {
        return ApiResponse.ok(adminService.listLevels());
    }

    @PutMapping("/levels/{id}")
    public ApiResponse<LevelAdminView> updateLevel(@PathVariable Long id,
                                                    @Valid @RequestBody LevelUpdateRequest req) {
        return ApiResponse.ok(adminService.updateLevel(id, req));
    }

    @GetMapping("/users")
    public ApiResponse<List<UserAdminView>> listUsers() {
        return ApiResponse.ok(adminService.listUsers());
    }

    @PutMapping("/users/{id}")
    public ApiResponse<UserAdminView> updateUser(@PathVariable Long id,
                                                 @RequestBody UserUpdateRequest req) {
        return ApiResponse.ok(adminService.updateUser(id, req));
    }
}