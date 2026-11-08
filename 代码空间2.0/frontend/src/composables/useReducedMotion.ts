import { ref } from 'vue'

/** 全站统一的「减少动态效果」开关（系统 prefers-reduced-motion）。 */
const media = window.matchMedia('(prefers-reduced-motion: reduce)')
const reduced = ref(media.matches)

media.addEventListener('change', (e) => {
  reduced.value = e.matches
})

export function useReducedMotion() {
  return reduced
}