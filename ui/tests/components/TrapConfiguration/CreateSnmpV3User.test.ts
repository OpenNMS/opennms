import CreateSnmpV3User from '@/components/TrapConfiguration/CreateSnmpV3User.vue'
import { AUTH_PROTOCOL_OPTIONS, getDefaultTrapdConfig, SECURITY_LEVEL_OPTIONS } from '@/lib/trapdValidator'
import { mapUserToServer } from '@/mappers/trapdConfig.mapper'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapConfigStore } from '@/stores/trapConfigStore'
import { CreateEditMode } from '@/types'
import type { SnmpV3User } from '@/types/trapConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, nextTick } from 'vue'

const { showSnackBarMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn()
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
  template: '<input :data-test="dataTest || label" :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />'
})

describe('CreateSnmpV3User.vue', () => {
  let store: ReturnType<typeof useTrapConfigStore>
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
          SearchExistingCredential: true,
          FeatherIcon: true,
          FeatherInput: FeatherInputStub,
          'feather-input': FeatherInputStub,
          FeatherSelect: true,
          'feather-select': true,
          ScvInputIcon: {
            emits: ['click'],
            template: '<button :data-test="$attrs[\'data-test\']" @click="$emit(\'click\')" />'
          },
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

    store = useTrapConfigStore()
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

    mapUserToServerMock.mockImplementation((payload) => payload as SnmpV3User)
    updateTrapdConfigurationMock.mockResolvedValue(undefined)
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

    expect(wrapper.find('[data-test="create-user-button"]').text()).toContain('Update User')
    expect((wrapper.find('input[data-test="security-name-input"]').element as HTMLInputElement).value).toBe('existing-user')
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

    expect(store.openCredentialDrawer).toHaveBeenCalledTimes(1)
  })

  it('creates user successfully in create mode', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'new-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[0])
    await clickButton(wrapper, 'create-user-button')

    expect(mapUserToServerMock).toHaveBeenCalledWith(expect.objectContaining({
      securityName: 'new-user',
      securityLevel: expect.any(Number)
    }))
    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)
    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(expect.objectContaining({
      snmpv3User: expect.arrayContaining([
        expect.objectContaining({ securityName: 'new-user' })
      ])
    }))
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
    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(expect.objectContaining({
      snmpv3User: expect.arrayContaining([
        expect.objectContaining({ securityName: 'existing-user' })
      ])
    }))
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
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Unable to determine the selected SNMPv3 user to update.', error: true })
  })

  it('requires auth protocol and auth passphrase for auth-only security level', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'auth-only-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[1])
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Please fix validation errors before saving.', error: true })
  })

  it('requires privacy protocol and privacy passphrase for auth-priv security level', async () => {
    const wrapper = mountComponent()

    await setInputValue(wrapper, 'security-name-input', 'auth-priv-user')
    await setBindingValue(wrapper, 'securityLevel', SECURITY_LEVEL_OPTIONS[2])
    await setBindingValue(wrapper, 'authProtocol', AUTH_PROTOCOL_OPTIONS[0])
    await setBindingValue(wrapper, 'authPassphrase', 'auth-secret')
    await clickButton(wrapper, 'create-user-button')

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Please fix validation errors before saving.', error: true })
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
      () => new Promise<void>((resolve) => {
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

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Please fix validation errors before saving.', error: true })
    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
  })
})
