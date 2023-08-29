import { defineStore } from 'pinia'
import API from '@/services'
import { IfService, QueryParameters } from '@/types'

export const useIfServiceStore = defineStore('ifServiceStore', () => {
  const ifServices = ref([] as IfService[])
  const totalCount = ref(0)

  const getNodeIfServices = async (queryParameters?: QueryParameters) => {
    const resp = await API.getNodeIfServices(queryParameters)

    if (resp) {
      ifServices.value = resp['monitored-service']
      totalCount.value = resp.totalCount
      return resp['monitored-service']
    }
  }

  return {
    ifServices,
    totalCount,
    getNodeIfServices
  }
})
