import { getDefaultTrapdConfig } from '@/lib/trapdValidator'
import { getTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { CreateEditMode } from '@/types'
import type { TrapConfig } from '@/types/trapConfig'
import { createPinia, setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/trapdConfigurationService', () => ({
  getTrapdConfiguration: vi.fn()
}))

describe('useTrapdConfigStore', () => {
  let store: ReturnType<typeof useTrapdConfigStore>

  const trapConfigResponse: TrapConfig = {
    snmpTrapAddress: '192.168.0.20',
    snmpTrapPort: 1162,
    newSuspectOnTrap: true,
    includeRawMessage: true,
    threads: 8,
    queueSize: 12000,
    batchSize: 1500,
    batchInterval: 700,
    useAddressFromVarbind: true,
    snmpv3User: [
      {
        engineId: null,
        securityName: 'alpha-user',
        securityLevel: 2,
        authProtocol: 'SHA-256',
        authPassphrase: 'masked-auth',
        privacyProtocol: null,
        privacyPassphrase: null
      }
    ]
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(createPinia())
    store = useTrapdConfigStore()
  })

  it('has the expected initial state', () => {
    expect(store.isLoading).toBe(false)
    expect(store.trapdConfig).toEqual(getDefaultTrapdConfig())
    expect(store.snmpV3Users).toEqual([])
    expect(store.activeTab).toBe(0)
    expect(store.credentialDrawerState).toEqual({
      visible: false,
      key: null
    })
    expect(store.createUserDrawerState).toEqual({
      visible: false,
      mode: CreateEditMode.None,
      selectedUserIndex: -1
    })
  })

  it('fetchTrapConfig updates trapdConfig and snmpV3Users from service response', async () => {
    vi.mocked(getTrapdConfiguration).mockResolvedValue(trapConfigResponse)

    await store.fetchTrapConfig()

    expect(getTrapdConfiguration).toHaveBeenCalledTimes(1)
    expect(store.trapdConfig).toEqual(trapConfigResponse)
    expect(store.snmpV3Users).toEqual(trapConfigResponse.snmpv3User)
  })

  it('fetchTrapConfig propagates errors and keeps prior state unchanged', async () => {
    const previousConfig = store.trapdConfig
    const previousUsers = store.snmpV3Users
    const error = new Error('fetch failed')
    vi.mocked(getTrapdConfiguration).mockRejectedValue(error)

    await expect(store.fetchTrapConfig()).rejects.toThrow('fetch failed')
    expect(store.trapdConfig).toBe(previousConfig)
    expect(store.snmpV3Users).toBe(previousUsers)
  })

  it('openCredentialDrawer sets visibility and key', () => {
    store.openCredentialDrawer('authPassphrase')

    expect(store.credentialDrawerState.visible).toBe(true)
    expect(store.credentialDrawerState.key).toBe('authPassphrase')
  })

  it('openCredentialDrawer replaces key when called again', () => {
    store.openCredentialDrawer('authPassphrase')
    store.openCredentialDrawer('privacyPassphrase')

    expect(store.credentialDrawerState.visible).toBe(true)
    expect(store.credentialDrawerState.key).toBe('privacyPassphrase')
  })

  it('closeCredentialDrawer hides drawer and clears key', () => {
    store.openCredentialDrawer('authPassphrase')

    store.closeCredentialDrawer()

    expect(store.credentialDrawerState.visible).toBe(false)
    expect(store.credentialDrawerState.key).toBe(null)
  })

  it('openCreateUserDrawer sets create drawer state', () => {
    store.openCreateUserDrawer(CreateEditMode.Edit, 3)

    expect(store.createUserDrawerState.visible).toBe(true)
    expect(store.createUserDrawerState.mode).toBe(CreateEditMode.Edit)
    expect(store.createUserDrawerState.selectedUserIndex).toBe(3)
  })

  it('closeCreateUserDrawer resets create drawer state to defaults', () => {
    store.openCreateUserDrawer(CreateEditMode.Create, 0)

    store.closeCreateUserDrawer()

    expect(store.createUserDrawerState).toEqual({
      visible: false,
      mode: CreateEditMode.None,
      selectedUserIndex: -1
    })
  })
})
