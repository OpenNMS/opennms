import SnmpDataCollectionChangeStatusDialog from '@/components/SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, describe, expect, it } from 'vitest'

// Stub the Common ConfirmationDialog wrapper so these tests exercise this
// component's logic via the wrapper's public API (props + ok/cancel events).
const ConfirmationDialogStub = {
  name: 'ConfirmationDialog',
  template: '<div class="confirmation-dialog"><div class="dialog-content"><slot name="content"></slot></div><button class="action-btn" @click="$emit(\'ok\')">{{ actionButtonText }}</button><button class="cancel-btn" @click="$emit(\'cancel\')">{{ cancelButtonText || \'Cancel\' }}</button></div>',
  props: ['visible', 'title', 'actionButtonText', 'cancelButtonText']
}

describe('SnmpDataCollectionChangeStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>

  const defaultProps = {
    status: 'Enable' as const,
    visible: true,
    selected: { id: 1, name: 'Test Item', enabled: true },
    type: 'source' as const
  }

  const mountComponent = (props = {}) => {
    return mount(SnmpDataCollectionChangeStatusDialog, {
      props: { ...defaultProps, ...props },
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

  describe('Initial Rendering', () => {
    it('renders the ConfirmationDialog', () => {
      wrapper = mountComponent()
      expect(confirmationDialog().exists()).toBe(true)
    })

    it('passes the visible prop through', async () => {
      wrapper = mountComponent({ visible: false })
      expect(confirmationDialog().props('visible')).toBe(false)
      await wrapper.setProps({ visible: true })
      expect(confirmationDialog().props('visible')).toBe(true)
    })

    it('uses "Save" as the action button text', () => {
      wrapper = mountComponent()
      expect(confirmationDialog().props('actionButtonText')).toBe('Save')
    })
  })

  describe('Dialog Title', () => {
    it.each([
      { type: 'source', status: 'Enable', expectedTitle: 'Enable SNMP Data Collection Source' },
      { type: 'source', status: 'Disable', expectedTitle: 'Disable SNMP Data Collection Source' },
      { type: 'mib-group', status: 'Enable', expectedTitle: 'Enable MIB Group' },
      { type: 'mib-group', status: 'Disable', expectedTitle: 'Disable MIB Group' },
      { type: 'system-def', status: 'Enable', expectedTitle: 'Enable System Definition' },
      { type: 'system-def', status: 'Disable', expectedTitle: 'Disable System Definition' },
      { type: 'resource-type', status: 'Enable', expectedTitle: 'Enable Resource Type' },
      { type: 'resource-type', status: 'Disable', expectedTitle: 'Disable Resource Type' }
    ] as const)('displays "$expectedTitle" for type=$type status=$status', async ({ type, status, expectedTitle }) => {
      wrapper = mountComponent({ type, status })
      expect(confirmationDialog().props('title')).toBe(expectedTitle)
    })

    it('updates title when status changes', async () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable' })
      expect(confirmationDialog().props('title')).toBe('Enable SNMP Data Collection Source')
      await wrapper.setProps({ status: 'Disable' })
      expect(confirmationDialog().props('title')).toBe('Disable SNMP Data Collection Source')
    })

    it('updates title when type changes', async () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable' })
      expect(confirmationDialog().props('title')).toBe('Enable SNMP Data Collection Source')
      await wrapper.setProps({ type: 'mib-group' })
      expect(confirmationDialog().props('title')).toBe('Enable MIB Group')
    })
  })

  describe('Content Rendering', () => {
    it('renders source-specific content with cascade note', async () => {
      wrapper = mountComponent({
        type: 'source',
        status: 'Enable',
        selected: { id: 1, name: 'Test Source', enabled: false }
      })
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain('SNMP Data Collection Source')
      expect(text).toContain('Test Source')
      expect(text).toContain('Note:')
      expect(text).toContain('MIB Groups')
      expect(text).toContain('System Definitions')
      expect(text).toContain('Resource Types')
      expect(text).toContain('This action can not be undone.')
      expect(text).toContain('Are you sure you want to proceed?')
    })

    it('uses lowercase status in the message', async () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable', selected: { id: 1, name: 'S', enabled: false }})
      await flushPromises()
      expect(wrapper.text()).toContain('enable')
      await wrapper.setProps({ status: 'Disable' })
      expect(wrapper.text()).toContain('disable')
    })

    it.each([
      ['mib-group', 'MIB Group'],
      ['system-def', 'System Definition'],
      ['resource-type', 'Resource Type']
    ] as const)('renders %s content without source-specific note', async (type, label) => {
      wrapper = mountComponent({ type, selected: { id: 2, name: 'Item', enabled: true }})
      await flushPromises()
      const text = wrapper.text()
      expect(text).toContain(label)
      expect(text).toContain('Item')
      expect(text).not.toContain('Note:')
    })

    it('shows item name in bold', async () => {
      wrapper = mountComponent({ selected: { id: 1, name: 'Bold Name', enabled: true }})
      await flushPromises()
      expect(wrapper.find('.dialog-content strong').text()).toContain('Bold Name')
    })

    it('does not render content body when selected is null', async () => {
      wrapper = mountComponent({ selected: null })
      await flushPromises()
      expect(wrapper.text()).not.toContain('This will')
    })

    it('does not render content body when selected.id is 0', async () => {
      wrapper = mountComponent({ selected: { id: 0, name: 'Zero', enabled: true }})
      await flushPromises()
      expect(wrapper.text()).not.toContain('This will')
    })
  })

  describe('Event Emissions', () => {
    it('emits close when Cancel (cancel) is triggered, without confirm', async () => {
      wrapper = mountComponent()
      await wrapper.find('.cancel-btn').trigger('click')
      expect(wrapper.emitted('close')).toHaveLength(1)
      expect(wrapper.emitted('confirm')).toBeFalsy()
    })

    it('emits confirm with selected and type when Save (ok) is triggered, without close', async () => {
      const selected = { id: 1, name: 'Test Item', enabled: true }
      wrapper = mountComponent({ selected, type: 'source' })
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'source'])
      expect(wrapper.emitted('close')).toBeFalsy()
    })

    it('emits confirm with null when selected is null', async () => {
      wrapper = mountComponent({ selected: null, type: 'mib-group' })
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'mib-group'])
    })

    it.each([
      ['source'],
      ['mib-group'],
      ['system-def'],
      ['resource-type']
    ] as const)('emits correct type "%s" in confirm', async (type) => {
      const selected = { id: 1, name: 'Test', enabled: true }
      wrapper = mountComponent({ selected, type })
      await wrapper.find('.action-btn').trigger('click')
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, type])
    })
  })
})
