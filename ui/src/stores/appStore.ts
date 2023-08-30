import { defineStore } from 'pinia'

export const useAppStore = defineStore('appStore', () => {
  const theme = ref(localStorage.getItem('theme') as string)

  const setTheme = async (newTheme: string) => {
    theme.value = newTheme
  }

  return {
    theme,
    setTheme
  }
})
