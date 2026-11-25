import request from './request'
import type { AgentChatResponse, ApiResponse, HintResponse } from '@/types'

export const aiApi = {
  hint: (levelId: number, question: string, currentLevel: number) =>
    request.post<ApiResponse<HintResponse>>('/ai/hint', { levelId, question, currentLevel }),

  chat: (
    levelId: number,
    question: string,
    currentLevel: number,
    puzzleState?: Record<string, string>
  ) =>
    request.post<ApiResponse<AgentChatResponse>>('/ai/chat', {
      levelId,
      question,
      currentLevel,
      puzzleState
    })
}