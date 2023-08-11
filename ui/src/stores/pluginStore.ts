import { defineStore } from 'pinia'
import API from '@/services'
import { Plugin } from '@/types'

export const usePluginStore = defineStore('pluginStore', () => {
  const plugins = ref([] as Plugin[])

  const getPlugins = async () => {
    const resp = await API.getPlugins()
    plugins.value = resp
  }

  return {
    plugins,
    getPlugins
  }
})
