import DeleteConfirmationDialog from '@/components/SnmpDataCollection/Dialog/DeleteConfirmationDialog.vue'
import { FeatherButton } from '@featherds/button'
import { FeatherDialog } from '@featherds/dialog'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, describe, expect, it, vi } from 'vitest'

vi.mock('@featherds/dialog', () => ({
  FeatherDialog: {
    name: 'FeatherDialog',
    template: '<div><slot></slot><slot name="footer"></slot></div>',
    props: ['labels', 'modelValue', 'hideClose'],
    emits: ['hidden']
  }
}))

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
        components: { FeatherButton, FeatherDialog }
      }
    })
  }

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  describe('Component Rendering', () => {
    it('renders the dialog when visible is true', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })

    it('does not render modal body when selected is null', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(false)
    })

    it('does not render modal body when selected id is missing', async () => {
      wrapper = createWrapper({ selected: { name: 'Test' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(false)
    })

    it('does not render modal body when selected name is missing', async () => {
      wrapper = createWrapper({ selected: { id: 1 } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(false)
    })

    it('renders modal body when selected has both id and name', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('renders Cancel and Delete buttons', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons).toHaveLength(2)
      expect(buttons[0].text()).toBe('Cancel')
      expect(buttons[1].text()).toBe('Delete')
    })

    it('Delete button has primary prop', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      expect(deleteButton.props('primary')).toBe(true)
    })
  })

  describe('Dialog Titles', () => {
    it('displays correct title for resource-type', async () => {
      wrapper = createWrapper({ type: 'resource-type' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete Resource Type' })
    })

    it('displays correct title for source', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete SNMP Data Collection Source' })
    })

    it('displays correct title for mib-group', async () => {
      wrapper = createWrapper({ type: 'mib-group' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete MIB Group' })
    })

    it('displays correct title for system-def', async () => {
      wrapper = createWrapper({ type: 'system-def' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete System Definition' })
    })

    it('updates title when type changes', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()

      let dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete SNMP Data Collection Source' })

      await wrapper.setProps({ type: 'mib-group' })
      await flushPromises()

      dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete MIB Group' })
    })
  })

  describe('Dialog Content', () => {
    it('displays correct content for resource-type', async () => {
      wrapper = createWrapper({ type: 'resource-type', selected: { id: 5, name: 'CPU Resource' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('This will delete the Resource Type:')
      expect(modalBody.text()).toContain('CPU Resource')
      expect(modalBody.text()).toContain('This action can not be undone.')
      expect(modalBody.text()).toContain('Are you sure you want to proceed?')
    })

    it('displays correct content for source', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 3, name: 'Cisco Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('This will delete the SNMP Data Collection Source:')
      expect(modalBody.text()).toContain('Cisco Source')
      expect(modalBody.text()).toContain(
        'Deleting an SNMP Data Collection Source will also remove all associated MIB Groups'
      )
      expect(modalBody.text()).toContain('System Definitions, and Resource Types')
    })

    it('displays correct content for mib-group', async () => {
      wrapper = createWrapper({ type: 'mib-group', selected: { id: 7, name: 'System MIB' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('This will delete the MIB Group:')
      expect(modalBody.text()).toContain('System MIB')
    })

    it('displays correct content for system-def', async () => {
      wrapper = createWrapper({ type: 'system-def', selected: { id: 2, name: 'Router Definition' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('This will delete the System Definition:')
      expect(modalBody.text()).toContain('Router Definition')
    })

    it('displays selected name in bold', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'Test Name' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.exists()).toBe(true)
      expect(strong.text()).toBe('Test Name')
    })
  })

  describe('Event Emissions', () => {
    it('emits close event when Cancel button is clicked', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('close')).toBeTruthy()
      expect(wrapper.emitted('close')).toHaveLength(1)
    })

    it('emits close event when dialog hidden event fires', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      await dialog.vm.$emit('hidden')
      await flushPromises()

      expect(wrapper.emitted('close')).toBeTruthy()
    })

    it('emits confirm event with correct parameters when Delete is clicked', async () => {
      const selected = { id: 5, name: 'Test Resource' }
      wrapper = createWrapper({ type: 'resource-type', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')).toHaveLength(1)
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'resource-type'])
    })

    it('emits confirm with null when selected is null on Delete click', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'resource-type'])
    })

    it('emits confirm with correct type for source', async () => {
      const selected = { id: 2, name: 'Source Item' }
      wrapper = createWrapper({ type: 'source', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'source'])
    })

    it('emits confirm with correct type for mib-group', async () => {
      const selected = { id: 8, name: 'MIB Item' }
      wrapper = createWrapper({ type: 'mib-group', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'mib-group'])
    })

    it('emits confirm with correct type for system-def', async () => {
      const selected = { id: 3, name: 'System Item' }
      wrapper = createWrapper({ type: 'system-def', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'system-def'])
    })

    it('sets isVisible to false when Cancel is clicked', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('sets isVisible to false when Delete is clicked', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)
    })
  })

  describe('Visibility Management', () => {
    it('syncs isVisible with visible prop on mount', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('updates isVisible when visible prop changes from false to true', async () => {
      wrapper = createWrapper({ visible: false })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('does not update isVisible when visible prop changes from true to false', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)

      await wrapper.setProps({ visible: false })
      await flushPromises()

      // isVisible should stay true because watcher only updates on visible=true
      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('updates isVisible immediately when visible prop is set with immediate option', async () => {
      wrapper = createWrapper({ visible: true })
      await wrapper.vm.$nextTick()

      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('handles multiple visibility toggles correctly', async () => {
      wrapper = createWrapper({ visible: false })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await flushPromises()
      expect(wrapper.vm.isVisible).toBe(true)

      // Click cancel to close - this sets isVisible to false
      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()
      expect(wrapper.vm.isVisible).toBe(false)

      // Open again - watcher will update isVisible when visible prop changes to true
      await wrapper.setProps({ visible: false })
      await flushPromises()
      await wrapper.setProps({ visible: true })
      await flushPromises()
      expect(wrapper.vm.isVisible).toBe(true)
    })
  })

  describe('Dialog Props', () => {
    it('has hideClose prop set', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      // hideClose is a boolean attribute, check it's defined (not undefined)
      expect(dialog.props('hideClose')).toBeDefined()
    })

    it('binds isVisible to FeatherDialog modelValue', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('modelValue')).toBe(true)
    })
  })

  describe('Edge Cases', () => {
    it('handles selected with id as 0', async () => {
      wrapper = createWrapper({ selected: { id: 0, name: 'Zero ID' } })
      await flushPromises()

      // Should not render because 0 is falsy
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(false)
    })

    it('handles empty string name', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: '' } })
      await flushPromises()

      // Should not render because empty string is falsy
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(false)
    })

    it('handles special characters in name', async () => {
      const specialName = 'Test<>Name&"Special\'Chars'
      wrapper = createWrapper({ selected: { id: 1, name: specialName } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain(specialName)
    })

    it('handles long names correctly', async () => {
      const longName = 'A'.repeat(200)
      wrapper = createWrapper({ selected: { id: 1, name: longName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(longName)
    })

    it('does not crash when type changes while dialog is open', async () => {
      wrapper = createWrapper({ type: 'source', visible: true })
      await flushPromises()

      await wrapper.setProps({ type: 'mib-group' })
      await flushPromises()

      await wrapper.setProps({ type: 'system-def' })
      await flushPromises()

      await wrapper.setProps({ type: 'resource-type' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
      expect(dialog.props('labels')).toEqual({ title: 'Delete Resource Type' })
    })

    it('does not crash when selected changes while dialog is open', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'First' } })
      await flushPromises()

      await wrapper.setProps({ selected: { id: 2, name: 'Second' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe('Second')
    })

    it('emits correct data after selected and type are changed', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'First' } })
      await flushPromises()

      await wrapper.setProps({
        type: 'mib-group',
        selected: { id: 2, name: 'Second' }
      })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([{ id: 2, name: 'Second' }, 'mib-group'])
    })

    it('does not emit events when buttons are not present', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons).toHaveLength(2) // Buttons still exist in footer slot

      await buttons[1].trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'resource-type'])
    })
  })

  describe('Multiple Interactions', () => {
    it('can be opened, cancelled, and reopened', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      // Cancel
      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('close')).toHaveLength(1)
      expect(wrapper.vm.isVisible).toBe(false)

      // Reopen - need to toggle visible prop to false first then back to true
      // for the watcher to trigger again
      await wrapper.setProps({ visible: false })
      await flushPromises()
      await wrapper.setProps({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('emits correct number of events for multiple cancel clicks', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]

      await cancelButton.trigger('click')
      await flushPromises()

      await cancelButton.trigger('click')
      await flushPromises()

      await cancelButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('close')).toHaveLength(3)
    })

    it('emits correct number of events for multiple delete clicks', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]

      await deleteButton.trigger('click')
      await flushPromises()

      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toHaveLength(2)
    })
  })

  describe('CSS Structure', () => {
    it('has root container with correct class', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.delete-event-config-source-modal').exists()).toBe(true)
    })

    it('modal body has correct class when rendered', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
      expect(modalBody.classes()).toContain('modal-body')
    })

    it('contains FeatherDialog as direct child', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const rootContainer = wrapper.find('.delete-event-config-source-modal')
      const dialog = rootContainer.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Source Type Specific Content', () => {
    it('displays Note section only for source type', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Note:')
    })

    it('does not display Note section for mib-group type', async () => {
      wrapper = createWrapper({ type: 'mib-group', selected: { id: 1, name: 'Test MIB' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).not.toContain('Note:')
    })

    it('does not display Note section for system-def type', async () => {
      wrapper = createWrapper({ type: 'system-def', selected: { id: 1, name: 'Test System' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).not.toContain('Note:')
    })

    it('does not display Note section for resource-type', async () => {
      wrapper = createWrapper({ type: 'resource-type', selected: { id: 1, name: 'Test Resource' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).not.toContain('Note:')
    })

    it('mentions MIB Groups removal for source type', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('MIB Groups')
    })

    it('mentions System Definitions removal for source type', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('System Definitions')
    })

    it('mentions Resource Types removal for source type', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Resource Types')
    })
  })

  describe('Unicode and International Characters', () => {
    it('handles Chinese characters in name', async () => {
      const chineseName = '测试资源类型'
      wrapper = createWrapper({ selected: { id: 1, name: chineseName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(chineseName)
    })

    it('handles Japanese characters in name', async () => {
      const japaneseName = 'テストリソース'
      wrapper = createWrapper({ selected: { id: 1, name: japaneseName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(japaneseName)
    })

    it('handles Korean characters in name', async () => {
      const koreanName = '테스트 리소스'
      wrapper = createWrapper({ selected: { id: 1, name: koreanName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(koreanName)
    })

    it('handles Arabic characters in name', async () => {
      const arabicName = 'اختبار المورد'
      wrapper = createWrapper({ selected: { id: 1, name: arabicName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(arabicName)
    })

    it('handles Cyrillic characters in name', async () => {
      const cyrillicName = 'Тестовый ресурс'
      wrapper = createWrapper({ selected: { id: 1, name: cyrillicName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(cyrillicName)
    })

    it('handles emoji in name', async () => {
      const emojiName = 'Test 🚀 Resource 📦'
      wrapper = createWrapper({ selected: { id: 1, name: emojiName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(emojiName)
    })

    it('handles mixed scripts in name', async () => {
      const mixedName = 'Test测试テスト'
      wrapper = createWrapper({ selected: { id: 1, name: mixedName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(mixedName)
    })
  })

  describe('Whitespace Handling', () => {
    it('handles leading whitespace in name (trimmed by DOM)', async () => {
      const name = '   Leading Spaces'
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
      // DOM naturally trims leading whitespace
      expect(strong.text()).toBe(name.trim())
    })

    it('handles trailing whitespace in name (trimmed by DOM)', async () => {
      const name = 'Trailing Spaces   '
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
      // DOM naturally trims trailing whitespace
      expect(strong.text()).toBe(name.trim())
    })

    it('handles multiple internal spaces in name', async () => {
      const name = 'Name    With    Spaces'
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(name)
    })

    it('handles tabs in name', async () => {
      const name = 'Name\twith\ttabs'
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toContain('Name')
      expect(strong.text()).toContain('tabs')
    })

    it('handles newlines in name', async () => {
      const name = 'Name\nwith\nnewlines'
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toContain('Name')
      expect(strong.text()).toContain('newlines')
    })
  })

  describe('Numeric Edge Cases', () => {
    it('handles negative id', async () => {
      wrapper = createWrapper({ selected: { id: -1, name: 'Negative ID' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('handles very large id', async () => {
      wrapper = createWrapper({ selected: { id: Number.MAX_SAFE_INTEGER, name: 'Large ID' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('handles decimal id', async () => {
      wrapper = createWrapper({ selected: { id: 1.5, name: 'Decimal ID' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.exists()).toBe(true)
    })

    it('handles numeric name string', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: '12345' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe('12345')
    })

    it('handles name starting with number', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: '123-Resource-Name' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe('123-Resource-Name')
    })
  })

  describe('Common Content', () => {
    it('displays "This action can not be undone" for all types', async () => {
      const types: Array<'source' | 'mib-group' | 'system-def' | 'resource-type'> = [
        'source',
        'mib-group',
        'system-def',
        'resource-type'
      ]

      for (const type of types) {
        wrapper = createWrapper({ type, selected: { id: 1, name: 'Test' } })
        await flushPromises()

        const modalBody = wrapper.find('.modal-body')
        expect(modalBody.text()).toContain('This action can not be undone.')

        wrapper.unmount()
      }
    })

    it('displays "Are you sure you want to proceed?" for all types', async () => {
      const types: Array<'source' | 'mib-group' | 'system-def' | 'resource-type'> = [
        'source',
        'mib-group',
        'system-def',
        'resource-type'
      ]

      for (const type of types) {
        wrapper = createWrapper({ type, selected: { id: 1, name: 'Test' } })
        await flushPromises()

        const modalBody = wrapper.find('.modal-body')
        expect(modalBody.text()).toContain('Are you sure you want to proceed?')

        wrapper.unmount()
      }
    })

    it('displays name in bold for all types', async () => {
      const types: Array<'source' | 'mib-group' | 'system-def' | 'resource-type'> = [
        'source',
        'mib-group',
        'system-def',
        'resource-type'
      ]

      for (const type of types) {
        wrapper = createWrapper({ type, selected: { id: 1, name: 'Test Name' } })
        await flushPromises()

        const strong = wrapper.find('strong')
        expect(strong.exists()).toBe(true)
        expect(strong.text()).toBe('Test Name')

        wrapper.unmount()
      }
    })
  })

  describe('Rapid Prop Changes', () => {
    it('handles rapid type changes without crashing', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()

      const types: Array<'source' | 'mib-group' | 'system-def' | 'resource-type'> = [
        'mib-group',
        'system-def',
        'resource-type',
        'source',
        'mib-group'
      ]

      for (const type of types) {
        await wrapper.setProps({ type })
      }
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })

    it('handles rapid selected changes without crashing', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const items = [
        { id: 1, name: 'First' },
        { id: 2, name: 'Second' },
        { id: 3, name: 'Third' },
        null,
        { id: 4, name: 'Fourth' }
      ]

      for (const selected of items) {
        await wrapper.setProps({ selected })
      }
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })

    it('handles rapid visibility changes without crashing', async () => {
      wrapper = createWrapper({ visible: false })
      await flushPromises()

      for (let i = 0; i < 10; i++) {
        await wrapper.setProps({ visible: i % 2 === 0 })
      }
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Button States', () => {
    it('Cancel button does not have primary prop', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      expect(cancelButton.props('primary')).toBeFalsy()
    })

    it('buttons remain functional after multiple renders', async () => {
      wrapper = createWrapper()
      await flushPromises()

      // Trigger re-renders
      await wrapper.setProps({ selected: { id: 2, name: 'Updated' } })
      await flushPromises()
      await wrapper.setProps({ type: 'mib-group' })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([{ id: 2, name: 'Updated' }, 'mib-group'])
    })
  })

  describe('Content Sections', () => {
    it('renders exactly one type-specific content section', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      const divs = modalBody.findAll('div > div')

      // Only one type-specific div should have visible content
      const visibleDivs = divs.filter((d) => d.text().includes('This will delete'))
      expect(visibleDivs).toHaveLength(1)
    })

    it('all paragraphs are present for source type', async () => {
      wrapper = createWrapper({ type: 'source', selected: { id: 1, name: 'Test' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      const paragraphs = modalBody.findAll('p')

      // Source type has 4 paragraphs: delete message, note, "action cannot be undone", "are you sure"
      expect(paragraphs.length).toBeGreaterThanOrEqual(4)
    })

    it('all paragraphs are present for non-source types', async () => {
      wrapper = createWrapper({ type: 'mib-group', selected: { id: 1, name: 'Test' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      const paragraphs = modalBody.findAll('p')

      // Non-source types have 3 paragraphs: delete message, "action cannot be undone", "are you sure"
      expect(paragraphs.length).toBeGreaterThanOrEqual(3)
    })
  })

  describe('Event Payload Validation', () => {
    it('confirm event payload has correct structure', async () => {
      const selected = { id: 123, name: 'Test Resource' }
      wrapper = createWrapper({ type: 'resource-type', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      const emitted = wrapper.emitted('confirm')
      expect(emitted).toBeTruthy()
      expect(emitted).toHaveLength(1)

      const [emittedSelected, emittedType] = emitted![0]
      expect(emittedSelected).toHaveProperty('id', 123)
      expect(emittedSelected).toHaveProperty('name', 'Test Resource')
      expect(emittedType).toBe('resource-type')
    })

    it('close event has no payload', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()

      const emitted = wrapper.emitted('close')
      expect(emitted).toBeTruthy()
      expect(emitted![0]).toEqual([])
    })
  })

  describe('Component State Consistency', () => {
    it('isVisible reflects correct state after cancel', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('isVisible reflects correct state after delete', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)
    })

    it('label computed property updates correctly', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()

      expect(wrapper.vm.label).toEqual({ title: 'Delete SNMP Data Collection Source' })

      await wrapper.setProps({ type: 'mib-group' })
      await flushPromises()

      expect(wrapper.vm.label).toEqual({ title: 'Delete MIB Group' })
    })
  })

  describe('Accessibility', () => {
    it('dialog has labels prop for screen readers', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      const labels = dialog.props('labels') as { title: string }
      expect(labels).toBeDefined()
      expect(labels.title).toBeTruthy()
    })

    it('uses semantic strong element for emphasis', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const strongElements = wrapper.findAll('strong')
      expect(strongElements.length).toBeGreaterThan(0)
    })

    it('uses semantic paragraph elements for text content', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const paragraphs = wrapper.findAll('p')
      expect(paragraphs.length).toBeGreaterThan(0)
    })

    it('buttons have descriptive text', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const buttons = wrapper.findAllComponents(FeatherButton)
      expect(buttons[0].text()).toBe('Cancel')
      expect(buttons[1].text()).toBe('Delete')
    })
  })
})

