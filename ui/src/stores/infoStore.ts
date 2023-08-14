import { defineStore } from 'pinia'
import API from '@/services'
import { AppInfo } from '@/types'

export const useInfoStore = defineStore('infoStore', () => {
  const info = ref({} as AppInfo)

  const getInfo = async () => {
    const resp = await API.getInfo()
    info.value = resp
  }

  return {
    info,
    getInfo
  }
})
