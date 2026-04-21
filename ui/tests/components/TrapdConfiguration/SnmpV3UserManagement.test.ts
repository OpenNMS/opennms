import SnmpV3UserManagement from '@/components/TrapdConfiguration/SnmpV3UserManagement.vue'
import { updateTrapdConfiguration } from '@/services/trapdConfigurationService'
import { useTrapdConfigStore } from '@/stores/trapdConfigStore'
import { CreateEditMode } from '@/types'
import type { SnmpV3User } from '@/types/trapConfig'
import { FeatherSortHeader, SORT } from '@featherds/table'
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

vi.mock('@/services/trapdConfigurationService', () => ({
  updateTrapdConfiguration: vi.fn()
}))

const FeatherButtonStub = defineComponent({
  name: 'FeatherButton',
  props: {
    dataTest: {
      type: String,
      default: ''
    },
    disabled: {
      type: Boolean,
      default: false
    },
    icon: {
      type: String,
      default: ''
    }
  },
  emits: ['click'],
  template:
    '<button :data-test="dataTest" :disabled="disabled" :aria-label="icon" @click="$emit(\'click\')"><slot /></button>'
})

const FeatherSortHeaderStub = defineComponent({
  name: 'FeatherSortHeader',
  props: {
    property: {
      type: String,
      default: ''
    },
    sort: {
      type: String,
      default: ''
    }
  },
  emits: ['sort-changed'],
  template: '<th><button :data-test="`sort-${property}`"><slot /></button></th>'
})

const DeleteDialogStub = defineComponent({
  name: 'DeleteUserConfirmationDialog',
  props: {
    visible: {
      type: Boolean,
      default: false
    }
  },
  emits: ['close', 'confirm'],
  template: `
    <div>
      <div data-test="delete-dialog-visible">{{ String(visible) }}</div>
      <button data-test="close-delete-dialog" @click="$emit('close')">close</button>
      <button data-test="confirm-delete-dialog" @click="$emit('confirm')">confirm</button>
    </div>
  `
})

