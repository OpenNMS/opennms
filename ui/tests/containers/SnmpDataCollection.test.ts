import SnmpDataCollection from '@/containers/SnmpDataCollection.vue'
import { useMenuStore } from '@/stores/menuStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

// Hoisted so these refs exist when the mock factories run during the
// service-chain module import (some services call useSnackbar() at load time).
const { mockDownloadDatacollectionConfig, mockShowSnackBar } = vi.hoisted(() => ({
  mockDownloadDatacollectionConfig: vi.fn(),
  mockShowSnackBar: vi.fn()
}))
vi.mock('@/services/snmpDataCollectionService', () => ({
  downloadDatacollectionConfig: (...args: any[]) => mockDownloadDatacollectionConfig(...args)
}))

vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar
  })
}))

const stubs = {
  SnmpDataCollectionSourcesTable: true,
  SnmpDataCollectionSourceImport: true,
  SnmpDataCollectionProfilesTable: true,
  BreadCrumbs: true
}

describe('SnmpDataCollection.vue (container)', () => {
  let wrapper: VueWrapper<any>
  let store: ReturnType<typeof useSnmpDataCollectionStore>

  const mountContainer = () => mount(SnmpDataCollection, {
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs
    }
  })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountContainer()
    store = useSnmpDataCollectionStore()
    store.activeTab = 0
    const menuStore = useMenuStore()
    menuStore.mainMenu = { homeUrl: '/opennms' } as any
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the heading and download button', () => {
      expect(wrapper.text()).toContain('Manage SNMP Data Collection Sources')
      expect(wrapper.find('[data-test="download-config-button"]').exists()).toBe(true)
    })

    it('renders the three tab labels', () => {
      const text = wrapper.text()
      expect(text).toContain('Data Collection Sources')
      expect(text).toContain('Import Data Collection Sources')
      expect(text).toContain('Profiles')
    })

    it('renders the active tab panel content (Sources)', () => {
      expect(wrapper.findComponent({ name: 'SnmpDataCollectionSourcesTable' }).exists()).toBe(true)
    })
  })

  describe('Tab binding', () => {
    it('writes the selected tab back to the store as a number', () => {
      wrapper.vm.onTabChange('2')
      expect(store.activeTab).toBe(2)
      expect(typeof store.activeTab).toBe('number')
    })
  })

  describe('Download config menu', () => {
    it('builds XML and JSON download items', () => {
      const labels = wrapper.vm.downloadMenuItems.map((i: any) => i.label)
      expect(labels).toEqual(['Download XML', 'Download JSON'])
    })

    it.each(['xml', 'json'] as const)('downloads the %s config via the menu command', async (format) => {
      mockDownloadDatacollectionConfig.mockResolvedValue(new Blob(['data']))
      const createSpy = vi.spyOn(window.URL, 'createObjectURL').mockReturnValue('blob:url')
      vi.spyOn(window.URL, 'revokeObjectURL').mockImplementation(() => {})

      const item = wrapper.vm.downloadMenuItems.find((i: any) => i.label.toLowerCase().includes(format))
      await item.command()
      await flushPromises()

      expect(mockDownloadDatacollectionConfig).toHaveBeenCalledWith(format)
      expect(createSpy).toHaveBeenCalled()
    })

    it('shows an error snackbar when the download throws', async () => {
      mockDownloadDatacollectionConfig.mockRejectedValue(new Error('boom'))
      await wrapper.vm.downloadConfig('xml')
      await flushPromises()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })
  })
})
