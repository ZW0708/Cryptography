import { defineStore } from 'pinia'
import { ref } from 'vue'
import { levelApi } from '@/api/level'
import type { ProgressOverview, LevelView } from '@/types'

export const useProgressStore = defineStore('progress', () => {
  const overview = ref<ProgressOverview | null>(null)
  const loading = ref(false)

  async function fetchOverview() {
    loading.value = true
    try {
      const { data } = await levelApi.overview()
      overview.value = data.data
    } finally {
      loading.value = false
    }
  }

  function findLevel(id: number): LevelView | undefined {
    return overview.value?.levels.find((l) => l.id === id)
  }

  return { overview, loading, fetchOverview, findLevel }
})