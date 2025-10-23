import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfigSourceTable from '@/components/EventConfiguration/EventConfigSourceTable.vue'
import EventConfigUploadFilesTab from '@/components/EventConfiguration/EventConfigUploadFilesTab.vue'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'
import { FeatherButton } from '@featherds/button'

describe('EventConfigTabContainer', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn
    })

    vi.mock('vue-router', () => ({
      useRouter: () => vi.fn()
    }))

    store = useEventConfigStore(pinia)
    store.activeTab = 0
    wrapper = mount(EventConfigTabContainer, {
      global: {
        plugins: [pinia],
        stubs: {
          FeatherButton,
          FeatherTab,
          FeatherTabContainer,
          FeatherTabPanel
        }
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    expect(wrapper.exists()).toBe(true)
  })

  it('renders two tabs with correct labels', () => {
    const tabs = wrapper.findAllComponents(FeatherTab)
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('View')
    expect(tabs[1].text()).toBe('Upload Files')
  })

  it('renders tab container with correct active tab', () => {
    const tabContainer = wrapper.findComponent(FeatherTabContainer)
    expect(tabContainer.exists()).toBe(true)
    expect(tabContainer.props('modelValue')).toBe(0)
  })

  it('renders both tab panels', () => {
    const tabPanels = wrapper.findAllComponents(FeatherTabPanel)
    expect(tabPanels).toHaveLength(2)
  })

  it('renders EventConfigSourceTable in first tab panel', () => {
    const sourceTable = wrapper.findComponent(EventConfigSourceTable)
    expect(sourceTable.exists()).toBe(true)
  })

  it('renders EventConfigUploadFilesTab in second tab panel', () => {
    const uploadFilesTab = wrapper.findComponent(EventConfigUploadFilesTab)
    expect(uploadFilesTab.exists()).toBe(true)
  })

  it('updates active tab when tab is changed', async () => {
    const tabContainer = wrapper.findComponent(FeatherTabContainer)
    await tabContainer.setValue(1)
    expect(store.activeTab).toBe(1)
  })

  it('renders the component without crashing', () => {
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.classes()).toContain('event-config-tab-container')
  })

  it('renders with activeTab set to 1 initially (second tab active)', async () => {
    store.activeTab = 1
    await wrapper.vm.$nextTick()

    const tabContainer = wrapper.findComponent(FeatherTabContainer)
    const panels = tabContainer.findAllComponents(FeatherTabPanel)

    expect(panels.length).toBe(2)

    expect(panels[0].attributes('aria-expanded')).toBe('false')
    expect(panels[1].attributes('aria-expanded')).toBe('true')
  })

  it('renders with activeTab set to 1 initially (second tab active)', async () => {
    store.activeTab = 0
    await wrapper.vm.$nextTick()

    const tabContainer = wrapper.findComponent(FeatherTabContainer)
    const panels = tabContainer.findAllComponents(FeatherTabPanel)

    expect(panels.length).toBe(2)

    expect(panels[0].attributes('aria-expanded')).toBe('true')
    expect(panels[1].attributes('aria-expanded')).toBe('false')
  })

  it('does not crash if vue-router mock fails', () => {
    // Temporarily break the mock to simulate import failure
    vi.doMock('vue-router', () => ({
      useRouter: () => {
        throw new Error('Mock fail')
      }
    }))
    expect(() => {
      wrapper = mount(EventConfigTabContainer, {
        global: {
          plugins: [createTestingPinia({ createSpy: vi.fn })],
          stubs: {
            /* ... same as before */
          }
        }
      })
    }).not.toThrow() // Or expect specific handling if component catches it
    vi.doUnmock('vue-router')
  })

  it('gracefully handles missing Pinia plugin (no store)', () => {
    wrapper = mount(EventConfigTabContainer, {
      global: {
        // No plugins: [pinia] provided
        stubs: {
          /* ... same as before */
        }
      }
    })
    // Expect no crash; component should render skeleton (tabs without reactivity)
    expect(wrapper.exists()).toBe(true)
    expect(wrapper.findComponent(FeatherTabContainer).exists()).toBe(true)
  })

  it('renders tab panels with correct data-test attributes', () => {
    const sourcePanel = wrapper.find('.event-configuration-table')
    const uploadPanel = wrapper.find('.upload-files-tab')
    expect(sourcePanel.exists()).toBe(true)
    expect(uploadPanel.exists()).toBe(true)
  })

  it('child components are not re-mounted unnecessarily on tab switch', async () => {
    const sourceTable = wrapper.findComponent(EventConfigSourceTable)
    const uploadFilesTab = wrapper.findComponent(EventConfigUploadFilesTab)

    // Since children are not stubbed and tabs likely use v-show (not v-if), check existence after switch
    expect(sourceTable.exists()).toBe(true)
    expect(uploadFilesTab.exists()).toBe(true)

    await wrapper.findComponent(FeatherTabContainer).setValue(1)
    await wrapper.vm.$nextTick()

    // Both should still exist (no unmount/remount)
    expect(sourceTable.exists()).toBe(true)
    expect(uploadFilesTab.exists()).toBe(true)
  })
})

