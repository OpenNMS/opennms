import CreateSnmpV3User from '@/components/TrapdConfiguration/CreateSnmpV3User.vue'
import {
  AUTH_PROTOCOL_OPTIONS,
  getDefaultTrapdConfig,
  PRIVACY_PROTOCOL_OPTIONS,
  SECURITY_LEVEL_OPTIONS
} from '@/lib/trapdValidator'
import { mapUserToServer } from '@/mappers/trapdConfig.mapper'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useScvStore } from '@/stores/scvStore'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { CreateEditMode } from '@/types'
import type { SnmpV3User } from '@/types/trapConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, nextTick } from 'vue'

const { showSnackBarMock, populateScvMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn(),
  populateScvMock: vi.fn().mockResolvedValue(undefined)
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: showSnackBarMock
  })
}))

vi.mock('@/mappers/trapdConfig.mapper', () => ({
  mapUserToServer: vi.fn()
}))

vi.mock('@/services/trapdConfigurationService', () => ({
  updateTrapdConfiguration: vi.fn()
}))

vi.mock('@/stores/scvStore', () => ({
  useScvStore: vi.fn(() => ({
    populate: populateScvMock
  }))
}))

const FeatherInputStub = defineComponent({
  name: 'FeatherInput',
  props: {
    modelValue: {
      type: String,
      default: ''
    },
    label: {
      type: String,
      default: ''
    },
    dataTest: {
      type: String,
      default: ''
    }
  },
  emits: ['update:modelValue'],
  template:
    '<input :data-test="dataTest || label" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
})

const ScvSearchDrawerStub = defineComponent({
  name: 'ScvSearchDrawer',
  props: {
    isOpen: {
      type: Boolean,
      default: false
    }
  },
  emits: ['hidden', 'itemSelected'],
  template: '<div data-test="scv-search-drawer" :data-open="String(isOpen)" />'
})

