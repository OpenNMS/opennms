import SnmpDataCollectionChangeStatusDialog from '@/components/SnmpDataCollection/Dialog/SnmpDataCollectionChangeStatusDialog.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

describe('SnmpDataCollectionChangeStatusDialog.vue', () => {
  let wrapper: VueWrapper<any>

  const defaultProps = {
    status: 'Enable' as const,
    visible: false,
    selected: { id: 1, name: 'Test Item', enabled: true },
    type: 'source' as const
  }

  const mountComponent = (props = {}) => {
    return mount(SnmpDataCollectionChangeStatusDialog, {
      props: { ...defaultProps, ...props },
      global: {
        components: {
          FeatherButton,
          FeatherDialog
        },
        stubs: {
          FeatherDialog: {
            template: `
              <div class="feather-dialog-stub" v-if="modelValue">
                <div class="dialog-title">{{ labels?.title }}</div>
                <slot></slot>
                <div class="dialog-footer"><slot name="footer"></slot></div>
              </div>
            `,
            props: ['modelValue', 'labels', 'hideClose'],
            emits: ['update:modelValue', 'hidden']
          }
        }
      }
    })
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Initial Rendering', () => {
    it('renders the component', () => {
      wrapper = mountComponent()
      expect(wrapper.exists()).toBe(true)
    })

    it('renders with change-status-dialog-modal class', () => {
      wrapper = mountComponent()
      expect(wrapper.find('.change-status-dialog-modal').exists()).toBe(true)
    })

    it('dialog is hidden when visible prop is false', () => {
      wrapper = mountComponent({ visible: false })
      expect(wrapper.find('.feather-dialog-stub').exists()).toBe(false)
    })

    it('dialog is shown when visible prop is true', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.find('.feather-dialog-stub').exists()).toBe(true)
    })

    it('renders Cancel button', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      const buttons = wrapper.findAllComponents(FeatherButton)
      const cancelButton = buttons.find((b) => b.text() === 'Cancel')
      expect(cancelButton).toBeDefined()
    })

    it('renders Save button', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      const buttons = wrapper.findAllComponents(FeatherButton)
      const saveButton = buttons.find((b) => b.text() === 'Save')
      expect(saveButton).toBeDefined()
    })
  })

  describe('Dialog Visibility', () => {
    it('shows dialog when visible changes from false to true', async () => {
      wrapper = mountComponent({ visible: false })
      expect(wrapper.find('.feather-dialog-stub').exists()).toBe(false)

      await wrapper.setProps({ visible: true })
      await nextTick()
      expect(wrapper.find('.feather-dialog-stub').exists()).toBe(true)
    })

    it('internal isVisible state tracks visible prop', async () => {
      wrapper = mountComponent({ visible: false })
      expect(wrapper.vm.isVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('isVisible is set to true immediately when visible prop is true', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)
    })
  })

  describe('Dialog Title/Labels', () => {
    it.each([
      { type: 'source', status: 'Enable', expectedTitle: 'Enable SNMP Data Collection Source' },
      { type: 'source', status: 'Disable', expectedTitle: 'Disable SNMP Data Collection Source' },
      { type: 'mib-group', status: 'Enable', expectedTitle: 'Enable MIB Group' },
      { type: 'mib-group', status: 'Disable', expectedTitle: 'Disable MIB Group' },
      { type: 'system-def', status: 'Enable', expectedTitle: 'Enable System Definition' },
      { type: 'system-def', status: 'Disable', expectedTitle: 'Disable System Definition' },
      { type: 'resource-type', status: 'Enable', expectedTitle: 'Enable Resource Type' },
      { type: 'resource-type', status: 'Disable', expectedTitle: 'Disable Resource Type' }
    ] as const)(
      'displays correct title "$expectedTitle" for type=$type and status=$status',
      async ({ type, status, expectedTitle }) => {
        wrapper = mountComponent({ type, status, visible: true })
        await nextTick()

        expect(wrapper.vm.label.title).toBe(expectedTitle)
      }
    )

    it('label is computed correctly', () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable' })
      expect(wrapper.vm.label).toEqual({ title: 'Enable SNMP Data Collection Source' })
    })

    it('label updates when status prop changes', async () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable' })
      expect(wrapper.vm.label.title).toBe('Enable SNMP Data Collection Source')

      await wrapper.setProps({ status: 'Disable' })
      expect(wrapper.vm.label.title).toBe('Disable SNMP Data Collection Source')
    })

    it('label updates when type prop changes', async () => {
      wrapper = mountComponent({ type: 'source', status: 'Enable' })
      expect(wrapper.vm.label.title).toBe('Enable SNMP Data Collection Source')

      await wrapper.setProps({ type: 'mib-group' })
      expect(wrapper.vm.label.title).toBe('Enable MIB Group')
    })
  })

  describe('Content Rendering - Source Type', () => {
    beforeEach(async () => {
      wrapper = mountComponent({
        type: 'source',
        status: 'Enable',
        visible: true,
        selected: { id: 1, name: 'Test Source', enabled: false }
      })
      await nextTick()
    })

    it('displays source-specific content', () => {
      const text = wrapper.text()
      expect(text).toContain('SNMP Data Collection Source')
      expect(text).toContain('Test Source')
    })

    it('displays note about cascading effect', () => {
      const text = wrapper.text()
      expect(text).toContain('Note:')
      expect(text).toContain('MIB Groups')
      expect(text).toContain('System Definitions')
      expect(text).toContain('Resource Types')
    })

    it('displays action cannot be undone message', () => {
      expect(wrapper.text()).toContain('This action can not be undone.')
    })

    it('displays confirmation question', () => {
      expect(wrapper.text()).toContain('Are you sure you want to proceed?')
    })

    it('shows item name in bold', () => {
      const strong = wrapper.find('.modal-body strong')
      expect(strong.text()).toContain('Test Source')
    })

    it('uses lowercase status in message', async () => {
      await wrapper.setProps({ status: 'Enable' })
      expect(wrapper.text()).toContain('enable')

      await wrapper.setProps({ status: 'Disable' })
      expect(wrapper.text()).toContain('disable')
    })
  })

  describe('Content Rendering - MIB Group Type', () => {
    beforeEach(async () => {
      wrapper = mountComponent({
        type: 'mib-group',
        status: 'Disable',
        visible: true,
        selected: { id: 2, name: 'Test MIB Group', enabled: true }
      })
      await nextTick()
    })

    it('displays mib-group-specific content', () => {
      const text = wrapper.text()
      expect(text).toContain('MIB Group')
      expect(text).toContain('Test MIB Group')
    })

    it('does not display source-specific note', () => {
      const text = wrapper.text()
      expect(text).not.toContain('Note:')
      expect(text).not.toContain('all associated')
    })

    it('displays action cannot be undone message', () => {
      expect(wrapper.text()).toContain('This action can not be undone.')
    })

    it('displays confirmation question', () => {
      expect(wrapper.text()).toContain('Are you sure you want to proceed?')
    })
  })

  describe('Content Rendering - System Definition Type', () => {
    beforeEach(async () => {
      wrapper = mountComponent({
        type: 'system-def',
        status: 'Enable',
        visible: true,
        selected: { id: 3, name: 'Test System Def', enabled: false }
      })
      await nextTick()
    })

    it('displays system-def-specific content', () => {
      const text = wrapper.text()
      expect(text).toContain('System Definition')
      expect(text).toContain('Test System Def')
    })

    it('does not display source-specific note', () => {
      const text = wrapper.text()
      expect(text).not.toContain('Note:')
    })

    it('displays action cannot be undone message', () => {
      expect(wrapper.text()).toContain('This action can not be undone.')
    })
  })

  describe('Content Rendering - Resource Type', () => {
    beforeEach(async () => {
      wrapper = mountComponent({
        type: 'resource-type',
        status: 'Disable',
        visible: true,
        selected: { id: 4, name: 'Test Resource Type', enabled: true }
      })
      await nextTick()
    })

    it('displays resource-type-specific content', () => {
      const text = wrapper.text()
      expect(text).toContain('Resource Type')
      expect(text).toContain('Test Resource Type')
    })

    it('does not display source-specific note', () => {
      const text = wrapper.text()
      expect(text).not.toContain('Note:')
    })

    it('displays action cannot be undone message', () => {
      expect(wrapper.text()).toContain('This action can not be undone.')
    })
  })

  describe('Conditional Modal Body Rendering', () => {
    it('renders modal body when selected has id and name', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: 'Valid Item', enabled: true }
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('does not render modal body when selected is null', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: null
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(false)
    })

    it('does not render modal body when selected.id is missing', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { name: 'No ID Item', enabled: true } as any
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(false)
    })

    it('does not render modal body when selected.name is missing', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, enabled: true } as any
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(false)
    })

    it('does not render modal body when selected.id is 0', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 0, name: 'Zero ID', enabled: true }
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(false)
    })

    it('does not render modal body when selected.name is empty string', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: '', enabled: true }
      })
      await nextTick()
      expect(wrapper.find('.modal-body').exists()).toBe(false)
    })
  })

  describe('Cancel Button', () => {
    it('emits close event when Cancel button is clicked', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      expect(wrapper.emitted('close')).toBeTruthy()
      expect(wrapper.emitted('close')?.length).toBe(1)
    })

    it('sets isVisible to false when Cancel is clicked', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('does not emit confirm when Cancel is clicked', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      expect(wrapper.emitted('confirm')).toBeFalsy()
    })
  })

  describe('Save Button', () => {
    it('emits confirm event with selected and type when Save is clicked', async () => {
      const selected = { id: 1, name: 'Test Item', enabled: true }
      wrapper = mountComponent({
        visible: true,
        selected,
        type: 'source'
      })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'source'])
    })

    it('sets isVisible to false when Save is clicked', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('emits confirm with null selected when selected is null', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: null,
        type: 'mib-group'
      })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'mib-group'])
    })

    it.each([
      { type: 'source' },
      { type: 'mib-group' },
      { type: 'system-def' },
      { type: 'resource-type' }
    ] as const)('emits correct type "$type" in confirm event', async ({ type }) => {
      const selected = { id: 1, name: 'Test', enabled: true }
      wrapper = mountComponent({
        visible: true,
        selected,
        type
      })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, type])
    })
  })

  describe('Close Function', () => {
    it('sets isVisible to false', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      wrapper.vm.close()
      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('emits close event', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      wrapper.vm.close()
      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('can be called multiple times', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      wrapper.vm.close()
      wrapper.vm.close()
      wrapper.vm.close()

      expect(wrapper.emitted('close')?.length).toBe(3)
    })
  })

  describe('ChangeStatus Function', () => {
    it('sets isVisible to false', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      wrapper.vm.changeStatus()
      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('emits confirm with selected and type', async () => {
      const selected = { id: 5, name: 'Custom Item', enabled: false }
      wrapper = mountComponent({
        visible: true,
        selected,
        type: 'system-def'
      })
      await nextTick()

      wrapper.vm.changeStatus()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'system-def'])
    })
  })

  describe('Watch Behavior', () => {
    it('updates isVisible when visible prop changes to true', async () => {
      wrapper = mountComponent({ visible: false })
      expect(wrapper.vm.isVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('immediate option sets isVisible on mount when visible is true', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('watch only triggers when visible becomes true (not false)', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)

      // Close the dialog internally
      wrapper.vm.close()
      expect(wrapper.vm.isVisible).toBe(false)

      // Setting visible to false should not change isVisible
      await wrapper.setProps({ visible: false })
      expect(wrapper.vm.isVisible).toBe(false)
    })
  })

  describe('Props Validation', () => {
    it('accepts valid status prop values', () => {
      expect(() => mountComponent({ status: 'Enable' })).not.toThrow()
      expect(() => mountComponent({ status: 'Disable' })).not.toThrow()
    })

    it('accepts valid type prop values', () => {
      expect(() => mountComponent({ type: 'source' })).not.toThrow()
      expect(() => mountComponent({ type: 'mib-group' })).not.toThrow()
      expect(() => mountComponent({ type: 'system-def' })).not.toThrow()
      expect(() => mountComponent({ type: 'resource-type' })).not.toThrow()
    })

    it('accepts visible as boolean', () => {
      expect(() => mountComponent({ visible: true })).not.toThrow()
      expect(() => mountComponent({ visible: false })).not.toThrow()
    })

    it('accepts selected with proper structure', () => {
      expect(() =>
        mountComponent({ selected: { id: 1, name: 'Test', enabled: true } })
      ).not.toThrow()
    })

    it('accepts null selected', () => {
      expect(() => mountComponent({ selected: null })).not.toThrow()
    })
  })

  describe('Edge Cases', () => {
    it('handles special characters in name', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: 'Test<>Item&"Special\'Chars', enabled: true }
      })
      await nextTick()

      expect(wrapper.text()).toContain('Test<>Item&"Special\'Chars')
    })

    it('handles very long name', async () => {
      const longName = 'A'.repeat(500)
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: longName, enabled: true }
      })
      await nextTick()

      expect(wrapper.text()).toContain(longName)
    })

    it('handles unicode characters in name', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: '测试项目 🎉', enabled: true }
      })
      await nextTick()

      expect(wrapper.text()).toContain('测试项目 🎉')
    })

    it('handles whitespace-only name', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: '   ', enabled: true }
      })
      await nextTick()

      // Whitespace name should render modal body
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('handles negative id value', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: -1, name: 'Negative ID', enabled: true }
      })
      await nextTick()

      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('handles very large id value', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: Number.MAX_SAFE_INTEGER, name: 'Large ID', enabled: true }
      })
      await nextTick()

      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('handles rapid visibility toggling - ends visible when last toggle is true', async () => {
      wrapper = mountComponent({ visible: false })

      for (let i = 0; i < 10; i++) {
        await wrapper.setProps({ visible: true })
        await wrapper.setProps({ visible: false })
      }

      // The watch only sets isVisible when visible becomes true,
      // so after rapid toggling ending on false, isVisible remains from last true toggle
      // since the component doesn't auto-close when visible prop becomes false
      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('internal close function resets isVisible regardless of prop', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()
      expect(wrapper.vm.isVisible).toBe(true)

      wrapper.vm.close()
      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('handles changing selected while visible', async () => {
      wrapper = mountComponent({
        visible: true,
        selected: { id: 1, name: 'First', enabled: true }
      })
      await nextTick()

      expect(wrapper.text()).toContain('First')

      await wrapper.setProps({ selected: { id: 2, name: 'Second', enabled: false } })
      await nextTick()

      expect(wrapper.text()).toContain('Second')
    })

    it('handles changing type while visible', async () => {
      wrapper = mountComponent({
        visible: true,
        type: 'source',
        status: 'Enable'
      })
      await nextTick()

      expect(wrapper.vm.label.title).toBe('Enable SNMP Data Collection Source')

      await wrapper.setProps({ type: 'mib-group' })
      expect(wrapper.vm.label.title).toBe('Enable MIB Group')
    })

    it('handles changing status while visible', async () => {
      wrapper = mountComponent({
        visible: true,
        type: 'source',
        status: 'Enable'
      })
      await nextTick()

      expect(wrapper.vm.label.title).toBe('Enable SNMP Data Collection Source')

      await wrapper.setProps({ status: 'Disable' })
      expect(wrapper.vm.label.title).toBe('Disable SNMP Data Collection Source')
    })
  })

  describe('Button Props', () => {
    it('Save button has primary prop', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      expect(saveButton?.props('primary')).toBe(true)
    })

    it('Cancel button does not have primary prop', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      expect(cancelButton?.props('primary')).toBeFalsy()
    })
  })

  describe('Dialog Configuration', () => {
    it('dialog has hide-close attribute', async () => {
      wrapper = mount(SnmpDataCollectionChangeStatusDialog, {
        props: { ...defaultProps, visible: true },
        global: {
          components: { FeatherButton, FeatherDialog }
        }
      })
      await nextTick()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('hideClose')).toBe(true)
    })
  })

  describe('Emit Patterns', () => {
    it('emits close once per cancel click', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      expect(wrapper.emitted('close')?.length).toBe(1)
    })

    it('emits confirm once per save click', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')?.length).toBe(1)
    })

    it('canceling does not emit confirm', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      expect(wrapper.emitted('close')?.length).toBe(1)
      expect(wrapper.emitted('confirm')).toBeFalsy()
    })

    it('saving does not emit close', async () => {
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')?.length).toBe(1)
      expect(wrapper.emitted('close')).toBeFalsy()
    })

    it('emits independently accumulate over multiple dialogopen/close cycles', async () => {
      // Start with fresh wrapper for cancel
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')
      expect(wrapper.emitted('close')?.length).toBe(1)
      expect(wrapper.emitted('confirm')).toBeFalsy()

      // Create a new wrapper for save scenario
      wrapper.unmount()
      wrapper = mountComponent({ visible: true })
      await nextTick()

      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      expect(wrapper.emitted('confirm')?.length).toBe(1)
      expect(wrapper.emitted('close')).toBeFalsy()
    })
  })

  describe('Integration Scenarios', () => {
    it('full enable workflow for source type', async () => {
      const selected = { id: 1, name: 'My SNMP Source', enabled: false }
      wrapper = mountComponent({
        visible: true,
        selected,
        status: 'Enable',
        type: 'source'
      })
      await nextTick()

      // Verify content
      expect(wrapper.text()).toContain('enable')
      expect(wrapper.text()).toContain('My SNMP Source')
      expect(wrapper.text()).toContain('Note:')
      expect(wrapper.vm.label.title).toBe('Enable SNMP Data Collection Source')

      // Click save
      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      // Verify emit
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'source'])
      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('full disable workflow for mib-group type', async () => {
      const selected = { id: 2, name: 'My MIB Group', enabled: true }
      wrapper = mountComponent({
        visible: true,
        selected,
        status: 'Disable',
        type: 'mib-group'
      })
      await nextTick()

      // Verify content
      expect(wrapper.text()).toContain('disable')
      expect(wrapper.text()).toContain('My MIB Group')
      expect(wrapper.text()).not.toContain('Note:')
      expect(wrapper.vm.label.title).toBe('Disable MIB Group')

      // Click save
      const saveButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Save')
      await saveButton?.trigger('click')

      // Verify emit
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'mib-group'])
    })

    it('cancel workflow preserves no emit of confirm', async () => {
      const selected = { id: 3, name: 'Test Item', enabled: true }
      wrapper = mountComponent({
        visible: true,
        selected,
        status: 'Disable',
        type: 'system-def'
      })
      await nextTick()

      // Click cancel
      const cancelButton = wrapper.findAllComponents(FeatherButton).find((b) => b.text() === 'Cancel')
      await cancelButton?.trigger('click')

      // Verify no confirm emitted
      expect(wrapper.emitted('confirm')).toBeFalsy()
      expect(wrapper.emitted('close')?.length).toBe(1)
      expect(wrapper.vm.isVisible).toBe(false)
    })
  })
})
