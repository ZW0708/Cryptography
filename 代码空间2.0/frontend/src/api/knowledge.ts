import request from './request'
import type { ApiResponse, KnowledgeItem } from '@/types'

export const knowledgeApi = {
  list: () => request.get<ApiResponse<KnowledgeItem[]>>('/knowledge')
}