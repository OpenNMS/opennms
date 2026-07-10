import EventConfigTabContainer from '@/components/EventConfiguration/EventConfigTabContainer.vue'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import { createTestingPinia } from '@pinia/testing'
import { mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import Tab from 'primevue/tab'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

vi.mock('vue-router', () => ({
  useRouter: () => vi.fn()
}))

const stubs = {
  EventConfigSourceTable: { name: 'EventConfigSourceTable', template: '<div class="source-table-stub"></div>' },
  EventConfigUploadFilesTab: { name: 'EventConfigUploadFilesTab', template: '<div class="upload-tab-stub"></div>' }
}

describe('EventConfigTabContainer.vue', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useEventConfigStore>

  beforeEach(() => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    store = useEventConfigStore(pinia)
    store.activeTab = 0
    wrapper = mount(EventConfigTabContainer, {
      global: {
        plugins: [pinia, PrimeVue],
        stubs
      }
    })
  })

  afterEach(() => {
    wrapper.unmount()
    vi.clearAllMocks()
  })

  it('renders correctly', () => {
    expect(wrapper.find('.event-config-tab-container').exists()).toBe(true)
  })

  it('renders two tabs with the correct labels', () => {
    const tabs = wrapper.findAllComponents(Tab)
    expect(tabs).toHaveLength(2)
    expect(tabs[0].text()).toBe('View')
    expect(tabs[1].text()).toBe('Upload Files')
  })

  it('renders the source table for the active (View) tab', () => {
    expect(wrapper.findComponent({ name: 'EventConfigSourceTable' }).exists()).toBe(true)
  })

  it('writes the selected tab back to the store as a number', async () => {
    wrapper.vm.onTabChange('1')
    await nextTick()
    expect(store.activeTab).toBe(1)
    expect(typeof store.activeTab).toBe('number')
  })
})
