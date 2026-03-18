import { CreateEditMode } from '.'

export interface TrapConfigStoreState {
  isLoading: boolean
  activeTab: number
  credentialDrawerState: {
    visible: boolean
  }
  createUserDrawerState: {
    visible: boolean
    mode: CreateEditMode
  }
}