describe('SnmpV3UserManagement.vue', () => {
  let store: ReturnType<typeof useTrapdConfigStore>
  const updateTrapdConfigurationMock = vi.mocked(updateTrapdConfiguration)

  const users: SnmpV3User[] = [
    {
      engineId: null,
      securityName: 'user-one',
      securityLevel: 1,
      authProtocol: null,
      authPassphrase: null,
      privacyProtocol: null,
      privacyPassphrase: null
    },
    {
      engineId: null,
      securityName: 'user-two',
      securityLevel: 3,
      authProtocol: 'SHA-256',
      authPassphrase: 'masked-a',
      privacyProtocol: 'AES256',
      privacyPassphrase: 'masked-b'
    }
  ]

  const mountComponent = () => {
    return mount(SnmpV3UserManagement, {
      global: {
        stubs: {
          TableCard: {
            template: '<div><slot /></div>'
          },
          EmptyList: {
            props: ['content'],
            template: '<div data-test="empty-list">{{ content.msg }}</div>'
          },
          DeleteUserConfirmationDialog: DeleteDialogStub,
          FeatherButton: FeatherButtonStub,
          'feather-button': FeatherButtonStub,
          FeatherSortHeader: FeatherSortHeaderStub,
          'feather-sort-header': FeatherSortHeaderStub,
          FeatherIcon: true,
          'feather-icon': true,
          TransitionGroup: {
            template: '<tbody><slot /></tbody>'
          }
        }
      }
    })
  }

  const clickByDataTest = async (wrapper: ReturnType<typeof mountComponent>, dataTest: string, index = 0) => {
    const elements = wrapper.findAll(`[data-test="${dataTest}"]`)
    expect(elements[index]?.exists()).toBe(true)
    await elements[index].trigger('click')
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
    store.createUserDrawerState.visible = false
    store.snmpV3Users = [...users]
    store.trapdConfig = {
      snmpTrapAddress: '127.0.0.1',
      snmpTrapPort: 162,
      newSuspectOnTrap: false,
      includeRawMessage: false,
      threads: 0,
      queueSize: 10000,
      batchSize: 1000,
      batchInterval: 500,
      useAddressFromVarbind: false,
      snmpv3User: [...users]
    }
    store.fetchTrapConfig = vi.fn().mockResolvedValue(undefined)
    store.openCreateUserDrawer = vi.fn()

    updateTrapdConfigurationMock.mockResolvedValue(undefined)
  })

  it('does not render when the create user drawer is visible', () => {
    store.createUserDrawerState.visible = true
    const wrapper = mountComponent()

    expect(wrapper.find('[data-test="snmpv3-user-management"]').exists()).toBe(false)
  })

  it('renders the heading, add button, column headers, and user rows', () => {
    const wrapper = mountComponent()

    expect(wrapper.text()).toContain('SNMPv3 User Management')
    expect(wrapper.text()).toContain('List SNMPv3 users credentials')
    expect(wrapper.find('[data-test="add-user-button"]').exists()).toBe(true)
    expect(wrapper.text()).toContain('SnmpV3 Username')
    expect(wrapper.text()).toContain('Security Level')
    expect(wrapper.text()).toContain('Authentication Protocol')
    expect(wrapper.text()).toContain('Privacy Protocol')
    expect(wrapper.text()).toContain('Action')
    expect(wrapper.text()).toContain('user-one')
    expect(wrapper.text()).toContain('user-two')
    expect((wrapper.vm as any).tableRecords).toEqual(users)
  })

  it('renders the empty state when there are no users', async () => {
    store.snmpV3Users = []
    const wrapper = mountComponent()
    await nextTick()

    expect(wrapper.find('[data-test="empty-list"]').text()).toBe('No SNMPv3 users found')
    expect(wrapper.findAll('tbody tr')).toHaveLength(0)
  })

  it('opens create user drawer in create mode from the add user button', async () => {
    const wrapper = mountComponent()

    await clickByDataTest(wrapper, 'add-user-button')

    expect(store.openCreateUserDrawer).toHaveBeenCalledWith(CreateEditMode.Create, -1)
  })

  it('opens create user drawer in edit mode for the selected row', async () => {
    const wrapper = mountComponent()

    await clickByDataTest(wrapper, 'edit-user-button', 1)

    expect(store.openCreateUserDrawer).toHaveBeenCalledWith(CreateEditMode.Edit, 1)
  })

  it('opens the delete dialog with the selected index', async () => {
    const wrapper = mountComponent()

    await clickByDataTest(wrapper, 'delete-user-button', 1)

    expect((wrapper.vm as any).deleteUserIndex).toBe(1)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(true)
    expect(wrapper.find('[data-test="delete-dialog-visible"]').text()).toBe('true')
  })

  it('cancels delete and resets dialog state', async () => {
    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(0)
    await nextTick()

    await clickByDataTest(wrapper, 'close-delete-dialog')

    expect((wrapper.vm as any).deleteUserIndex).toBe(null)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(false)
  })

  it('deletes the selected user successfully, refreshes config, and closes the dialog', async () => {
    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(0)
    await nextTick()

    await clickByDataTest(wrapper, 'confirm-delete-dialog')

    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)
    expect(updateTrapdConfigurationMock).toHaveBeenCalledWith(expect.objectContaining({
      snmpv3User: [expect.objectContaining({ securityName: 'user-two' })]
    }))
    expect(store.fetchTrapConfig).toHaveBeenCalledTimes(1)
    expect((wrapper.vm as any).deleteUserIndex).toBe(null)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(false)
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'SNMPv3 user deleted successfully.' })
    expect((wrapper.vm as any).isDeleting).toBe(false)
  })

  it('shows the service error when delete fails with an Error', async () => {
    updateTrapdConfigurationMock.mockRejectedValue(new Error('delete failed'))
    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(0)
    await nextTick()

    await clickByDataTest(wrapper, 'confirm-delete-dialog')

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'delete failed', error: true })
    expect(store.fetchTrapConfig).not.toHaveBeenCalled()
    expect((wrapper.vm as any).deleteUserIndex).toBe(0)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(true)
    expect((wrapper.vm as any).isDeleting).toBe(false)
  })

  it('shows a generic error when delete fails with a non-Error value', async () => {
    updateTrapdConfigurationMock.mockRejectedValue('boom')
    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(0)
    await nextTick()

    await clickByDataTest(wrapper, 'confirm-delete-dialog')

    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'Failed to delete SNMPv3 user.', error: true })
  })

  it('does not call delete when no selected index exists', async () => {
    const wrapper = mountComponent()

    await (wrapper.vm as any).confirmDeleteUser()
    await flushPromises()

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect((wrapper.vm as any).deleteUserIndex).toBe(null)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(false)
    expect(showSnackBarMock).not.toHaveBeenCalled()
  })

  it('does not call delete again while a delete request is already in progress', async () => {
    let resolveDelete: (() => void) | undefined
    updateTrapdConfigurationMock.mockImplementation(
      () =>
        new Promise<void>((resolve) => {
          resolveDelete = resolve
        })
    )

    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(0)
    await nextTick()

    const pendingDelete = (wrapper.vm as any).confirmDeleteUser()
    await nextTick()
    await (wrapper.vm as any).confirmDeleteUser()

    expect(updateTrapdConfigurationMock).toHaveBeenCalledTimes(1)
    expect((wrapper.vm as any).isDeleting).toBe(true)

    resolveDelete?.()
    await pendingDelete
    await flushPromises()

    expect((wrapper.vm as any).isDeleting).toBe(false)
  })

  it('updates tableRecords when the store users list changes', async () => {
    const wrapper = mountComponent()
    const nextUsers: SnmpV3User[] = [
      {
        engineId: null,
        securityName: 'replacement-user',
        securityLevel: 2,
        authProtocol: 'MD5',
        authPassphrase: 'masked',
        privacyProtocol: null,
        privacyPassphrase: null
      }
    ]

    store.snmpV3Users = nextUsers
    await nextTick()
    await nextTick()

    expect((wrapper.vm as any).tableRecords).toEqual(nextUsers)
    expect(wrapper.text()).toContain('replacement-user')
    expect(wrapper.text()).not.toContain('user-one')
  })

  it('falls back to an empty records list when store users become undefined', async () => {
    const wrapper = mountComponent()

    ;(store as any).snmpV3Users = undefined
    await nextTick()
    await nextTick()

    expect((wrapper.vm as any).tableRecords).toEqual([])
  })

  it('shows user-not-found error and closes dialog when selected index is out of range', async () => {
    const wrapper = mountComponent()
    ;(wrapper.vm as any).openDeleteUserDialog(99)
    await nextTick()

    await (wrapper.vm as any).confirmDeleteUser()
    await flushPromises()

    expect(updateTrapdConfigurationMock).not.toHaveBeenCalled()
    expect(showSnackBarMock).toHaveBeenCalledWith({ msg: 'SNMPv3 user not found.', error: true })
    expect((wrapper.vm as any).deleteUserIndex).toBe(null)
    expect((wrapper.vm as any).deleteDialogVisible).toBe(false)
  })

  it('updates sort state when a sort header emits sort-changed', async () => {
    const wrapper = mountComponent()
    const sortHeaders = wrapper.findAllComponents(FeatherSortHeader)

    expect(sortHeaders).toHaveLength(4)
    await sortHeaders[1].vm.$emit('sort-changed', {
      property: 'securityLevel',
      value: SORT.ASCENDING
    })
    await nextTick()

    expect((wrapper.vm as any).sort.username).toBe(SORT.NONE)
    expect((wrapper.vm as any).sort.securityLevel).toBe(SORT.ASCENDING)
    expect((wrapper.vm as any).sort.authenticationProtocol).toBe(SORT.NONE)
    expect((wrapper.vm as any).sort.privacyProtocol).toBe(SORT.NONE)
  })

  it('sortChanged resets previous sort values before applying the next property', () => {
    const wrapper = mountComponent()

    ;(wrapper.vm as any).sort.username = SORT.DESCENDING
    ;(wrapper.vm as any).sort.securityLevel = SORT.ASCENDING
    ;(wrapper.vm as any).sortChanged({
      property: 'privacyProtocol',
      value: SORT.ASCENDING
    })

    expect((wrapper.vm as any).sort.username).toBe(SORT.NONE)
    expect((wrapper.vm as any).sort.securityLevel).toBe(SORT.NONE)
    expect((wrapper.vm as any).sort.authenticationProtocol).toBe(SORT.NONE)
    expect((wrapper.vm as any).sort.privacyProtocol).toBe(SORT.ASCENDING)
  })
})
