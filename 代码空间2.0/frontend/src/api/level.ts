import request from './request'
import type { ApiResponse, ProgressOverview, SummaryCardView } from '@/types'

export const levelApi = {
  overview: () => request.get<ApiResponse<ProgressOverview>>('/levels/overview'),
  summary: (id: number) => request.get<ApiResponse<SummaryCardView>>(`/levels/${id}/summary`),
  submit: (levelId: number, answer: string, durationSec = 0, hintUsed = 0) =>
    request.post<ApiResponse<boolean>>('/levels/submit', { levelId, answer, durationSec, hintUsed })
}