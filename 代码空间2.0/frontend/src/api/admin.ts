import request from './request'
import type {
  AdminLevelView,
  ApiResponse,
  LevelUpdatePayload,
  UserAdminView,
  UserUpdatePayload
} from '@/types'

export const adminApi = {
  listLevels: () => request.get<ApiResponse<AdminLevelView[]>>('/admin/levels'),
  updateLevel: (id: number, payload: LevelUpdatePayload) =>
    request.put<ApiResponse<AdminLevelView>>(`/admin/levels/${id}`, payload),
  listUsers: () => request.get<ApiResponse<UserAdminView[]>>('/admin/users'),
  updateUser: (id: number, payload: UserUpdatePayload) =>
    request.put<ApiResponse<UserAdminView>>(`/admin/users/${id}`, payload)
}