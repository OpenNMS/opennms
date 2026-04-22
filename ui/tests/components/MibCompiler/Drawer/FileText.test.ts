import { mount, VueWrapper, flushPromises } from '@vue/test-utils'
import { describe, it, expect, vi, afterEach, beforeEach } from 'vitest'
import FileText from '@/components/MibCompiler/Drawer/FileText.vue'

// Mock the @featherds/drawer module
vi.mock('@featherds/drawer', () => ({
  FeatherDrawer: {
    name: 'FeatherDrawer',
    props: ['modelValue', 'labels', 'width'],
    emits: ['update:modelValue', 'hidden'],
    template: `
      <div class="feather-drawer-stub" :data-visible="modelValue" data-test="scv-drawer">
        <div class="drawer-title">{{ labels?.title }}</div>
        <div class="drawer-content">
          <slot></slot>
        </div>
      </div>
    `
  }
}))

describe('FileText.vue', () => {
  let wrapper: VueWrapper<InstanceType<typeof FileText>>

  afterEach(() => {
    wrapper?.unmount()
  })

  const mountComponent = (props = {}, slots = {}) => {
    const defaultProps = {
      title: 'Test Title',
      visible: false
    }
    
    wrapper = mount(FileText, {
      props: { ...defaultProps, ...props },
      slots
    })
    return wrapper
  }

  describe('Basic Rendering', () => {
    it('renders the component', () => {
      mountComponent()
      expect(wrapper.exists()).toBe(true)
    })

    it('renders with FeatherDrawer stub', () => {
      mountComponent()
      expect(wrapper.find('.feather-drawer-stub').exists()).toBe(true)
    })

    it('renders the modal-body container', () => {
      mountComponent()
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('passes title to drawer labels', () => {
      mountComponent({ title: 'My Custom Title' })
      expect(wrapper.find('.drawer-title').text()).toBe('My Custom Title')
    })

    it('renders with different titles', () => {
      const titles = ['File Content', 'MIB Details', 'View Source']
      
      titles.forEach(title => {
        const w = mount(FileText, {
          props: { title, visible: false }
        })
        expect(w.find('.drawer-title').text()).toBe(title)
        w.unmount()
      })
    })
  })

  describe('Props', () => {
    it('requires title prop', () => {
      const { title } = FileText.props as { title: { required: boolean; type: StringConstructor } }
      expect(title.required).toBe(true)
      expect(title.type).toBe(String)
    })

    it('requires visible prop', () => {
      const { visible } = FileText.props as { visible: { required: boolean; type: BooleanConstructor } }
      expect(visible.required).toBe(true)
      expect(visible.type).toBe(Boolean)
    })

    it('accepts title as string', () => {
      mountComponent({ title: 'Test Title' })
      expect(wrapper.props('title')).toBe('Test Title')
    })

    it('accepts visible as boolean', () => {
      mountComponent({ visible: true })
      expect(wrapper.props('visible')).toBe(true)
    })

    it('handles long title', () => {
      const longTitle = 'This is a very long title that might need truncation in some cases'
      mountComponent({ title: longTitle })
      expect(wrapper.find('.drawer-title').text()).toBe(longTitle)
    })

    it('handles title with special characters', () => {
      const specialTitle = 'File: test.mib (Copy) [v2.0]'
      mountComponent({ title: specialTitle })
      expect(wrapper.find('.drawer-title').text()).toBe(specialTitle)
    })
  })

  describe('Visibility', () => {
    it('initializes with visible=false', () => {
      mountComponent({ visible: false })
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })

    it('initializes with visible=true', () => {
      mountComponent({ visible: true })
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
    })

    it('updates visibility when prop changes', async () => {
      mountComponent({ visible: false })
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
      
      await wrapper.setProps({ visible: true })
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
    })

    it('updates from visible to hidden', async () => {
      mountComponent({ visible: true })
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
      
      await wrapper.setProps({ visible: false })
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })

    it('handles rapid visibility changes', async () => {
      mountComponent({ visible: false })
      
      await wrapper.setProps({ visible: true })
      await wrapper.setProps({ visible: false })
      await wrapper.setProps({ visible: true })
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
    })
  })

  describe('Events', () => {
    it('emits hidden event when drawer closes', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      
      expect(wrapper.emitted('hidden')).toBeTruthy()
      expect(wrapper.emitted('hidden')?.length).toBe(1)
    })

    it('emits hidden event only once per close', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      
      expect(wrapper.emitted('hidden')?.length).toBe(1)
    })

    it('sets isVisible to false on close', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })
  })

  describe('Slots', () => {
    it('renders content slot', () => {
      mountComponent({}, {
        content: '<div class="custom-content">Custom Content</div>'
      })
      
      expect(wrapper.find('.custom-content').exists()).toBe(true)
      expect(wrapper.find('.custom-content').text()).toBe('Custom Content')
    })

    it('renders complex content in slot', () => {
      mountComponent({}, {
        content: `
          <div class="file-content">
            <pre class="code-block">Some MIB content here</pre>
            <button class="action-btn">Action</button>
          </div>
        `
      })
      
      expect(wrapper.find('.file-content').exists()).toBe(true)
      expect(wrapper.find('.code-block').exists()).toBe(true)
      expect(wrapper.find('.action-btn').exists()).toBe(true)
    })

    it('renders empty content slot', () => {
      mountComponent({}, {
        content: ''
      })
      
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('handles slot with multiple elements', () => {
      mountComponent({}, {
        content: `
          <p class="para-1">Paragraph 1</p>
          <p class="para-2">Paragraph 2</p>
          <p class="para-3">Paragraph 3</p>
        `
      })
      
      expect(wrapper.find('.para-1').exists()).toBe(true)
      expect(wrapper.find('.para-2').exists()).toBe(true)
      expect(wrapper.find('.para-3').exists()).toBe(true)
    })
  })

  describe('Computed Labels', () => {
    it('computes labels with title', () => {
      mountComponent({ title: 'My Title' })
      expect(wrapper.find('.drawer-title').text()).toBe('My Title')
    })

    it('updates labels when title prop changes', async () => {
      mountComponent({ title: 'Initial Title' })
      
      expect(wrapper.find('.drawer-title').text()).toBe('Initial Title')
      
      await wrapper.setProps({ title: 'Updated Title' })
      
      expect(wrapper.find('.drawer-title').text()).toBe('Updated Title')
    })

    it('handles title update while visible', async () => {
      mountComponent({ title: 'Title 1', visible: true })
      
      expect(wrapper.find('.drawer-title').text()).toBe('Title 1')
      
      await wrapper.setProps({ title: 'Title 2' })
      
      expect(wrapper.find('.drawer-title').text()).toBe('Title 2')
    })
  })

  describe('Drawer Configuration', () => {
    it('passes width prop to drawer', () => {
      mountComponent()
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      expect(drawer.props('width')).toBe('70em')
    })

    it('has correct data-test attribute', () => {
      mountComponent()
      expect(wrapper.find('[data-test="scv-drawer"]').exists()).toBe(true)
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => mountComponent()).not.toThrow()
    })

    it('unmounts without errors', () => {
      mountComponent()
      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('initializes with correct default state', () => {
      mountComponent({ visible: false })
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })

    it('handles remounting', () => {
      mountComponent({ visible: true, title: 'First Mount' })
      wrapper.unmount()
      
      mountComponent({ visible: false, title: 'Second Mount' })
      expect(wrapper.find('.drawer-title').text()).toBe('Second Mount')
    })
  })

  describe('Watcher', () => {
    it('watches visible prop and updates internal state', async () => {
      mountComponent({ visible: false })
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
      
      await wrapper.setProps({ visible: true })
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
    })

    it('handles multiple sequential prop changes', async () => {
      mountComponent({ visible: false })
      
      for (let i = 0; i < 5; i++) {
        await wrapper.setProps({ visible: true })
        await flushPromises()
        expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
        
        await wrapper.setProps({ visible: false })
        await flushPromises()
        expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
      }
    })
  })

  describe('CSS Classes', () => {
    it('has modal-body class on content container', () => {
      mountComponent()
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('modal-body contains slot content', () => {
      mountComponent({}, {
        content: '<span class="test-span">Test</span>'
      })
      
      const modalBody = wrapper.find('.modal-body')
      expect(modalBody.find('.test-span').exists()).toBe(true)
    })
  })

  describe('Edge Cases', () => {
    it('handles undefined slot content gracefully', () => {
      mountComponent()
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('handles visibility toggle during event emission', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      
      await wrapper.setProps({ visible: false })
      await drawer.vm.$emit('hidden')
      await flushPromises()
      
      expect(wrapper.emitted('hidden')).toBeTruthy()
    })

    it('handles empty string title', () => {
      mountComponent({ title: '' })
      expect(wrapper.find('.drawer-title').text()).toBe('')
    })

    it('handles unicode title', () => {
      const unicodeTitle = 'MIB 文件 - 测试 🔧'
      mountComponent({ title: unicodeTitle })
      expect(wrapper.find('.drawer-title').text()).toBe(unicodeTitle)
    })

    it('handles HTML entities in title', () => {
      const htmlTitle = 'File &amp; Content'
      mountComponent({ title: htmlTitle })
      expect(wrapper.find('.drawer-title').text()).toBe(htmlTitle)
    })
  })

  describe('Accessibility', () => {
    it('renders drawer with appropriate structure', () => {
      mountComponent({ title: 'Accessible Drawer', visible: true })
      
      expect(wrapper.find('.feather-drawer-stub').exists()).toBe(true)
      expect(wrapper.find('.modal-body').exists()).toBe(true)
    })

    it('includes title in labels for screen readers', () => {
      mountComponent({ title: 'Screen Reader Title' })
      expect(wrapper.find('.drawer-title').text()).toBe('Screen Reader Title')
    })
  })

  describe('Integration with Parent', () => {
    it('can be controlled by parent visibility prop', async () => {
      mountComponent({ visible: false })
      
      // Parent opens drawer
      await wrapper.setProps({ visible: true })
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
      
      // Parent closes drawer
      await wrapper.setProps({ visible: false })
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })

    it('notifies parent when drawer is closed internally', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      
      expect(wrapper.emitted('hidden')).toBeTruthy()
    })
  })

  describe('Multiple Instances', () => {
    it('handles multiple instances independently', () => {
      const wrapper1 = mount(FileText, {
        props: { title: 'Drawer 1', visible: true }
      })
      
      const wrapper2 = mount(FileText, {
        props: { title: 'Drawer 2', visible: false }
      })
      
      expect(wrapper1.find('.drawer-title').text()).toBe('Drawer 1')
      expect(wrapper1.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
      
      expect(wrapper2.find('.drawer-title').text()).toBe('Drawer 2')
      expect(wrapper2.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
      
      wrapper1.unmount()
      wrapper2.unmount()
    })
  })

  describe('Event Handler - onClose', () => {
    it('sets isVisible to false when onClose is called', async () => {
      mountComponent({ visible: true })
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('true')
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      await flushPromises()
      
      expect(wrapper.find('.feather-drawer-stub').attributes('data-visible')).toBe('false')
    })

    it('emits hidden event when onClose is called', async () => {
      mountComponent({ visible: true })
      
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      await drawer.vm.$emit('hidden')
      
      expect(wrapper.emitted('hidden')).toHaveLength(1)
    })
  })

  describe('v-model binding', () => {
    it('passes modelValue to drawer', () => {
      mountComponent({ visible: true })
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      expect(drawer.props('modelValue')).toBe(true)
    })

    it('syncs internal state with visible prop', async () => {
      mountComponent({ visible: false })
      const drawer = wrapper.findComponent({ name: 'FeatherDrawer' })
      
      expect(drawer.props('modelValue')).toBe(false)
      
      await wrapper.setProps({ visible: true })
      expect(drawer.props('modelValue')).toBe(true)
    })
  })
})