describe('CreateSnmpV3User.vue', () => {
  let store: ReturnType<typeof useTrapdConfigStore>
  const useScvStoreMock = vi.mocked(useScvStore)
  const mapUserToServerMock = vi.mocked(mapUserToServer)
  const updateTrapdConfigurationMock = vi.mocked(updateTrapdConfiguration)

  const selectedUser: SnmpV3User = {
    engineId: null,
    securityName: 'existing-user',
    securityLevel: 2,
    authProtocol: 'MD5',
    authPassphrase: 'masked-auth',
    privacyProtocol: null,
    privacyPassphrase: null
  }

  const mountComponent = () => {
    return mount(CreateSnmpV3User, {
      global: {
        stubs: {
          TableCard: {
            template: '<div><slot /></div>'
          },
          FeatherIcon: true,
          FeatherInput: FeatherInputStub,
          'feather-input': FeatherInputStub,
          FeatherSelect: true,
          'feather-select': true,
          ScvInputIcon: {
            emits: ['click'],
            template: '<button :data-test="$attrs[\'data-test\']" @click="$emit(\'click\')" />'
          },
          ScvSearchDrawer: ScvSearchDrawerStub,
          FeatherButton: {
            props: ['dataTest', 'disabled'],
            emits: ['click'],
            template: '<button :data-test="dataTest" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
          },
          'feather-button': {
            props: ['dataTest', 'disabled'],
            emits: ['click'],
            template: '<button :data-test="dataTest" :disabled="disabled" @click="$emit(\'click\')"><slot /></button>'
          }
        }
      }
    })
  }

  const setInputValue = async (wrapper: ReturnType<typeof mountComponent>, dataTest: string, value: string) => {
    const input = wrapper.find(`input[data-test="${dataTest}"]`)
    expect(input.exists()).toBe(true)
    await input.setValue(value)
  }

  const setBindingValue = async (wrapper: ReturnType<typeof mountComponent>, key: string, value: any) => {
    ;(wrapper.vm as any)[key] = value
    await nextTick()
  }

  const clickButton = async (wrapper: ReturnType<typeof mountComponent>, dataTest: string) => {
    const button = wrapper.findComponent(`[data-test="${dataTest}"]`)
    expect(button.exists()).toBe(true)
    await (button as any).vm.$emit('click')
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    setActivePinia(
      createTestingPinia({
        stubActions: false,
        createSpy: vi.fn
      })
    )

    store = useTrapdConfigStore()
    store.createUserDrawerState.visible = true
    store.createUserDrawerState.mode = CreateEditMode.Create
    store.createUserDrawerState.selectedUserIndex = -1
    store.snmpV3Users = [selectedUser]
    store.trapdConfig = {
      ...getDefaultTrapdConfig(),
      snmpv3User: [selectedUser]
    }

    store.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)
    store.closeCreateUserDrawer = vi.fn()
    store.openCredentialDrawer = vi.fn()
    store.closeCredentialDrawer = vi.fn()

    mapUserToServerMock.mockImplementation((payload) => payload as SnmpV3User)
    updateTrapdConfigurationMock.mockResolvedValue(undefined)
  })

  it('calls scvStore.populate on mount', () => {
    mountComponent()

    expect(useScvStoreMock).toHaveBeenCalledTimes(1)
    expect(populateScvMock).toHaveBeenCalledTimes(1)
  })

  it('does not render when drawer is hidden', () => {
    store.createUserDrawerState.visible = false
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="create-snmpv3-user"]').exists()).toBe(false)
  })

  it('renders create mode with heading and action buttons', () => {
    const wrapper = mountComponent()

    expect(wrapper.find('h3').text()).toBe('New SNMPv3 User Management')
    expect(wrapper.find('[data-test="create-user-button"]').text()).toContain('Create User')
    expect(wrapper.find('[data-test="cancel-button"]').exists()).toBe(true)
  })

  it('renders update label and preloads security name in edit mode', async () => {
    store.createUserDrawerState.mode = CreateEditMode.Edit
    store.createUserDrawerState.selectedUserIndex = 0

    const wrapper = mountComponent()
    await nextTick()
    await nextTick()

    expect(wrapper.find('[data-test="create-user-button"]').text()).toContain('Update User')
    expect((wrapper.find('input[data-test="security-name-input"]').element as HTMLInputElement).value).toBe(
      'existing-user'
    )
  })

  it('calls closeCreateUserDrawer from back and cancel buttons', async () => {
    const wrapper = mountComponent()

    await wrapper.findAll('button')[0].trigger('click')
    await wrapper.find('[data-test="cancel-button"]').trigger('click')

    expect(store.closeCreateUserDrawer).toHaveBeenCalledTimes(2)
  })

  it('opens credential drawer from auth passphrase button in edit mode', async () => {
    store.createUserDrawerState.mode = CreateEditMode.Edit
    store.createUserDrawerState.selectedUserIndex = 0

    const wrapper = mountComponent()
    await nextTick()

    await wrapper.find('[data-test="auth-passphrase-save-button"]').trigger('click')

    expect(store.openCredentialDrawer).toHaveBeenCalledWith('auth')
  })

  it('opens credential drawer from privacy passphrase button when privacy row is visible', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[2])
    await wrapper.find('[data-test="privacy-passphrase-save-button"]').trigger('click')

    expect(store.openCredentialDrawer).toHaveBeenCalledWith('privacy')
  })

  it('toggles auth/privacy rows based on security level', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])
    expect(wrapper.find('[data-test="auth-passphrase-input"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="privacy-passphrase-input"]').exists()).toBe(false)

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[1])
    expect(wrapper.find('[data-test="auth-passphrase-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="privacy-passphrase-input"]').exists()).toBe(false)

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[2])
    expect(wrapper.find('[data-test="auth-passphrase-input"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="privacy-passphrase-input"]').exists()).toBe(true)
  })

  it('clears dependent values when security level is lowered to noAuthNoPriv', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[2])
    await setBindingValue(wrapper, 'authProtocol', AUTH_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'privacyProtocol', PRIVACY_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'authPassphrase', 'auth-secret')
    await setBindingValue(wrapper, 'privacyPassphrase', 'privacy-secret')

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])

    expect((wrapper.vm as any).authProtocol).toBeUndefined()
    expect((wrapper.vm as any).privacyProtocol).toBeUndefined()
    expect((wrapper.vm as any).authPassphrase).toBe('')
    expect((wrapper.vm as any).privacyPassphrase).toBe('')
  })

  it('fills auth passphrase from SCV selection and closes SCV drawer', async () => {
    const wrapper = mountComponent()
    store.credentialDrawerState.key = 'auth'
    ;(wrapper.vm as any).scvItemSelected({ alias: 'vault', key: 'auth-key' })
    await nextTick()

    expect((wrapper.vm as any).authPassphrase).toBe('${scv:vault:auth-key}')
    expect(store.closeCredentialDrawer).toHaveBeenCalledTimes(1)
  })

  it('fills privacy passphrase from SCV selection and closes SCV drawer', async () => {
    const wrapper = mountComponent()
    store.credentialDrawerState.key = 'privacy'
    ;(wrapper.vm as any).scvItemSelected({ alias: 'vault', key: 'privacy-key' })
    await nextTick()

    expect((wrapper.vm as any).privacyPassphrase).toBe('${scv:vault:privacy-key}')
    expect(store.closeCredentialDrawer).toHaveBeenCalledTimes(1)
  })

  it('creates user successfully in create mode', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'new-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])
    await clickButton(wrapper, 'create-user-button')

    expect(mapUserToServerMock).toHaveBeenCalledWith(
      expect.objectContaining({
        securityName: 'new-user',
        securityLevel: expect.any(Number)
      })
    )
    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)
    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(
      expect.objectContaining({
        snmpv3User: expect.arrayContaining([expect.objectContaining({ securityName: 'new-user' })])
      })
    )
    expect(store.fetchTrapConfig).toHaveBeenCalledTimes(1)
    expect(store.closeCreateUserDrawer).toHaveBeenCalledTimes(1)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'SNMPv3 user created successfully.' })
  })

  it('updates user successfully in edit mode', async () => {
    store.createUserDrawerState.mode = CreateEditMode.Edit
    store.createUserDrawerState.selectedUserIndex = 0

    const wrapper = mountComponent()
    await nextTick()

    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[1])
    await setBindingValue(wrapper, 'authProtocol', AUTH_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'authPassphrase', 'masked-auth')
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)
    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(
      expect.objectContaining({
        snmpv3User: expect.arrayContaining([expect.objectContaining({ securityName: 'existing-user' })])
      })
    )
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'SNMPv3 user updated successfully.' })
  })

  it('shows explicit edit error when selected user cannot be found', async () => {
    store.snmpV3Users = []
    store.createUserDrawerState.mode = CreateEditMode.Edit
    store.createUserDrawerState.selectedUserIndex = 0

    const wrapper = mountComponent()
    await nextTick()

    await setInputValue(wrapper, 'security-name-input', 'replacement-name')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[1])
    await setBindingValue(wrapper, 'authProtocol', AUTH_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'authPassphrase', 'masked-auth')
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(store.fetchTrapConfig).not.toHaveBeenCalled()
    expect(store.closeCreateUserDrawer).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Unable to determine the selected SNMPv3 user to update.',
      error: true
    })
  })

  it('requires auth protocol and auth passphrase for auth-only security level', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'auth-only-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[1])
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Please fix validation errors before saving.',
      error: true
    })
  })

  it('requires privacy protocol and privacy passphrase for auth-priv security level', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'auth-priv-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[2])
    await setBindingValue(wrapper, 'authProtocol', AUTH_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'authPassphrase', 'auth-secret')
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Please fix validation errors before saving.',
      error: true
    })
  })

  it('shows service error when updateTrapdConfiguration throws Error', async () => {
    updateTrapdConfigurationMock.mockRejectedValue(new Error('save failed'))
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'new-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])
    await clickButton(wrapper, 'create-user-button')

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'save failed', error: true })
  })

  it('shows generic service error when updateTrapdConfiguration throws non-Error', async () => {
    updateTrapdConfigurationMock.mockRejectedValue('boom')
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'new-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])
    await clickButton(wrapper, 'create-user-button')

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to save SNMPv3 user.', error: true })
  })

  it('prevents duplicate create requests while saving is in progress', async () => {
    let resolveSave: () => void = () => undefined
    updateTrapdConfigurationMock.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveSave = resolve
        })
    )
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'new-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])

    await clickButton(wrapper, 'create-user-button')
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)

    resolveSave()
    await flushPromises()
  })

  it('shows validation message when trying to save with empty security name', async () => {
    const wrapper = mountComponent()

    await clickButton(wrapper, 'create-user-button')

    expect(showSnackBarMock).toHaveBeenCalledWith({
      msg: 'Please fix validation errors before saving.',
      error: true
    })
    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
  })
})
