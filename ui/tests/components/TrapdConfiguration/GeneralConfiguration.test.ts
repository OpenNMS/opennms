import GeneralConfiguration from '@/components/TrapdConfiguration/GeneralConfiguration.vue'
import { MAX_PORT, MIN_PORT } from '@/lib/trapdValidator'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import type { TrapConfig } from '@/types/trapConfig'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import { cloneDeep } from 'lodash'
import { setActivePinia } from 'pinia'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const { showSnackBarMock } = vi.hoisted(() => ({
  showSnackBarMock: vi.fn()
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: showSnackBarMock
  })
}))

vi.mock('@/services/trapdConfigurationService', () => ({
  updateTrapdConfiguration: vi.fn()
}))

describe('GeneralConfiguration.vue', () => {
  let store: ReturnType<typeof useTrapdConfigStore>
  const updateTrapdConfigurationMock = vi.mocked(updateTrapdConfiguration)

  const baseTrapConfig: TrapConfig = {
    snmpTrapAddress: '192.168.1.10',
    snmpTrapPort: 162,
    newSuspectOnTrap: true,
    includeRawMessage: false,
    threads: 4,
    queueSize: 5000,
    batchSize: 250,
    batchInterval: 750,
    useAddressFromVarbind: true,
    snmpv3User: []
  }

  const mountComponent = () => {
    return mount(GeneralConfiguration, {
      global: {
        stubs: {
          TableCard: {
            template: '<div><slot /></div>'
          },
          FeatherExpansionPanel: {
            props: ['title'],
            template: '<div><slot /></div>'
          },
          FeatherInput: true,
          'feather-input': true,
          FeatherButton: true,
          'feather-button': true,
          SwitchRender: true,
          'switch-render': true
        }
      }
    })
  }

  const setBindingValue = async (
    wrapper: ReturnType<typeof mountComponent>,
    key: string,
    value: string | number | boolean
  ) => {
    ;(wrapper.vm as any)[key] = value
    await nextTick()
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
    store.trapdConfig = cloneDeep(baseTrapConfig)
    store.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)

    updateTrapdConfigurationMock.mockResolvedValue(undefined)
  })

  it('renders the section labels and loads the current trap configuration values from the store', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('Trap Listener Settings')
    expect(wrapper.text()).toContain('Update Changes')
    expect((wrapper.vm as any).port).toBe(162)
    expect((wrapper.vm as any).bindAddress).toBe('192.168.1.10')
    expect((wrapper.vm as any).status).toBe(true)
    expect((wrapper.vm as any).trapMessageStatus).toBe(false)
    expect((wrapper.vm as any).trapSourceAddressStatus).toBe(true)
    expect((wrapper.vm as any).threads).toBe(4)
    expect((wrapper.vm as any).queueSize).toBe(5000)
    expect((wrapper.vm as any).batchSize).toBe(250)
    expect((wrapper.vm as any).batchInterval).toBe(750)
    expect((wrapper.vm as any).trapConfigError).toEqual({})
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('reloads form state when trapdConfig changes in the store', async () => {
    const wrapper = mountComponent()

    store.trapdConfig = {
      ...cloneDeep(baseTrapConfig),
      snmpTrapPort: 10162,
      snmpTrapAddress: '*',
      newSuspectOnTrap: false,
      includeRawMessage: true,
      useAddressFromVarbind: false,
      threads: 0,
      queueSize: 10000,
      batchSize: 1000,
      batchInterval: 500
    }
    await nextTick()
    await nextTick()

    expect((wrapper.vm as any).port).toBe(10162)
    expect((wrapper.vm as any).bindAddress).toBe('*')
    expect((wrapper.vm as any).status).toBe(false)
    expect((wrapper.vm as any).trapMessageStatus).toBe(true)
    expect((wrapper.vm as any).trapSourceAddressStatus).toBe(false)
    expect((wrapper.vm as any).threads).toBe(0)
    expect((wrapper.vm as any).queueSize).toBe(10000)
    expect((wrapper.vm as any).batchSize).toBe(1000)
    expect((wrapper.vm as any).batchInterval).toBe(500)
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('enables save when a valid field changes and disables it again when reverted', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 163)
    expect((wrapper.vm as any).isSaveDisabled).toBe(false)

    await setBindingValue(wrapper, 'port', 162)
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('toggles all switch values through the component handlers', () => {
    const wrapper = mountComponent()

    ;(wrapper.vm as any).onChangeStatus()
    ;(wrapper.vm as any).onChangeTrapMessageStatus()
    ;(wrapper.vm as any).onChangeTrapSourceAddressStatus()

    expect((wrapper.vm as any).status).toBe(false)
    expect((wrapper.vm as any).trapMessageStatus).toBe(true)
    expect((wrapper.vm as any).trapSourceAddressStatus).toBe(false)
  })

  it('submits the updated payload successfully and refreshes the store', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', '10162')
    await setBindingValue(wrapper, 'bindAddress', '*')
    await setBindingValue(wrapper, 'status', false)
    await setBindingValue(wrapper, 'trapMessageStatus', true)
    await setBindingValue(wrapper, 'trapSourceAddressStatus', false)
    await setBindingValue(wrapper, 'threads', '2')
    await setBindingValue(wrapper, 'queueSize', '6000')
    await setBindingValue(wrapper, 'batchSize', '300')
    await setBindingValue(wrapper, 'batchInterval', '900')

    await (wrapper.vm as any).updateConfig()
    await flushPromises()

    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith({
      snmpTrapPort: 10162,
      snmpTrapAddress: '*',
      newSuspectOnTrap: false,
      useAddressFromVarbind: false,
      includeRawMessage: true,
      threads: 2,
      queueSize: 6000,
      batchSize: 300,
      batchInterval: 900,
      snmpv3User: []
    })
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Trap configuration updated successfully.' })
    expect(store.fetchTrapConfig).toHaveBeenCalledTimes(1)
    expect((wrapper.vm as any).isSaving).toBe(false)
  })

  it('shows the service error message when update fails with an Error', async () => {
    updateTrapdConfigurationMock.mockRejectedValue(new Error('update failed'))
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 163)
    await (wrapper.vm as any).updateConfig()
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'update failed', error: true })
    expect(store.fetchTrapConfig).not.toHaveBeenCalled()
    expect((wrapper.vm as any).isSaving).toBe(false)
  })

  it('shows a generic error message when update fails with a non-Error value', async () => {
    updateTrapdConfigurationMock.mockRejectedValue('boom')
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 163)
    await (wrapper.vm as any).updateConfig()
    await flushPromises()

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to update trap configuration.', error: true })
    expect(store.fetchTrapConfig).not.toHaveBeenCalled()
    expect((wrapper.vm as any).isSaving).toBe(false)
  })

  it('includes store snmpV3Users in the update payload', async () => {
    store.snmpV3Users = [{
      securityName: 'sec-user-1',
      securityLevel: 1,
      authProtocol: null,
      authPassphrase: null,
      privacyProtocol: null,
      privacyPassphrase: null,
      engineId: null
    }]
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 163)
    await (wrapper.vm as any).updateConfig()
    await flushPromises()

    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(expect.objectContaining({
      snmpv3User: [
        expect.objectContaining({ securityName: 'sec-user-1' })
      ]
    }))
  })

  it('sets isSaving during an in-flight request and clears it after completion', async () => {
    let resolveRequest: (() => void) | undefined
    updateTrapdConfigurationMock.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveRequest = resolve
        })
    )

    const wrapper = mountComponent()
    await setBindingValue(wrapper, 'port', 163)

    const pendingSave = (wrapper.vm as any).updateConfig()
    await nextTick()

    expect((wrapper.vm as any).isSaving).toBe(true)

    resolveRequest?.()
    await pendingSave
    await flushPromises()

    expect((wrapper.vm as any).isSaving).toBe(false)
  })

  it(`shows a validation error when port is less than ${MIN_PORT}`, async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 0)

    expect((wrapper.vm as any).trapConfigError.port).toBe(`Port must be between ${MIN_PORT} and ${MAX_PORT}.`)
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it(`shows a validation error when port is greater than ${MAX_PORT}`, async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'port', 65536)

    expect((wrapper.vm as any).trapConfigError.port).toBe(`Port must be between ${MIN_PORT} and ${MAX_PORT}.`)
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when bind address is empty', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'bindAddress', '')

    expect((wrapper.vm as any).trapConfigError.bindAddress).toBe('Bind Address cannot be empty.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when bind address is not * or a valid IPv4 address', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'bindAddress', 'localhost')

    expect((wrapper.vm as any).trapConfigError.bindAddress).toBe('Bind Address must be * or a valid IP address.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('accepts wildcard bind address as a valid value', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'bindAddress', '*')

    expect((wrapper.vm as any).trapConfigError.bindAddress).toBeUndefined()
    expect((wrapper.vm as any).isSaveDisabled).toBe(false)
  })

  it('shows a validation error when threads is negative', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'threads', -1)

    expect((wrapper.vm as any).trapConfigError.threads).toBe('Threads cannot be negative.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when queue size is negative', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'queueSize', -1)

    expect((wrapper.vm as any).trapConfigError.queueSize).toBe('Queue Size must be greater than 0.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when queue size is zero', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'queueSize', 0)

    expect((wrapper.vm as any).trapConfigError.queueSize).toBe('Queue Size must be greater than 0.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when batch size is negative', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'batchSize', -1)

    expect((wrapper.vm as any).trapConfigError.batchSize).toBe('Batch Size must be greater than 0.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when batch size is zero', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'batchSize', 0)

    expect((wrapper.vm as any).trapConfigError.batchSize).toBe('Batch Size must be greater than 0.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('shows a validation error when batch interval is negative', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'batchInterval', -1)

    expect((wrapper.vm as any).trapConfigError.batchInterval).toBe('Batch Interval cannot be negative.')
    expect((wrapper.vm as any).isSaveDisabled).toBe(true)
  })

  it('does not call the update service when the form is invalid', async () => {
    const wrapper = mountComponent()

    await setBindingValue(wrapper, 'bindAddress', '')
    await (wrapper.vm as any).updateConfig()
    await flushPromises()

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })
})