import request from './request'
import type { ApiResponse, HintResponse } from '@/types'

export const aiApi = {
  hint: (levelId: number, question: string, currentLevel: number) =>
    request.post<ApiResponse<HintResponse>>('/ai/hint', { levelId, question, currentLevel })
}