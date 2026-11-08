import request from './request'
import type { AnswerView, ApiResponse } from '@/types'

export const answerApi = {
  list: () => request.get<ApiResponse<AnswerView[]>>('/answers')
}