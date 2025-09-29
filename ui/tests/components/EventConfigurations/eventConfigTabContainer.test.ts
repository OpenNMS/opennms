import { mount, VueWrapper } from '@vue/test-utils'
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import EventConfigSourceTable from '@/components/EventConfiguration/EventConfigSourceTable.vue'
import EventConfigUploadFilesTab from '@/components/EventConfiguration/EventConfigUploadFilesTab.vue'
import { FeatherTab, FeatherTabContainer, FeatherTabPanel } from '@featherds/tabs'

describe('EventConfigTabContainer', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(() => {
    const pinia = createTestingPinia({
      createSpy: vi.fn,
      stubActions: false
    })

    store = useEventConfigStore(pinia)
    store.activeTab = 0
    wrapper = mount(EventConfigTabContainer, {
      global: {
        plugins: [pinia]
      }
    })
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
  
})