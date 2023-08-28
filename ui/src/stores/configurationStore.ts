import { defineStore } from 'pinia'
import API from '@/services'

export const useConfigurationStore = defineStore('configurationStore', () => {
  const types = ref([] as any[])
  const provisionDService = ref(null as any)
  const sendModifiedData = ref(null as any)

  const getProvisionDService = async () => {
    const resp = await API.getProvisionDService()

    if (resp) {
      provisionDService.value = resp
    }
  }

  return {
    types,
    provisionDService,
    sendModifiedData,
    getProvisionDService
  }
})
