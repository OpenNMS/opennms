import { SnmpConfig, SnmpConfigStoreState } from '@/types/snmpConfig'
import { defineStore } from 'pinia'

export const defaultSnmpConfig = () => {
  return {
    definitions: [],
    profiles: []
  } as SnmpConfig
}

export const useSnmpConfigStore = defineStore('useSnmpConfigStore', {
  state: (): SnmpConfigStoreState => ({
    config: defaultSnmpConfig(),
    isLoading: false,
    activeTab: 0
  }),
  actions: {
    async fetchAllSourcesNames() {
    }
  }
})
