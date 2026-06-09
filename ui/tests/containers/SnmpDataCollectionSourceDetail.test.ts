import MibGroupsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/MibGroupsTable.vue'
import ResourceTypesTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/ResourceTypesTable.vue'
import SystemDefinitionsTable from '@/components/SnmpDataCollection/SnmpDataCollectionSourceDetail/SystemDefinitionsTable.vue'
import SnmpDataCollectionSourceDetail from '@/containers/SnmpDataCollectionSourceDetail.vue'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { FeatherBackButton } from '@featherds/back-button'
import { FeatherButton } from '@featherds/button'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { format } from 'date-fns-tz'
import { setActivePinia } from 'pinia'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useRoute } from 'vue-router'

const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRoute: vi.fn(() => mockUseRoute()),
  useRouter: vi.fn(() => ({
    push: mockPush
  }))
}))
const mockUseRoute = vi.fn(() => ({
  params: { id: '1' }
}))

const mockDeleteSnmpCollectionSources = vi.fn()
const mockEnableDisableSnmpDataCollectionSources = vi.fn()
vi.mock('@/services/snmpDataCollectionService', () => ({
  deleteSnmpCollectionSources: (...args: any[]) => mockDeleteSnmpCollectionSources(...args),
  enableDisableSnmpDataCollectionSources: (...args: any[]) => mockEnableDisableSnmpDataCollectionSources(...args),
  updateDataCollectionProfile: vi.fn().mockResolvedValue(true)
}))

const mockShowSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({
    showSnackBar: mockShowSnackBar,
    hideSnackbar: vi.fn(),
    isDisplayed: { value: false },
    isCentered: { value: false },
    hasError: { value: false },
    message: { value: '' },
    setTimeout: vi.fn()
  })
}))

