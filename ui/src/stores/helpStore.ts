import { defineStore } from 'pinia'
import API from '@/services'

export const useHelpStore = defineStore('helpStore', () => {
  const openApi = ref({} as Record<string, unknown>)
  const openApiV1 = ref({} as Record<string, unknown>)

  const getOpenApi = async () => {
    const resp = await API.getOpenApi()
    openApi.value = resp

    return resp
  }

  const getOpenApiV1 = async () => {
    const resp = await API.getOpenApiV1()
    openApiV1.value = resp

    return resp
  }

  return {
    openApi,
    openApiV1,
    getOpenApi,
    getOpenApiV1
  }
})
