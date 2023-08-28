import { defineStore } from 'pinia'
import API from '@/services'
import { WhoAmIResponse } from '@/types'

export const useAuthStore = defineStore('authStore', () => {
  const whoAmI = ref({ roles: [] as string[] } as WhoAmIResponse)
  const loaded = ref(false)

  const getWhoAmI = async () => {
    const resp = await API.getWhoAmI()
    whoAmI.value = resp
  }

  return {
    loaded,
    whoAmI,
    getWhoAmI
  }
})
