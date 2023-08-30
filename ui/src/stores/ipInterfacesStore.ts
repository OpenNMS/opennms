import { defineStore } from 'pinia'
import API from '@/services'
import { IpInterface, QueryParameters } from '@/types'

export const useIpInterfacesStore = defineStore('ipInterfacesStore', () => {
  const ipInterfaces = ref([] as IpInterface[])

  const getAllIpInterfaces = async (queryParameters?: QueryParameters) => {
    const defaultParams = queryParameters || { limit: 5000, offset: 0 }
    const resp = await API.getIpInterfaces(defaultParams)

    if (resp) {
      ipInterfaces.value = [...resp.ipInterface]
    }
  }

  return {
    ipInterfaces,
    getAllIpInterfaces
  }
})
