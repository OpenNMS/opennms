import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useEventConfigStore } from '@/stores/eventConfigStore'
import {
  changeEventConfigSourceStatus,
  filterEventConfigSources,
  getAllSourceNames
} from '@/services/eventConfigService'
import { EventConfigSource } from '@/types/eventConfig'

vi.mock('@/services/eventConfigService', () => ({
  changeEventConfigSourceStatus: vi.fn(),
  filterEventConfigSources: vi.fn(),
  getAllSourceNames: vi.fn()
}))

describe('useEventConfigStore', () => {
  let store: ReturnType<typeof useEventConfigStore>

  const mockSourceNames = ['Source1', 'Source2', 'Source3']

  const mockSources: EventConfigSource[] = [
    {
      id: 1,
      name: 'Test Source 1',
      vendor: 'Vendor A',
      description: 'Description 1',
      enabled: true,
      eventCount: 5,
      fileOrder: 1,
      uploadedBy: 'user1',
      createdTime: new Date('2024-01-01'),
      lastModified: new Date('2024-01-02')
    },
    {
      id: 2,
      name: 'Test Source 2',
      vendor: 'Vendor B',
      description: 'Description 2',
      enabled: false,
      eventCount: 10,
      fileOrder: 2,
      uploadedBy: 'user2',
      createdTime: new Date('2024-01-03'),
      lastModified: new Date('2024-01-04')
    }
  ]

  const mockFilterResponse = {
    sources: mockSources,
    totalRecords: 2
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useEventConfigStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  it('should have correct initial state', () => {
    expect(store.sources).toEqual([])
    expect(store.sourcesPagination).toEqual({
      page: 1,
      pageSize: 10,
      total: 0
    })
    expect(store.sourcesSearchTerm).toBe('')
    expect(store.sourcesSorting).toEqual({
      sortOrder: 'desc',
      sortKey: 'createdTime'
    })
    expect(store.isLoading).toBe(false)
    expect(store.activeTab).toBe(0)
    expect(store.uploadedSourceNames).toEqual([])
    expect(store.uploadedEventConfigFilesReportDialogState.visible).toBe(false)
    expect(store.deleteEventConfigSourceDialogState).toEqual({
      visible: false,
      eventConfigSource: null
    })
    expect(store.changeEventConfigSourceStatusDialogState).toEqual({
      visible: false,
      eventConfigSource: null
    })
  })

  it('should fetch all source names successfully', async () => {
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.fetchAllSourcesNames()

    expect(getAllSourceNames).toHaveBeenCalledTimes(1)
    expect(store.uploadedSourceNames).toEqual(mockSourceNames)
    expect(store.isLoading).toBe(false)
  })

  it('should handle errors when fetching source names', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const error = new Error('Failed to fetch source names')
    vi.mocked(getAllSourceNames).mockRejectedValue(error)

    await store.fetchAllSourcesNames()

    expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching all event configuration source names:', error)
    expect(store.isLoading).toBe(false)
    consoleErrorSpy.mockRestore()
  })

  it('should set loading state correctly during fetch', async () => {
    vi.mocked(getAllSourceNames).mockImplementation(
      () =>
        new Promise((resolve) => {
          expect(store.isLoading).toBe(true)
          resolve(mockSourceNames)
        })
    )

    await store.fetchAllSourcesNames()
    expect(store.isLoading).toBe(false)
  })

  it('should fetch event configs successfully', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.fetchEventConfigs()

    expect(filterEventConfigSources).toHaveBeenCalledWith(0, 10, '', 'createdTime', 'desc')
    expect(getAllSourceNames).toHaveBeenCalledTimes(1)
    expect(store.sources).toEqual(mockSources)
    expect(store.sourcesPagination.total).toBe(2)
    expect(store.uploadedSourceNames).toEqual(mockSourceNames)
    expect(store.isLoading).toBe(false)
  })

  it('should fetch with correct pagination parameters', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    store.sourcesPagination.page = 3
    store.sourcesPagination.pageSize = 20

    await store.fetchEventConfigs()

    expect(filterEventConfigSources).toHaveBeenCalledWith(40, 20, '', 'createdTime', 'desc')
  })

  it('should fetch with search term', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    store.sourcesSearchTerm = 'test search'

    await store.fetchEventConfigs()

    expect(filterEventConfigSources).toHaveBeenCalledWith(0, 10, 'test search', 'createdTime', 'desc')
  })

  it('should fetch with custom sorting', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    store.sourcesSorting = { sortKey: 'name', sortOrder: 'asc' }

    await store.fetchEventConfigs()

    expect(filterEventConfigSources).toHaveBeenCalledWith(0, 10, '', 'name', 'asc')
  })

  it('should handle errors when fetching event configs', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
    const error = new Error('Failed to fetch event configs')
    vi.mocked(filterEventConfigSources).mockRejectedValue(error)

    await store.fetchEventConfigs()

    expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching event configurations:', error)
    expect(store.isLoading).toBe(false)
    consoleErrorSpy.mockRestore()
  })

  it('should handle page change', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.onSourcePageChange(3)

    expect(store.sourcesPagination.page).toBe(3)
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  it('should handle page size change and reset page to 1', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    store.sourcesPagination.page = 5

    await store.onSourcePageSizeChange(25)

    expect(store.sourcesPagination.page).toBe(1)
    expect(store.sourcesPagination.pageSize).toBe(25)
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  it('should reset pagination', () => {
    store.sourcesPagination = { page: 5, pageSize: 25, total: 100 }

    store.resetSourcesPagination()

    expect(store.sourcesPagination).toEqual({
      page: 1,
      pageSize: 10,
      total: 0
    })
  })

  it('should handle search term change and reset page', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    store.sourcesPagination.page = 3

    await store.onChangeSourcesSearchTerm('new search')

    expect(store.sourcesSearchTerm).toBe('new search')
    expect(store.sourcesPagination.page).toBe(1)
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  it('should handle null search term', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.onChangeSourcesSearchTerm(null as any)

    expect(store.sourcesSearchTerm).toBe('')
  })

  it('should handle sort change', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.onSourcesSortChange('vendor', 'asc')

    expect(store.sourcesSorting.sortKey).toBe('vendor')
    expect(store.sourcesSorting.sortOrder).toBe('asc')
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  const mockSource = mockSources[0]

  it('should show delete dialog', () => {
    store.showDeleteEventConfigSourceModal(mockSource)

    expect(store.deleteEventConfigSourceDialogState.visible).toBe(true)
    expect(store.deleteEventConfigSourceDialogState.eventConfigSource).toEqual(mockSource)
  })

  it('should hide delete dialog', () => {
    store.deleteEventConfigSourceDialogState = {
      visible: true,
      eventConfigSource: mockSource
    }

    store.hideDeleteEventConfigSourceModal()

    expect(store.deleteEventConfigSourceDialogState.visible).toBe(false)
    expect(store.deleteEventConfigSourceDialogState.eventConfigSource).toBeNull()
  })

  it('should show change status dialog', () => {
    store.showChangeEventConfigSourceStatusDialog(mockSource)

    expect(store.changeEventConfigSourceStatusDialogState.visible).toBe(true)
    expect(store.changeEventConfigSourceStatusDialogState.eventConfigSource).toEqual(mockSource)
  })

  it('should hide change status dialog', () => {
    store.changeEventConfigSourceStatusDialogState = {
      visible: true,
      eventConfigSource: mockSource
    }

    store.hideChangeEventConfigSourceStatusDialog()

    expect(store.changeEventConfigSourceStatusDialogState.visible).toBe(false)
    expect(store.changeEventConfigSourceStatusDialogState.eventConfigSource).toBeNull()
  })
  beforeEach(() => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)
  })

  it('should disable event config source successfully', async () => {
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(true)

    await store.disableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, false)
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  it('should enable event config source successfully', async () => {
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(true)

    await store.enableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, true)
    expect(filterEventConfigSources).toHaveBeenCalled()
  })

  it('should not fetch configs if disable fails', async () => {
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(false)

    await store.disableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, false)
    expect(filterEventConfigSources).not.toHaveBeenCalled()
  })

  it('should not fetch configs if enable fails', async () => {
    vi.mocked(changeEventConfigSourceStatus).mockResolvedValue(false)

    await store.enableEventConfigSource(1)

    expect(changeEventConfigSourceStatus).toHaveBeenCalledWith(1, true)
    expect(filterEventConfigSources).not.toHaveBeenCalled()
  })

  it('should log error when disabling with no source id', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await expect(store.disableEventConfigSource(0)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')

    consoleErrorSpy.mockRestore()
  })

  it('should log error when enabling with no source id', async () => {
    const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})

    await expect(store.disableEventConfigSource(0)).rejects.toThrow('No source selected')

    expect(changeEventConfigSourceStatus).not.toHaveBeenCalled()
    expect(consoleErrorSpy).toHaveBeenCalledWith('No source selected')

    consoleErrorSpy.mockRestore()
  })

  it('should reset active tab', () => {
    store.activeTab = 5

    store.resetActiveTab()

    expect(store.activeTab).toBe(0)
  })

  it('should refresh events sources with all defaults', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)
    store.sourcesPagination = { page: 5, pageSize: 25, total: 100 }
    store.sourcesSearchTerm = 'search term'
    store.sourcesSorting = { sortKey: 'vendor', sortOrder: 'asc' }

    await store.refreshSourcesfilters()

    expect(store.sourcesPagination.page).toBe(1)
    expect(store.sourcesPagination.pageSize).toBe(10)
    expect(store.sourcesPagination.total).toBe(2)
    expect(store.sourcesSearchTerm).toBe('')
    expect(store.sourcesSorting).toEqual({
      sortKey: 'createdTime',
      sortOrder: 'desc'
    })
    expect(filterEventConfigSources).toHaveBeenCalledWith(0, 10, '', 'createdTime', 'desc')
  })

  it('should handle multiple consecutive fetches correctly', async () => {
    vi.mocked(filterEventConfigSources).mockResolvedValue(mockFilterResponse)
    vi.mocked(getAllSourceNames).mockResolvedValue(mockSourceNames)

    await store.fetchEventConfigs()
    await store.onSourcePageChange(2)
    await store.onChangeSourcesSearchTerm('test')

    expect(filterEventConfigSources).toHaveBeenCalledTimes(3)
    expect(getAllSourceNames).toHaveBeenCalledTimes(3)
  })

  it('should maintain state consistency after failed operations', async () => {
    const error = new Error('Network error')
    vi.mocked(filterEventConfigSources).mockRejectedValue(error)
    vi.spyOn(console, 'error').mockImplementation(() => {})

    const initialSources = [...store.sources]
    const initialPagination = { ...store.sourcesPagination }

    await store.fetchEventConfigs()

    expect(store.sources).toEqual(initialSources)
    expect(store.sourcesPagination).toEqual(initialPagination)
    expect(store.isLoading).toBe(false)
  })
})

