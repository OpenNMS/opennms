import DeleteSnmpDataCollectionSource from '@/components/SnmpDataCollection/DIalog/DeleteSnmpDataCollectionSource.vue'
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

describe('DeleteSnmpDataCollectionSource', () => {
  let wrapper: VueWrapper<any>

  const createWrapper = (props: any = {}) => {
    return mount(DeleteSnmpDataCollectionSource, {
      props: {
        visible: true,
        selected: { id: 1, name: 'Test Source' },
        type: 'source',
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

    it('renders root container with correct class', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.find('.delete-snmp-data-collection-source').exists()).toBe(true)
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

    it('Cancel button does not have primary prop', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      expect(cancelButton.props('primary')).toBeFalsy()
    })
  })

  describe('Dialog Title', () => {
    it('displays correct title for SNMP Data Collection Source', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete SNMP Data Collection Source' })
    })

    it('title remains constant regardless of type prop', async () => {
      wrapper = createWrapper({ type: 'mib-group' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete SNMP Data Collection Source' })
    })

    it('title remains constant when type changes', async () => {
      wrapper = createWrapper({ type: 'source' })
      await flushPromises()

      await wrapper.setProps({ type: 'system-def' })
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.props('labels')).toEqual({ title: 'Delete SNMP Data Collection Source' })
    })
  })

  describe('Dialog Content', () => {
    it('displays delete message with source name', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'Cisco SNMP Source' } })
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('This will delete the SNMP Data Collection Source:')
      expect(modalBody.text()).toContain('Cisco SNMP Source')
    })

    it('displays selected name in bold', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'Test Source Name' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.exists()).toBe(true)
      expect(strong.text()).toBe('Test Source Name')
    })

    it('displays Note section about cascading delete', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Note:')
    })

    it('mentions MIB Groups removal', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('MIB Groups')
    })

    it('mentions System Definitions removal', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('System Definitions')
    })

    it('mentions Resource Types removal', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Resource Types')
    })

    it('displays confirmation question', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.text()).toContain('Are you sure you want to proceed?')
    })

    it('renders all three paragraphs', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      const paragraphs = modalBody.findAll('p')
      expect(paragraphs).toHaveLength(3)
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
      const selected = { id: 5, name: 'Test Source' }
      wrapper = createWrapper({ type: 'source', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')).toHaveLength(1)
      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'source'])
    })

    it('emits confirm with null when selected is null on Delete click', async () => {
      wrapper = createWrapper({ selected: null })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([null, 'source'])
    })

    it('emits confirm with correct type for mib-group', async () => {
      const selected = { id: 2, name: 'MIB Item' }
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

    it('emits confirm with correct type for resource-type', async () => {
      const selected = { id: 4, name: 'Resource Item' }
      wrapper = createWrapper({ type: 'resource-type', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([selected, 'resource-type'])
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

  describe('Visibility Management', () => {
    it('syncs isVisible with visible prop on mount', async () => {
      wrapper = createWrapper({ visible: true })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(true)
    })

    it('starts with isVisible false when visible prop is false', async () => {
      wrapper = createWrapper({ visible: false })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)
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

    it('handles multiple visibility toggles correctly', async () => {
      wrapper = createWrapper({ visible: false })
      await flushPromises()

      expect(wrapper.vm.isVisible).toBe(false)

      await wrapper.setProps({ visible: true })
      await flushPromises()
      expect(wrapper.vm.isVisible).toBe(true)

      // Click cancel to close
      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      await cancelButton.trigger('click')
      await flushPromises()
      expect(wrapper.vm.isVisible).toBe(false)

      // Reopen
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
      const specialName = 'Test<>Source&"Special\'Chars'
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

    it('does not crash when selected changes while dialog is open', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'First' } })
      await flushPromises()

      await wrapper.setProps({ selected: { id: 2, name: 'Second' } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe('Second')
    })

    it('emits correct data after selected changes', async () => {
      wrapper = createWrapper({ selected: { id: 1, name: 'First' } })
      await flushPromises()

      await wrapper.setProps({ selected: { id: 2, name: 'Second' } })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')?.[0]).toEqual([{ id: 2, name: 'Second' }, 'source'])
    })

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
  })

  describe('Unicode and International Characters', () => {
    it('handles Chinese characters in name', async () => {
      const chineseName = '测试数据源'
      wrapper = createWrapper({ selected: { id: 1, name: chineseName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(chineseName)
    })

    it('handles Japanese characters in name', async () => {
      const japaneseName = 'テストソース'
      wrapper = createWrapper({ selected: { id: 1, name: japaneseName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(japaneseName)
    })

    it('handles Korean characters in name', async () => {
      const koreanName = '테스트 소스'
      wrapper = createWrapper({ selected: { id: 1, name: koreanName } })
      await flushPromises()

      const strong = wrapper.find('strong')
      expect(strong.text()).toBe(koreanName)
    })

    it('handles emoji in name', async () => {
      const emojiName = 'Source 🚀 Test 📊'
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
      expect(strong.text()).toBe(name.trim())
    })

    it('handles trailing whitespace in name (trimmed by DOM)', async () => {
      const name = 'Trailing Spaces   '
      wrapper = createWrapper({ selected: { id: 1, name } })
      await flushPromises()

      const strong = wrapper.find('strong')
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

      // Reopen
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

  describe('Rapid Prop Changes', () => {
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

    it('handles rapid type changes without crashing', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const types: Array<'source' | 'mib-group' | 'system-def' | 'resource-type'> = [
        'source',
        'mib-group',
        'system-def',
        'resource-type',
        'source'
      ]

      for (const type of types) {
        await wrapper.setProps({ type })
      }
      await flushPromises()

      const dialog = wrapper.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Event Payload Validation', () => {
    it('confirm event payload has correct structure', async () => {
      const selected = { id: 123, name: 'Test Source' }
      wrapper = createWrapper({ type: 'source', selected })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      const emitted = wrapper.emitted('confirm')
      expect(emitted).toBeTruthy()
      expect(emitted).toHaveLength(1)

      const [emittedSelected, emittedType] = emitted![0] as [{ id: number; name: string }, string]
      expect(emittedSelected).toHaveProperty('id', 123)
      expect(emittedSelected).toHaveProperty('name', 'Test Source')
      expect(emittedType).toBe('source')
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

    it('label computed property returns correct value', async () => {
      wrapper = createWrapper()
      await flushPromises()

      expect(wrapper.vm.label).toEqual({ title: 'Delete SNMP Data Collection Source' })
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

  describe('CSS Structure', () => {
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

      const rootContainer = wrapper.find('.delete-snmp-data-collection-source')
      const dialog = rootContainer.findComponent(FeatherDialog)
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Content Sections', () => {
    it('renders exactly three paragraphs', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const modalBody = wrapper.find('.modal-body')
      const paragraphs = modalBody.findAll('p')
      expect(paragraphs).toHaveLength(3)
    })

    it('first paragraph contains delete message', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const paragraphs = wrapper.findAll('p')
      expect(paragraphs[0].text()).toContain('This will delete the SNMP Data Collection Source')
    })

    it('second paragraph contains Note', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const paragraphs = wrapper.findAll('p')
      expect(paragraphs[1].text()).toContain('Note:')
    })

    it('third paragraph contains confirmation question', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const paragraphs = wrapper.findAll('p')
      expect(paragraphs[2].text()).toContain('Are you sure you want to proceed?')
    })
  })

  describe('Button Functionality', () => {
    it('buttons remain functional after multiple renders', async () => {
      wrapper = createWrapper()
      await flushPromises()

      // Trigger re-renders
      await wrapper.setProps({ selected: { id: 2, name: 'Updated' } })
      await flushPromises()

      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]
      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('confirm')).toBeTruthy()
      expect(wrapper.emitted('confirm')?.[0]).toEqual([{ id: 2, name: 'Updated' }, 'source'])
    })

    it('cancel and delete buttons work independently', async () => {
      wrapper = createWrapper()
      await flushPromises()

      const cancelButton = wrapper.findAllComponents(FeatherButton)[0]
      const deleteButton = wrapper.findAllComponents(FeatherButton)[1]

      await cancelButton.trigger('click')
      await flushPromises()

      await deleteButton.trigger('click')
      await flushPromises()

      expect(wrapper.emitted('close')).toHaveLength(1)
      expect(wrapper.emitted('confirm')).toHaveLength(1)
    })
  })
})

