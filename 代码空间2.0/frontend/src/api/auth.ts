import request from './request'
import type { ApiResponse, AuthResponse } from '@/types'

export const authApi = {
  register: (nickname: string, password: string) =>
    request.post<ApiResponse<AuthResponse>>('/auth/register', { nickname, password }),
  login: (nickname: string, password: string) =>
    request.post<ApiResponse<AuthResponse>>('/auth/login', { nickname, password })
}