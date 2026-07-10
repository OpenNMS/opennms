import DeleteConfirmationDialog from '@/components/SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, describe, expect, it } from 'vitest'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events),
// independent of the underlying dialog library.
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="dialog-content"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('DeleteConfirmationDialog', () => {
  let wrapper: VueWrapper<any>

  const createWrapper = (props: any = {}) => {
    return mount(DeleteConfirmationDialog, {
      props: {
        visible: true,
        selected: { id: 1, name: 'Test Item' },
        type: 'resource-type',
        ...props
      },
      global: {
        stubs: { ConfirmationDialog: ConfirmationDialogStub }
      }
    })
  }

  const confirmationDialog = () => wrapper.findComponent({ name: 'ConfirmationDialog' })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Component Rendering', () => {
    it('renders the ConfirmationDialog', async () => {
      wrapper = createWrapper()
      await flushPromises()
      expect(confirmationDialog().exists()).toBe(true)
    })

    it('passes the visible prop through to ConfirmationDialog', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()
      expect(confirmationDialog().props('visible')).toBe(true)
    })

    it('uses "Delete" as the action button text', async () => {
      wrapper = createWrapper()
      await flushPromises()
      expect(confirmationDialog().props('actionButtonText')).toBe('Delete')
    })

    it('does not render content body when selected is null', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()
      expect(wrapper.text()).not.toContain('This will delete')
    })

    it('does not render content body when selected id is missing', async () => {
      wrapper = createWrapper({ selected: { name: 'Test' }})
      await flushPromises()
      expect(wrapper.text()).not.toContain('This will delete')
    })

    it('does not render content body when selected name is missing', async () => {
      wrapper = createWrapper({ selected: { id: 1 }})
      await flushPromises()
      expect(wrapper.text()).not.toContain('This will delete')
    })

    it('renders content body when selected has both id and name', async () => {
      wrapper = createWrapper()
      await flushPromises()
      expect(wrapper.text()).toContain('This will delete')
    })
  })

  describe('Dialog Titles', () => {
    it.each([
      ['source', 'Delete SNMP Data Collection Source'],
      ['mib-group', 'Delete MIB Group'],
      ['system-def', 'Delete System Definition'],
      ['resource-type', 'Delete Resource Type']
    ] as const)('displays correct title for %s', async (type, expected) => {
      wrapper = createWrapper({ type })
      await flushPromises()
      expect(confirmationDialog().props('title')).toBe(expected)
    })

    it('updates title when type changes', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()
      expect(confirmationDialog().props('title')).toBe('Delete SNMP Data Collection Source')

      await wrapper.setProps({ type: 'mib-group' })
      await flushPromises()
      expect(confirmationDialog().props('title')).toBe('Delete MIB Group')
    })
  })

  describe('Dialog Content', () => {
    it('displays correct content for resource-type', async () => {
      wrapper = createWrapper({ type: 'resource-type', selected: { id: 5, name: 'CPU Resource' }})
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain('This will delete the Resource Type:')
      expect(text).toContain('CPU Resource')
      expect(text).toContain('This action can not be undone.')
      expect(text).toContain('Are you sure you want to proceed?')
    })

    it('displays correct content for source including cascade note', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 3, name: 'Cisco Source' }})
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain('This will delete the SNMP Data Collection Source:')
      expect(text).toContain('Cisco Source')
      expect(text).toContain('Note:')
      expect(text).toContain('MIB Groups')
      expect(text).toContain('System Definitions')
      expect(text).toContain('Resource Types')
    })

    it('displays correct content for mib-group', async () => {
      wrapper = createWrapper({ type: 'mib-group', selected: { id: 7, name: 'System MIB' }})
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain('This will delete the MIB Group:')
      expect(text).toContain('System MIB')
      expect(text).not.toContain('Note:')
    })

    it('displays correct content for system-def', async () => {
      wrapper = createWrapper({ type: 'system-def', selected: { id: 2, name: 'Router Definition' }})
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain('This will delete the System Definition:')
      expect(text).toContain('Router Definition')
      expect(text).not.toContain('Note:')
    })

    it('displays selected name in bold', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'Test Name' }})
      await flushPromises()
      const strong = wrapper.find('strong')
      expect(strong.exists()).toBe(true)
      expect(strong.text()).toBe('Test Name')
    })

    it('handles special characters and unicode in name', async () => {
      const specialName = 'Test<>Name&"Special\'测试🚀'
      wrapper = createWrapper({ selected: { id: 1, name: specialName }})
      await flushPromises()
      expect(wrapper.text()).toContain(specialName)
    })
  })

  describe('Event Emissions', () => {
    it('emits close when Cancel (cancel) is triggered', async () => {
      wrapper = createWrapper()
      await flushPromises()
      await wrapper.find('.cancel-btn').trigger('click')
      expect(wrapper.emitted('close')).toBeTruthy()
      expect(wrapper.emitted('close')).toHaveLength(1)
    })

    it('emits confirm with selected and type when Delete (ok) is triggered', async () => {
      const selected = { id: 5, name: 'Test Resource' }
      wrapper = createWrapper({ type: 'resource-type', selected })
      await flushPromises()
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'resource-type'])
    })

    it('emits confirm with null when selected is null', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'resource-type'])
    })

    it.each([
      ['source'],
      ['mib-group'],
      ['system-def']
    ] as const)('emits confirm with correct type for %s', async (type) => {
      const selected = { id: 2, name: 'Item' }
      wrapper = createWrapper({ type, selected })
      await flushPromises()
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, type])
    })

    it('emits correct payload after selected and type change', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'First' }})
      await flushPromises()
      await wrapper.setProps({ type: 'mib-group', selected: { id: 2, name: 'Second' }})
      await flushPromises()
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([{ id: 2, name: 'Second' }, 'mib-group'])
    })
  })
})
