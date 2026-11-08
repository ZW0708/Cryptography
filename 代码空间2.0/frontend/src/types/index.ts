export interface AuthResponse {
  token: string
  nickname: string
  role: string
}

export interface LevelView {
  id: number
  code: string
  title: string
  difficulty: number
  cryptoConcept: string
  introText: string
  unlocked: boolean
  cleared: boolean
}

export interface ProgressOverview {
  totalLevels: number
  clearedCount: number
  levels: LevelView[]
}

export interface SummaryCardView {
  code: string
  title: string
  summaryCard: string
}

export interface HintResponse {
  hintLevel: number
  content: string
  escalateAvailable: boolean
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
}
export interface AdminLevelView {
  id: number
  code: string
  title: string
  difficulty: number
  cryptoConcept: string
  introText: string
  summaryCard: string
  unlockChain: string
}

export interface LevelUpdatePayload {
  title?: string
  introText?: string
  summaryCard?: string
}
export interface UserAdminView {
  id: number
  nickname: string
  role: string
  createdAt: string
  locked: boolean
  failedAttempts: number
}

export interface UserUpdatePayload {
  password?: string
  role?: string
  locked?: boolean
}
export interface KnowledgeSection {
  heading: string
  content: string
}

export interface KnowledgeItem {
  code: string
  title: string
  concept: string
  overview: string
  sections: KnowledgeSection[]
  tips: string[]
  solution?: string[] | null
  answer?: string | null
  analysis?: string | null
}

export interface AnswerView {
  code: string
  title: string
  difficulty: number
  cleared: boolean
  failedAttempts: number
  unlockThreshold: number
  unlocked: boolean
  solution?: string[] | null
  answer?: string | null
  analysis?: string | null
}