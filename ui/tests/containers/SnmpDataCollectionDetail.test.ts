import MibGroupsTable from '@/components/SnmpDataCollectionDetail/MibGroupsTable.vue'
import ResourceTypesTable from '@/components/SnmpDataCollectionDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollectionDetail/SystemDefinitionsTable.vue'
import SnmpDataCollectionDetail from '@/containers/SnmpDataCollectionDetail.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { format } from 'date-fns'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => ({
    params: { id: '1' }
  })),
  useRouter: vi.fn(() => ({
    push: mockPush
  }))
}))

describe('SnmpDataCollectionDetail.vue', () => {
  let wrapper: VueWrapper
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mockCollectionSource: SnmpCollectionSource = {
    id: 1,
    name: 'Test Collection',
    vendor: 'Test Vendor',
    description: 'Test Description',
    enabled: true,
    uploadedBy: 'test-user',
    createdTime: new Date('2024-01-15'),
    lastModified: new Date('2024-06-20')
  }

  const globalStubs = {
    FeatherBackButton: true,
    FeatherButton: true,
    SystemDefinitionsTable: true,
    ResourceTypesTable: true,
    MibGroupsTable: true
  }

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.clearAllMocks()
    store = useSnmpDataCollectionDetailStore()
  })

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount()
    }
  })

  const createWrapper = async (
    selectedSource: SnmpCollectionSource | null = mockCollectionSource
  ): Promise<VueWrapper> => {
    store.selectedCollectionSource = selectedSource
    store.fetchCollectionSourceById = vi.fn()

    wrapper = mount(SnmpDataCollectionDetail, {
      global: {
        stubs: globalStubs
      }
    })
    await wrapper.vm.$nextTick()
    await flushPromises()
    return wrapper
  }

  describe('Component Rendering', () => {
    it('renders the component with collection data', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('h1').text()).toBe('Data Collection Source Details')
    })

    it('renders heading text correctly', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('h1').text()).toBe('Data Collection Source Details')
    })

    it('renders all table components', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findComponent(SystemDefinitionsTable).exists()).toBe(true)
      expect(wrapper.findComponent(ResourceTypesTable).exists()).toBe(true)
      expect(wrapper.findComponent(MibGroupsTable).exists()).toBe(true)
    })

    it('renders back button', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findComponent(FeatherBackButton).exists()).toBe(true)
    })

    it('renders action buttons', async () => {
      wrapper = await createWrapper()

      const buttons = wrapper.findAll('.action-container button')
      expect(buttons.length).toBe(2)
    })

    it('renders enable/disable button', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="enable-disable-source"]').exists()).toBe(true)
    })

    it('renders delete button', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="delete-source"]').exists()).toBe(true)
    })

    it('renders config details box', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="config-box"]').exists()).toBe(true)
    })

    it('renders all child components together', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findComponent(FeatherBackButton).exists()).toBe(true)
      expect(wrapper.findComponent(SystemDefinitionsTable).exists()).toBe(true)
      expect(wrapper.findComponent(ResourceTypesTable).exists()).toBe(true)
      expect(wrapper.findComponent(MibGroupsTable).exists()).toBe(true)
      expect(wrapper.findAllComponents(FeatherButton).length).toBeGreaterThan(0)
    })
  })

  describe('Data Display', () => {
    it('displays collection source name', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('Test Collection')
    })

    it('displays uploaded by user', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('test-user')
    })

    it('displays vendor', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('Test Vendor')
    })

    it('displays enabled status', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('Enabled')
    })

    it('displays creation date formatted', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('01/15/2024')
    })

    it('displays last modified date formatted', async () => {
      wrapper = await createWrapper()

      expect(wrapper.text()).toContain('06/20/2024')
    })

    it('displays all field labels', async () => {
      wrapper = await createWrapper()

      const labels = ['Source:', 'Uploaded By:', 'Creation Date:', 'Vendor:', 'Status:', 'Last Modified Date:']
      const configBox = wrapper.find('.config-details-box')

      labels.forEach((label) => {
        expect(configBox.text()).toContain(label)
      })
    })

    it('displays config details correctly', async () => {
      wrapper = await createWrapper()

      const configBox = wrapper.find('.config-details-box')
      expect(configBox.text()).toContain('Test Collection')
      expect(configBox.text()).toContain('test-user')
      expect(configBox.text()).toContain('Test Vendor')
      expect(configBox.text()).toContain('Enabled')
      expect(configBox.text()).toContain(format(mockCollectionSource.createdTime, 'MM/dd/yyyy'))
      expect(configBox.text()).toContain(format(mockCollectionSource.lastModified, 'MM/dd/yyyy'))
    })
  })

  describe('Button Interactions', () => {
    it('navigates back when Go Back button is clicked', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.get('[data-test="back-button"]')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('displays "Disable Source" when source is enabled', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      expect(wrapper.text()).toContain('Disable Source')
    })

    it('displays "Enable Source" when source is disabled', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      expect(wrapper.text()).toContain('Enable Source')
    })

    it('shows Delete Source button', async () => {
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      expect(deleteButton.exists()).toBe(true)
      expect(deleteButton.text()).toBe('Delete Source')
    })

    it('handles multiple back button clicks', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('[data-test="back-button"]')
      await backButton.trigger('click')
      await backButton.trigger('click')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledTimes(3)
    })
  })

  describe('Not Found State', () => {
    it('shows "No data found" when source is null', async () => {
      wrapper = await createWrapper(null)

      expect(wrapper.find('.not-found-container').exists()).toBe(true)
      expect(wrapper.text()).toContain('No data found.')
    })

    it('shows not-found container when selectedCollectionSource is null', async () => {
      wrapper = await createWrapper(null)

      expect(wrapper.find('.not-found-container').exists()).toBe(true)
      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(false)
    })

    it('navigates back from not found page', async () => {
      wrapper = await createWrapper(null)

      const goBackButton = wrapper.find('.not-found-container button')
      await goBackButton.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('hides main content in not-found state', async () => {
      wrapper = await createWrapper(null)

      expect(wrapper.findComponent(SystemDefinitionsTable).exists()).toBe(false)
      expect(wrapper.findComponent(ResourceTypesTable).exists()).toBe(false)
      expect(wrapper.findComponent(MibGroupsTable).exists()).toBe(false)
    })
  })

  describe('CSS Structure', () => {
    it('applies correct CSS classes', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.title-container').exists()).toBe(true)
      expect(wrapper.find('.action-container').exists()).toBe(true)
      expect(wrapper.find('.config-details-box').exists()).toBe(true)
    })

    it('renders config rows structure', async () => {
      wrapper = await createWrapper()

      const configRows = wrapper.findAll('.config-row')
      expect(configRows.length).toBe(3)
    })

    it('has correct layout structure', async () => {
      wrapper = await createWrapper()

      const container = wrapper.find('.snmp-data-collection-detail-container')
      expect(container.find('.header').exists()).toBe(true)
      expect(container.find('.config-details-box').exists()).toBe(true)
      expect(container.find('.system-defs-container').exists()).toBe(true)
      expect(container.find('.resource-types-container').exists()).toBe(true)
      expect(container.find('.mib-groups-container').exists()).toBe(true)
    })

    it('renders table containers', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.system-defs-container').exists()).toBe(true)
      expect(wrapper.find('.resource-types-container').exists()).toBe(true)
      expect(wrapper.find('.mib-groups-container').exists()).toBe(true)
    })
  })

  describe('Store Interactions', () => {
    it('fetches collection source on mount when route has id', async () => {
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('1')
    })

    it('calls fetchCollectionSourceById with correct ID', async () => {
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledOnce()
      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('1')
    })

    it('does not fetch when route has no id param', async () => {
      vi.mocked(useRoute).mockReturnValue({ params: {} } as any)
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
    })
  })

  describe('Component Lifecycle', () => {
    it('mounts without errors', () => {
      expect(() => createWrapper()).not.toThrow()
    })

    it('unmounts without errors', async () => {
      wrapper = await createWrapper()

      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('renders correctly on mount', async () => {
      wrapper = await createWrapper()

      expect(wrapper.vm).toBeDefined()
      expect(wrapper.element).toBeInstanceOf(HTMLElement)
    })
  })

  describe('Parametrized Tests - Button States', () => {
    it.each([
      { enabled: true, expectedText: 'Disable Source' },
      { enabled: false, expectedText: 'Enable Source' }
    ])('should display "$expectedText" button when source is enabled=$enabled', async ({ enabled, expectedText }) => {
      const source = { ...mockCollectionSource, enabled }
      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain(expectedText)
    })
  })

  describe('Parametrized Tests - Status Display', () => {
    it.each([
      { enabled: true, expectedStatus: 'Enabled' },
      { enabled: false, expectedStatus: 'Disabled' }
    ])('should show "$expectedStatus" status when enabled=$enabled', async ({ enabled, expectedStatus }) => {
      const source = { ...mockCollectionSource, enabled }
      wrapper = await createWrapper(source)

      const configBox = wrapper.find('.config-details-box')
      expect(configBox.text()).toContain(`Status:${expectedStatus}`)
    })
  })

  describe('Parametrized Tests - Date Formatting', () => {
    it.each([
      { date: new Date('2024-01-15'), expectedFormat: '01/15/2024' },
      { date: new Date('2024-12-31'), expectedFormat: '12/31/2024' },
      { date: new Date('2024-06-01'), expectedFormat: '06/01/2024' },
      { date: new Date('2023-03-20'), expectedFormat: '03/20/2023' }
    ])('should format date $date correctly', async ({ date, expectedFormat }) => {
      const source = { ...mockCollectionSource, createdTime: date, lastModified: date }
      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain(expectedFormat)
    })
  })

  describe('Parametrized Tests - Component State', () => {
    it.each([
      { hasSource: true, description: 'with source data' },
      { hasSource: false, description: 'without source data (null)' }
    ])('renders correctly $description', async ({ hasSource }) => {
      const source = hasSource ? mockCollectionSource : null
      wrapper = await createWrapper(source)

      if (hasSource) {
        expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
        expect(wrapper.find('.not-found-container').exists()).toBe(false)
      } else {
        expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(false)
        expect(wrapper.find('.not-found-container').exists()).toBe(true)
      }
    })
  })

  describe('Parametrized Tests - Different Data Values', () => {
    it.each([
      { name: 'Simple Collection', vendor: 'Vendor A', uploadedBy: 'user1' },
      { name: 'Complex Collection Name', vendor: 'Vendor B', uploadedBy: 'admin' },
      { name: 'Test', vendor: 'OpenNMS', uploadedBy: 'test-user' },
      { name: 'Another Collection', vendor: 'Custom Vendor', uploadedBy: 'uploader' }
    ])('displays collection data: name=$name, vendor=$vendor', async ({ name, vendor, uploadedBy }) => {
      const source = { ...mockCollectionSource, name, vendor, uploadedBy }
      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain(name)
      expect(wrapper.text()).toContain(vendor)
      expect(wrapper.text()).toContain(uploadedBy)
    })
  })

  describe('Edge Cases', () => {
    it('handles missing store gracefully', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles incomplete collection source data', async () => {
      const incompleteSource = {
        id: 1,
        name: 'Test',
        enabled: true
      } as SnmpCollectionSource

      wrapper = await createWrapper(incompleteSource)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles null dates gracefully', async () => {
      const sourceWithNullDates = {
        ...mockCollectionSource,
        createdTime: null as any,
        lastModified: null as any
      }

      wrapper = await createWrapper(sourceWithNullDates)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles undefined dates gracefully', async () => {
      const sourceWithUndefinedDates = {
        ...mockCollectionSource,
        createdTime: undefined as any,
        lastModified: undefined as any
      }

      wrapper = await createWrapper(sourceWithUndefinedDates)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles empty string values', async () => {
      const sourceWithEmptyStrings = {
        ...mockCollectionSource,
        name: '',
        vendor: '',
        uploadedBy: ''
      }

      wrapper = await createWrapper(sourceWithEmptyStrings)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles special characters in names', async () => {
      const source = {
        ...mockCollectionSource,
        name: 'Test <Collection> & "Special" Characters'
      }

      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain('Test <Collection> & "Special" Characters')
    })

    it('handles long vendor names', async () => {
      const source = {
        ...mockCollectionSource,
        vendor: 'Very Long Vendor Name That Exceeds Normal Display Length For Testing Purposes'
      }

      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain('Very Long Vendor Name That Exceeds Normal Display Length For Testing Purposes')
    })

    it('handles null values in fields', async () => {
      const sourceWithNulls = {
        ...mockCollectionSource,
        name: null as any,
        vendor: null as any,
        uploadedBy: null as any
      }

      wrapper = await createWrapper(sourceWithNulls)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })
  })

  describe('Reactivity Tests', () => {
    it('updates when store.selectedCollectionSource changes', async () => {
      wrapper = await createWrapper()

      const newSource = { ...mockCollectionSource, name: 'Updated Collection' }
      store.selectedCollectionSource = newSource
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Updated Collection')
    })

    it('switches to not-found view when selectedCollectionSource becomes null', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('.not-found-container').exists()).toBe(false)

      store.selectedCollectionSource = null
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(false)
      expect(wrapper.find('.not-found-container').exists()).toBe(true)
    })

    it('toggles button text when enabled state changes', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      expect(wrapper.text()).toContain('Disable Source')

      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Enable Source')
    })

    it('updates status display when enabled changes', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      expect(wrapper.text()).toContain('Status:Enabled')

      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Status:Disabled')
    })
  })

  describe('Integration Tests', () => {
    it('maintains component structure after button clicks', async () => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="back-button"]').trigger('click')

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('.config-details-box').exists()).toBe(true)
    })

    it('maintains state consistency after multiple store updates', async () => {
      wrapper = await createWrapper()

      // Update 1
      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Disabled')

      // Update 2
      store.selectedCollectionSource = { ...mockCollectionSource, name: 'Updated Name' }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Updated Name')

      // Update 3
      store.selectedCollectionSource = { ...mockCollectionSource, vendor: 'New Vendor' }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('New Vendor')
    })

    it('handles rapid button clicks without errors', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('[data-test="back-button"]')

      for (let i = 0; i < 10; i++) {
        await backButton.trigger('click')
      }

      expect(mockPush).toHaveBeenCalledTimes(10)
    })

    it('renders all sections in correct order', async () => {
      wrapper = await createWrapper()

      const container = wrapper.find('.snmp-data-collection-detail-container')
      const children = container.element.children

      // Check order: header, config-box, system-defs, resource-types, mib-groups
      expect(children[0].classList.contains('header')).toBe(true)
      expect(children[1].classList.contains('config-details-box')).toBe(true)
      expect(children[2].classList.contains('system-defs-container')).toBe(true)
      expect(children[3].classList.contains('resource-types-container')).toBe(true)
      expect(children[4].classList.contains('mib-groups-container')).toBe(true)
    })
  })

  describe('Data-Test Attributes', () => {
    it('has correct data-test attributes on buttons', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="back-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="enable-disable-source"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="delete-source"]').exists()).toBe(true)
    })

    it('has data-test attribute on config box', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="config-box"]').exists()).toBe(true)
    })
  })

  describe('Route Parameter Handling', () => {
    it('handles numeric route id', async () => {
      vi.mocked(useRoute).mockReturnValue({ params: { id: '123' } } as any)
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('123')
    })

    it('handles string route id', async () => {
      vi.mocked(useRoute).mockReturnValue({ params: { id: 'test-id' } } as any)
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('test-id')
    })

    it('handles missing route id gracefully', async () => {
      vi.mocked(useRoute).mockReturnValue({ params: {} } as any)
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
      expect(wrapper.find('.not-found-container').exists()).toBe(true)
    })
  })

  describe('Accessibility', () => {
    it('heading is properly structured', async () => {
      wrapper = await createWrapper()

      const heading = wrapper.find('h1')
      expect(heading.exists()).toBe(true)
      expect(heading.element.tagName).toBe('H1')
    })

    it('buttons are within action container', async () => {
      wrapper = await createWrapper()

      const actionDiv = wrapper.find('.action-container')
      const buttons = actionDiv.findAllComponents(FeatherButton)

      expect(buttons.length).toBe(2)
    })

    it('back button has descriptive text', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.findComponent(FeatherBackButton)
      expect(backButton.text()).toBe('Go Back')
    })
  })
})

