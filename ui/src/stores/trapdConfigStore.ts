import { getDefaultTrapdConfig } from '@/lib/trapdValidator'
import { getTrapdConfiguration } from '@/services/trapdConfigurationService'
import { CreateEditMode } from '@/types'
import { TrapConfigStoreState } from '@/types/trapConfig'
import { defineStore } from 'pinia'

export const useTrapdConfigStore = defineStore('useTrapdConfigStore', {
  state: (): TrapConfigStoreState => ({
    isLoading: false,
    trapdConfig: getDefaultTrapdConfig(),
    snmpV3Users: [],
    activeTab: 0,
    credentialDrawerState: {
      visible: false,
      key: null
    },
    createUserDrawerState: {
      visible: false,
      mode: CreateEditMode.None,
      selectedUserIndex: -1
    }
  }),
  actions: {
    async fetchTrapConfig() {
      // Implementation for fetching trap configuration goes here
      const response = await getTrapdConfiguration()
      this.trapdConfig = response
      this.snmpV3Users = response.snmpv3User
    },
    openCredentialDrawer(key: string) {
      this.credentialDrawerState.visible = true
      this.credentialDrawerState.key = key
    },
    closeCredentialDrawer() {
      this.credentialDrawerState.visible = false
      this.credentialDrawerState.key = null
    },
    openCreateUserDrawer(mode: CreateEditMode, selectedUserIndex: number) {
      this.createUserDrawerState.visible = true
      this.createUserDrawerState.mode = mode
      this.createUserDrawerState.selectedUserIndex = selectedUserIndex
    },
    closeCreateUserDrawer() {
      this.createUserDrawerState.visible = false
      this.createUserDrawerState.mode = CreateEditMode.None
      this.createUserDrawerState.selectedUserIndex = -1
    }
  }
})