describe('SnmpDataCollectionSourceDetail.vue', () => {
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
    MibGroupsTable: true,
    DeleteConfirmationDialog: true,
    SnmpDataCollectionChangeStatusDialog: true
  }

  beforeEach(() => {
    setActivePinia(createTestingPinia())
    vi.clearAllMocks()
    mockUseRoute.mockReturnValue({ params: { id: '1' }})
    store = useSnmpDataCollectionDetailStore()
    const sourcesStore = useSnmpDataCollectionStore()
    sourcesStore.profilesForSource = vi.fn().mockReturnValue([])
    sourcesStore.fetchSnmpCollectionProfiles = vi.fn().mockResolvedValue(undefined)
    sourcesStore.fetchAllSourcesNames = vi.fn().mockResolvedValue(undefined)
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

    wrapper = mount(SnmpDataCollectionSourceDetail, {
      global: {
        stubs: globalStubs
      }
    })
    await wrapper.vm.$nextTick()
    await flushPromises()
    return wrapper
  }

  describe('Component Rendering', () => {
    it('renders detail container when selectedCollectionSource exists', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('.not-found-container').exists()).toBe(false)
    })

    it('renders heading with source name followed by Source Details', async () => {
      wrapper = await createWrapper()

      const heading = wrapper.find('h1')
      expect(heading.exists()).toBe(true)
      expect(heading.element.tagName).toBe('H1')
      expect(heading.text()).toContain('Source Details')
    })

    it('renders heading with source name in plain format', async () => {
      const source = { ...mockCollectionSource, name: 'test collection' }
      wrapper = await createWrapper(source)

      const heading = wrapper.find('h1')
      expect(heading.text()).toContain('Source Details for test collection')
    })

    it('renders config-details-box header with Source Details text', async () => {
      wrapper = await createWrapper()

      const configBox = wrapper.find('.config-details-box')
      const header = configBox.find('.header')
      expect(header.exists()).toBe(true)
      expect(header.text()).toBe('Source Details')
    })

    it('renders three tab labels in correct order', async () => {
      wrapper = await createWrapper()

      const tabContainer = wrapper.find('.tab-container')
      expect(tabContainer.exists()).toBe(true)
      expect(tabContainer.text()).toContain('Resource Types')
      expect(tabContainer.text()).toContain('MIB Groups')
      expect(tabContainer.text()).toContain('System Definitions')
    })

    it('renders back button with Go Back text', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.findComponent(FeatherBackButton)
      expect(backButton.exists()).toBe(true)
      expect(backButton.text()).toBe('Go Back')
    })

    it('renders config details box', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="config-box"]').exists()).toBe(true)
    })

    it('renders all table components inside tab panels', async () => {
      wrapper = await createWrapper()

      expect(wrapper.findComponent(SystemDefinitionsTable).exists()).toBe(true)
      expect(wrapper.findComponent(ResourceTypesTable).exists()).toBe(true)
      expect(wrapper.findComponent(MibGroupsTable).exists()).toBe(true)
    })

    it('renders DeleteConfirmationDialog', async () => {
      wrapper = await createWrapper()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('renders correct CSS structure', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('.header').exists()).toBe(true)
      expect(wrapper.find('.title-container').exists()).toBe(true)
      expect(wrapper.find('.action-container').exists()).toBe(true)
      expect(wrapper.find('.config-details-box').exists()).toBe(true)
      expect(wrapper.find('.tab-container').exists()).toBe(true)
    })

    it('renders three config rows with two fields each', async () => {
      wrapper = await createWrapper()

      const configRows = wrapper.findAll('.config-row')
      expect(configRows.length).toBe(3)
      configRows.forEach((row) => {
        expect(row.findAll('.config-field').length).toBe(2)
      })
    })

    it('has correct data-test attributes on interactive elements', async () => {
      wrapper = await createWrapper()

      expect(wrapper.find('[data-test="back-button"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="delete-source"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="config-box"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="status-tag"]').exists()).toBe(true)
    })

    it('renders delete source button with correct text', async () => {
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      expect(deleteButton.exists()).toBe(true)
      expect(deleteButton.text()).toBe('Delete Source')
    })
  })

  describe('Enabled/Disabled State', () => {
    it.each([
      {
        enabled: true,
        shownButton: 'disable-source',
        hiddenButton: 'enable-source',
        chipClass: 'enabled-tag',
        hiddenChipClass: 'disabled-tag',
        statusText: 'Enabled',
        buttonText: 'Disable Source'
      },
      {
        enabled: false,
        shownButton: 'enable-source',
        hiddenButton: 'disable-source',
        chipClass: 'disabled-tag',
        hiddenChipClass: 'enabled-tag',
        statusText: 'Disabled',
        buttonText: 'Enable Source'
      }
    ])(
      'renders correct button, chip, and status when enabled=$enabled',
      async ({ enabled, shownButton, hiddenButton, chipClass, hiddenChipClass, buttonText }) => {
        const source = { ...mockCollectionSource, enabled }
        wrapper = await createWrapper(source)

        // Action button
        expect(wrapper.find(`[data-test="${shownButton}"]`).exists()).toBe(true)
        expect(wrapper.find(`[data-test="${hiddenButton}"]`).exists()).toBe(false)
        expect(wrapper.find(`[data-test="${shownButton}"]`).text()).toBe(buttonText)

        // Chip tag
        expect(wrapper.find(`.${chipClass}`).exists()).toBe(true)
        expect(wrapper.find(`.${hiddenChipClass}`).exists()).toBe(false)
      }
    )

    it('renders two action buttons (enable/disable + delete) in action container', async () => {
      wrapper = await createWrapper()

      const actionDiv = wrapper.find('.action-container')
      const buttons = actionDiv.findAllComponents(FeatherButton)
      expect(buttons.length).toBe(2)
    })

    it('renders Enable Source button with primary style when disabled', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      expect(enableButton.exists()).toBe(true)
      expect(enableButton.text()).toBe('Enable Source')
    })

    it('renders Disable Source button when enabled', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      expect(disableButton.exists()).toBe(true)
      expect(disableButton.text()).toBe('Disable Source')
    })

    it('renders Delete Source button always present', async () => {
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      expect(deleteButton.exists()).toBe(true)
      expect(deleteButton.text()).toBe('Delete Source')
    })
  })

  describe('Data Display', () => {
    it('displays all field labels and values correctly', async () => {
      wrapper = await createWrapper()

      const configBox = wrapper.find('.config-details-box')

      // Labels
      const expectedLabels = ['Source:', 'Uploaded By:', 'Creation Date:', 'Last Modified Date:']
      expectedLabels.forEach((label) => {
        expect(configBox.text()).toContain(label)
      })

      // Values
      expect(configBox.text()).toContain('Test Collection')
      expect(configBox.text()).toContain('test-user')
      expect(configBox.text()).toContain(format(mockCollectionSource.createdTime, 'MM/dd/yyyy'))
      expect(configBox.text()).toContain(format(mockCollectionSource.lastModified, 'MM/dd/yyyy'))
    })

    it.each([
      { date: new Date('2024-01-15') },
      { date: new Date('2024-12-31') },
      { date: new Date('2023-03-20') }
    ])('formats date $date correctly', async ({ date }) => {
      const source = { ...mockCollectionSource, createdTime: date, lastModified: date }
      wrapper = await createWrapper(source)

      const expected = format(date, 'MM/dd/yyyy')
      expect(wrapper.text()).toContain(expected)
    })

    it.each([
      { name: 'Simple Collection', uploadedBy: 'user1' },
      { name: 'Complex Collection Name', uploadedBy: 'admin' }
    ])('displays dynamic data: name=$name', async ({ name, uploadedBy }) => {
      const source = { ...mockCollectionSource, name, uploadedBy }
      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain(name)
      expect(wrapper.text()).toContain(uploadedBy)
    })
  })

  describe('Navigation', () => {
    it('navigates back when Go Back button is clicked', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.get('[data-test="back-button"]')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('handles multiple back button clicks', async () => {
      wrapper = await createWrapper()

      const backButton = wrapper.find('[data-test="back-button"]')
      await backButton.trigger('click')
      await backButton.trigger('click')
      await backButton.trigger('click')

      expect(mockPush).toHaveBeenCalledTimes(3)
    })

    it('navigates back from not found page', async () => {
      wrapper = await createWrapper(null)

      // In the not-found state the detail container is hidden, so the only
      // FeatherButton rendered is the Go Back button inside .not-found-container
      const goBackButton = wrapper.findComponent(FeatherButton)
      await goBackButton.trigger('click')

      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })
  })

  describe('Not Found State', () => {
    it('shows not-found container when selectedCollectionSource is null', async () => {
      wrapper = await createWrapper(null)

      expect(wrapper.find('.not-found-container').exists()).toBe(true)
      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(false)
      expect(wrapper.text()).toContain('No data found.')
    })

    it('hides all table components in not-found state', async () => {
      wrapper = await createWrapper(null)

      expect(wrapper.findComponent(SystemDefinitionsTable).exists()).toBe(false)
      expect(wrapper.findComponent(ResourceTypesTable).exists()).toBe(false)
      expect(wrapper.findComponent(MibGroupsTable).exists()).toBe(false)
    })

    it('still renders DeleteConfirmationDialog when source is null', async () => {
      wrapper = await createWrapper(null)

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.exists()).toBe(true)
    })
  })

  describe('Store Interactions', () => {
    it('fetches collection source on mount when route has id', async () => {
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionSourceDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledOnce()
      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('1')
    })

    it.each([
      { id: '123', description: 'numeric route id' },
      { id: 'test-id', description: 'string route id' },
      { id: 'uuid-1234-5678', description: 'UUID-style route id' }
    ])('passes $description to fetchCollectionSourceById', async ({ id }) => {
      mockUseRoute.mockReturnValue({ params: { id }})
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionSourceDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith(id)
    })

    it('does not fetch when route id is missing', async () => {
      vi.mocked(useRoute).mockReturnValue({ params: {}} as any)
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionSourceDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
      expect(wrapper.find('.not-found-container').exists()).toBe(true)
    })

    it.each([
      { id: null, description: 'null' },
      { id: undefined, description: 'undefined' }
    ])('does not fetch when route id is $description', async ({ id }) => {
      mockUseRoute.mockReturnValue({ params: { id: String(id) }})
      store.fetchCollectionSourceById = vi.fn()

      wrapper = mount(SnmpDataCollectionSourceDetail, {
        global: {
          stubs: globalStubs
        }
      })
      await flushPromises()

      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
    })
  })

  describe('Reactivity', () => {
    it('updates displayed data when store.selectedCollectionSource changes', async () => {
      wrapper = await createWrapper()

      const newSource = { ...mockCollectionSource, name: 'Updated Collection' }
      store.selectedCollectionSource = newSource
      await wrapper.vm.$nextTick()

      expect(wrapper.text()).toContain('Updated Collection')
    })

    it('switches to not-found view when selectedCollectionSource becomes null', async () => {
      wrapper = await createWrapper()
      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)

      store.selectedCollectionSource = null
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(false)
      expect(wrapper.find('.not-found-container').exists()).toBe(true)
    })

    it('toggles enable/disable button and chip when enabled state changes', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      expect(wrapper.find('[data-test="disable-source"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="enable-source"]').exists()).toBe(false)
      expect(wrapper.find('.enabled-tag').exists()).toBe(true)

      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()

      expect(wrapper.find('[data-test="enable-source"]').exists()).toBe(true)
      expect(wrapper.find('[data-test="disable-source"]').exists()).toBe(false)
      expect(wrapper.find('.disabled-tag').exists()).toBe(true)
      expect(wrapper.find('.enabled-tag').exists()).toBe(false)
    })

    it('updates status display when enabled changes', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })
      expect(wrapper.find('.enabled-tag').exists()).toBe(true)
      expect(wrapper.find('.disabled-tag').exists()).toBe(false)

      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('.disabled-tag').exists()).toBe(true)
      expect(wrapper.find('.enabled-tag').exists()).toBe(false)
    })

    it('maintains state consistency after multiple store updates', async () => {
      wrapper = await createWrapper()

      store.selectedCollectionSource = { ...mockCollectionSource, enabled: false }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Disabled')

      store.selectedCollectionSource = { ...mockCollectionSource, name: 'Updated Name' }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('Updated Name')

      store.selectedCollectionSource = { ...mockCollectionSource, uploadedBy: 'new-user' }
      await wrapper.vm.$nextTick()
      expect(wrapper.text()).toContain('new-user')
    })

    it('updates heading when name changes', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, name: 'first name' })
      expect(wrapper.find('h1').text()).toContain('Source Details for first name')

      store.selectedCollectionSource = { ...mockCollectionSource, name: 'second name' }
      await wrapper.vm.$nextTick()
      expect(wrapper.find('h1').text()).toContain('Source Details for second name')
    })

    it('recovers from not-found back to detail view when source is set', async () => {
      wrapper = await createWrapper(null)
      expect(wrapper.find('.not-found-container').exists()).toBe(true)

      store.selectedCollectionSource = mockCollectionSource
      await wrapper.vm.$nextTick()

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      expect(wrapper.find('.not-found-container').exists()).toBe(false)
    })
  })

  describe('Delete Dialog - State Management', () => {
    it('dialog is initially hidden', async () => {
      wrapper = await createWrapper()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.attributes('visible')).toBe('false')
    })

    it('opens delete dialog when Delete Source button is clicked', async () => {
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.attributes('visible')).toBe('true')
    })

    it('passes correct type and selected source to dialog', async () => {
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.attributes('type')).toBe('source')
      expect(dialog.attributes('selected')).toBeDefined()
    })

    it('closes dialog and resets selected on close event', async () => {
      wrapper = await createWrapper()

      // Open dialog first
      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      // Close dialog
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(dialog.attributes('visible')).toBe('false')
    })

    it('can open and close dialog multiple times', async () => {
      wrapper = await createWrapper()
      const deleteButton = wrapper.find('[data-test="delete-source"]')
      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })

      for (let i = 0; i < 3; i++) {
        await deleteButton.trigger('click')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('true')

        dialog.vm.$emit('close')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('false')
      }
    })
  })

  describe('Delete Collection Source - Successful Deletion', () => {
    beforeEach(() => {
      mockDeleteSnmpCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
      mockPush.mockClear()
    })

    it('calls service, shows success snackbar, and navigates on confirmed deletion', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([1])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Collection Source \'Test Collection\' deleted successfully.'
      })
      expect(mockPush).toHaveBeenCalledWith({ name: 'SNMP Data Collection' })
    })

    it('uses correct source id for delete service call', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)
      const customSource: SnmpCollectionSource = { ...mockCollectionSource, id: 42 }
      wrapper = await createWrapper(customSource)

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 42, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([42])
    })
  })

  describe('Delete Collection Source - Failed Deletion', () => {
    beforeEach(() => {
      mockDeleteSnmpCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
      mockPush.mockClear()
    })

    it('shows error snackbar and does not navigate when service returns false', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Test Collection\'.',
        error: true
      })
      expect(mockPush).not.toHaveBeenCalled()
    })

    it('keeps dialog visible when deletion fails', async () => {
      mockDeleteSnmpCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper()

      const deleteButton = wrapper.find('[data-test="delete-source"]')
      await deleteButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      // Dialog should remain visible on failure (no close call)
      expect(dialog.attributes('visible')).toBe('true')
    })
  })

  describe('Delete Collection Source - Validation Failures', () => {
    beforeEach(() => {
      mockDeleteSnmpCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
      mockPush.mockClear()
    })

    it.each([
      {
        selected: { id: 1, name: 'Test Collection' },
        type: 'wrong-type',
        desc: 'wrong type'
      },
      {
        selected: { id: 999, name: 'Test Collection' },
        type: 'source',
        desc: 'mismatched id'
      },
      {
        selected: { id: 1, name: 'Different Name' },
        type: 'source',
        desc: 'mismatched name'
      },
      {
        selected: null,
        type: 'source',
        desc: 'null selected'
      },
      {
        selected: { name: 'Test Collection' } as any,
        type: 'source',
        desc: 'missing id'
      }
    ])('rejects deletion with $desc and does not navigate', async ({ selected, type }) => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', selected, type)
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
      expect(mockPush).not.toHaveBeenCalled()
    })

    it('rejects deletion when store source changed after dialog opened (race condition)', async () => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      // Simulate store source changing after dialog opened
      store.selectedCollectionSource = {
        ...mockCollectionSource,
        id: 999,
        name: 'Different Source'
      }
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
    })

    it('rejects deletion when store source becomes null after dialog opened', async () => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      store.selectedCollectionSource = null
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).not.toHaveBeenCalled()
    })

    it('shows validation error snackbar with selected name (not store name)', async () => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 999, name: 'Wrong Name' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'Wrong Name\'.',
        error: true
      })
    })

    it('shows validation error snackbar with selected?.name when selected is null', async () => {
      wrapper = await createWrapper()

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', null, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to delete Collection Source \'undefined\'.',
        error: true
      })
    })
  })

  describe('Change Status Dialog - State Management', () => {
    beforeEach(() => {
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('change status dialog is initially hidden', async () => {
      wrapper = await createWrapper()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
      expect(dialog.attributes('visible')).toBe('false')
    })

    it('opens change status dialog when Disable Source button is clicked', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')
    })

    it('passes correct props to change status dialog when disabling', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('type')).toBe('source')
      expect(dialog.attributes('status')).toBe('Disable')
      expect(dialog.attributes('selected')).toBeDefined()
    })

    it('passes correct status prop based on current enabled state - disabled source', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      // Dialog should show "Enable" when source is disabled (selectedCollectionSource is null initially)
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      // When selectedCollectionSource ref is null, enabled is undefined, so status is 'Enable'
      expect(dialog.attributes('status')).toBe('Enable')
    })

    it('passes correct status prop when dialog is opened for enabled source', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      // Click disable button to populate selectedCollectionSource ref
      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      // Dialog should show "Disable" when source is enabled
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('status')).toBe('Disable')
    })

    it('closes dialog and resets selected on close event', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('close')
      await wrapper.vm.$nextTick()

      expect(dialog.attributes('visible')).toBe('false')
    })

    it('can open and close change status dialog multiple times', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })
      const disableButton = wrapper.find('[data-test="disable-source"]')
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })

      for (let i = 0; i < 3; i++) {
        await disableButton.trigger('click')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('true')

        dialog.vm.$emit('close')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('false')
      }
    })

    it('renders change status dialog when source is null', async () => {
      wrapper = await createWrapper(null)

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.exists()).toBe(true)
    })

    it('opens change status dialog when Enable Source button is clicked', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')
    })

    it('passes correct props to change status dialog when enabling', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('type')).toBe('source')
      expect(dialog.attributes('status')).toBe('Enable')
      expect(dialog.attributes('selected')).toBeDefined()
    })
  })

  describe('Change Collection Source Status - Successful Status Change', () => {
    beforeEach(() => {
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('calls service with correct params when disabling source', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      // When source is enabled, disabling it sends false
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [1])
    })

    it('shows success snackbar when disable succeeds', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Collection Source \'Test Collection\' disabled successfully.'
      })
    })

    it('refreshes collection source data after successful status change', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('1')
    })

    it('closes dialog after successful status change', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(dialog.attributes('visible')).toBe('false')
    })

    it('uses correct source id for status change service call', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      const customSource: SnmpCollectionSource = { ...mockCollectionSource, id: 99, enabled: true }
      wrapper = await createWrapper(customSource)

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 99, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [99])
    })

    it('calls service with correct params when enabling source', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      // When source is disabled, enabling it sends true
      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [1])
    })

    it('shows success snackbar when enable succeeds', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Collection Source \'Test Collection\' enabled successfully.'
      })
    })

    it('refreshes collection source data after successful enable', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(store.fetchCollectionSourceById).toHaveBeenCalledWith('1')
    })

    it('closes dialog after successful enable', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(dialog.attributes('visible')).toBe('false')
    })

    it('uses correct source id when enabling', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)
      const customSource: SnmpCollectionSource = { ...mockCollectionSource, id: 77, enabled: false }
      wrapper = await createWrapper(customSource)

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 77, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [77])
    })
  })

  describe('Change Collection Source Status - Failed Status Change', () => {
    beforeEach(() => {
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it('shows error snackbar when service returns false', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to disable Collection Source \'Test Collection\'.',
        error: true
      })
    })

    it('does not refresh source data when status change fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      store.fetchCollectionSourceById = vi.fn()
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      // fetchCollectionSourceById should not be called on failure
      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
    })

    it('keeps dialog visible when status change fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      const disableButton = wrapper.find('[data-test="disable-source"]')
      await disableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      // Dialog should remain visible on failure
      expect(dialog.attributes('visible')).toBe('true')
    })

    it('shows error snackbar when enable service returns false', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to enable Collection Source \'Test Collection\'.',
        error: true
      })
    })

    it('does not refresh source data when enable fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      store.fetchCollectionSourceById = vi.fn()
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(store.fetchCollectionSourceById).not.toHaveBeenCalled()
    })

    it('keeps dialog visible when enable fails', async () => {
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(false)
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })

      const enableButton = wrapper.find('[data-test="enable-source"]')
      await enableButton.trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      expect(dialog.attributes('visible')).toBe('true')

      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(dialog.attributes('visible')).toBe('true')
    })
  })

  describe('Change Collection Source Status - Validation Failures', () => {
    beforeEach(() => {
      mockEnableDisableSnmpDataCollectionSources.mockClear()
      mockShowSnackBar.mockClear()
    })

    it.each([
      {
        selected: { id: 1, name: 'Test Collection' },
        type: 'wrong-type',
        desc: 'wrong type'
      },
      {
        selected: { id: 999, name: 'Test Collection' },
        type: 'source',
        desc: 'mismatched id'
      },
      {
        selected: { id: 1, name: 'Different Name' },
        type: 'source',
        desc: 'mismatched name'
      },
      {
        selected: null,
        type: 'source',
        desc: 'null selected'
      },
      {
        selected: { name: 'Test Collection' } as any,
        type: 'source',
        desc: 'missing id'
      }
    ])('rejects status change with $desc and does not call service', async ({ selected, type }) => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      await wrapper.find('[data-test="disable-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', selected, type)
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
      expect(mockShowSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })

    it('shows validation error snackbar with selected name', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      await wrapper.find('[data-test="disable-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 999, name: 'Wrong Name' }, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to change status for Collection Source \'Wrong Name\'.',
        error: true
      })
    })

    it('shows validation error when selected is null', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      await wrapper.find('[data-test="disable-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', null, 'source')
      await flushPromises()

      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: 'Failed to change status for Collection Source \'undefined\'.',
        error: true
      })
    })

    it('rejects status change when local selectedCollectionSource is null', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: true })

      // Don't click the disable button (so selectedCollectionSource ref stays null)
      // Emit confirm directly
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: 'Test Collection' }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).not.toHaveBeenCalled()
    })
  })

  describe('Edge Cases', () => {
    it('handles null dates gracefully', async () => {
      const source = {
        ...mockCollectionSource,
        createdTime: null as any,
        lastModified: null as any
      }
      wrapper = await createWrapper(source)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
    })

    it('handles undefined dates gracefully', async () => {
      const source = {
        ...mockCollectionSource,
        createdTime: undefined as any,
        lastModified: undefined as any
      }
      wrapper = await createWrapper(source)

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

    it('handles unicode/international characters', async () => {
      const source = {
        ...mockCollectionSource,
        name: '测试集合 日本語 العربية'
      }
      wrapper = await createWrapper(source)

      expect(wrapper.text()).toContain('测试集合 日本語 العربية')
    })

    it('handles XSS strings safely via text interpolation', async () => {
      const source = {
        ...mockCollectionSource,
        name: '<script>alert("xss")</script>'
      }
      wrapper = await createWrapper(source)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      // Vue {{ }} treats content as text, not HTML
      expect(wrapper.text()).toContain('<script>alert("xss")</script>')
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

    it('handles deletion with special characters in source name', async () => {
      const specialName = 'Test <Source> & "Quotes"'
      const specialSource: SnmpCollectionSource = { ...mockCollectionSource, name: specialName }
      mockDeleteSnmpCollectionSources.mockResolvedValue(true)

      wrapper = await createWrapper(specialSource)

      await wrapper.find('[data-test="delete-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'DeleteConfirmationDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: specialName }, 'source')
      await flushPromises()

      expect(mockDeleteSnmpCollectionSources).toHaveBeenCalledWith([1])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Collection Source '${specialName}' deleted successfully.`
      })
    })

    it('handles enabling source with special characters in name', async () => {
      const specialName = 'Test <Source> & "Quotes"'
      const specialSource: SnmpCollectionSource = { ...mockCollectionSource, name: specialName, enabled: false }
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)

      wrapper = await createWrapper(specialSource)

      await wrapper.find('[data-test="enable-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: specialName }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(true, [1])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Collection Source '${specialName}' enabled successfully.`
      })
    })

    it('handles disabling source with special characters in name', async () => {
      const specialName = 'Test <Source> & "Quotes"'
      const specialSource: SnmpCollectionSource = { ...mockCollectionSource, name: specialName, enabled: true }
      mockEnableDisableSnmpDataCollectionSources.mockResolvedValue(true)
      store.fetchCollectionSourceById = vi.fn().mockResolvedValue(undefined)

      wrapper = await createWrapper(specialSource)

      await wrapper.find('[data-test="disable-source"]').trigger('click')
      await wrapper.vm.$nextTick()

      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })
      dialog.vm.$emit('confirm', { id: 1, name: specialName }, 'source')
      await flushPromises()

      expect(mockEnableDisableSnmpDataCollectionSources).toHaveBeenCalledWith(false, [1])
      expect(mockShowSnackBar).toHaveBeenCalledWith({
        msg: `Collection Source '${specialName}' disabled successfully.`
      })
    })

    it('can open and close change status dialog multiple times with Enable button', async () => {
      wrapper = await createWrapper({ ...mockCollectionSource, enabled: false })
      const enableButton = wrapper.find('[data-test="enable-source"]')
      const dialog = wrapper.findComponent({ name: 'SnmpDataCollectionChangeStatusDialog' })

      for (let i = 0; i < 3; i++) {
        await enableButton.trigger('click')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('true')

        dialog.vm.$emit('close')
        await wrapper.vm.$nextTick()
        expect(dialog.attributes('visible')).toBe('false')
      }
    })

    it('handles future dates', async () => {
      const source = {
        ...mockCollectionSource,
        createdTime: new Date('2030-12-31'),
        lastModified: new Date('2030-12-31')
      }
      wrapper = await createWrapper(source)

      const expected = format(source.createdTime, 'MM/dd/yyyy')
      expect(wrapper.text()).toContain(expected)
    })

    it('handles very old dates', async () => {
      const source = {
        ...mockCollectionSource,
        createdTime: new Date('1990-01-01'),
        lastModified: new Date('1990-01-01')
      }
      wrapper = await createWrapper(source)

      const expected = format(source.createdTime, 'MM/dd/yyyy')
      expect(wrapper.text()).toContain(expected)
    })

    it('mounts and unmounts without errors', async () => {
      wrapper = await createWrapper()

      expect(wrapper.vm).toBeDefined()
      expect(wrapper.element).toBeInstanceOf(HTMLElement)
      expect(() => wrapper.unmount()).not.toThrow()
    })

    it('handles empty string values in all fields', async () => {
      const source: SnmpCollectionSource = {
        ...mockCollectionSource,
        name: '',
        vendor: '',
        uploadedBy: '',
        description: ''
      }
      wrapper = await createWrapper(source)

      expect(wrapper.find('.snmp-data-collection-detail-container').exists()).toBe(true)
      // capitalize('') returns ''
      expect(wrapper.find('h1').text()).toContain('Source Details')
    })

    it('handles same createdTime and lastModified dates', async () => {
      const sameDate = new Date('2024-06-15')
      const source = { ...mockCollectionSource, createdTime: sameDate, lastModified: sameDate }
      wrapper = await createWrapper(source)

      const configBox = wrapper.find('.config-details-box')
      const text = configBox.text()
      // Both dates should appear (even if the same value)
      const expected = format(sameDate, 'MM/dd/yyyy')
      expect(text).toContain(expected)
    })

    it('renders field-label and field-value spans in each data config field', async () => {
      wrapper = await createWrapper()

      const configFields = wrapper.findAll('.config-field')
      expect(configFields.length).toBe(6)
      // First 4 data fields (Source, Uploaded By, Creation Date, Last Modified) have both label and value
      configFields.slice(0, 4).forEach((field: any) => {
        expect(field.find('.field-label').exists()).toBe(true)
        expect(field.find('.field-value').exists()).toBe(true)
      })
    })
  })
})
