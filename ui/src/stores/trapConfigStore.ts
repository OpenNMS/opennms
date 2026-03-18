import { getTrapdConfiguration } from '@/services/trapdConfigurationService'
import { CreateEditMode } from '@/types'
import { TrapConfigStoreState } from '@/types/trapConfig'
import { defineStore } from 'pinia'

export const useTrapConfigStore = defineStore('useTrapConfigStore', {
  state: (): TrapConfigStoreState => ({
    isLoading: false,
    trapdConfig: null,
    SnmpV3Users: [],
    activeTab: 0,
    credentialDrawerState: {
      visible: false
    },
    createUserDrawerState: {
      visible: false,
      mode: CreateEditMode.None
    }
  }),
  actions: {
    async fetchTrapConfig() {
      // Implementation for fetching trap configuration goes here
      const response = await getTrapdConfiguration()
      this.trapdConfig = response
      this.SnmpV3Users = response.snmpv3User
    },
    openCredentialDrawer() {
      this.credentialDrawerState.visible = true
    },
    closeCredentialDrawer() {
      this.credentialDrawerState.visible = false
    },
    openCreateUserDrawer(mode: CreateEditMode) {
      this.createUserDrawerState.visible = true
      this.createUserDrawerState.mode = mode
    },
    closeCreateUserDrawer() {
      this.createUserDrawerState.visible = false
      this.createUserDrawerState.mode = CreateEditMode.None
    }
  }
})

