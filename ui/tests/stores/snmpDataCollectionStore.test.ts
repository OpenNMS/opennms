import { createPinia, setActivePinia } from 'pinia'
import {
  filterSnmpCollectionSources,
  getAllSnmpCollectionSourcesNamesAndIds
} from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionStore } from '@/stores/snmpDataCollectionStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/snmpDataCollectionService', () => ({
  filterSnmpCollectionSources: vi.fn(),
  getAllSnmpCollectionSourcesNamesAndIds: vi.fn()
}))

const DEFAULT_PAGE_SIZE = 50

describe('useSnmpDataCollectionStore', () => {
  let store: ReturnType<typeof useSnmpDataCollectionStore>

  const mockSourceNames = [
    { id: 1, name: 'Source1' },
    { id: 2, name: 'Source2' },
    { id: 3, name: 'Source3' }
  ]

  const mockSources: SnmpCollectionSource[] = [
    {
      id: 1,
      name: 'Test Source 1',
      vendor: 'Vendor A',
      description: 'Description 1',
      enabled: true,
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
    store = useSnmpDataCollectionStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      expect(store.sources).toEqual([])
      expect(store.sourcesPagination).toEqual({
        page: 1,
        pageSize: DEFAULT_PAGE_SIZE,
        total: 0
      })
      expect(store.sourcesSearchTerm).toBe('')
      expect(store.sourcesSorting).toEqual({
        sortOrder: 'desc',
        sortKey: 'createdTime'
      })
      expect(store.isLoading).toBe(false)
      expect(store.selectedSource).toBeNull()
      expect(store.uploadedSourceNames).toEqual([])
    })

    it('should have isLoading set to false initially', () => {
      expect(store.isLoading).toBe(false)
    })

    it('should have empty sources array initially', () => {
      expect(store.sources).toEqual([])
      expect(store.sources.length).toBe(0)
    })

    it('should have null selectedSource initially', () => {
      expect(store.selectedSource).toBeNull()
    })

    it('should have empty uploadedSourceNames array initially', () => {
      expect(store.uploadedSourceNames).toEqual([])
      expect(store.uploadedSourceNames.length).toBe(0)
    })

    it('should have default pagination settings', () => {
      expect(store.sourcesPagination.page).toBe(1)
      expect(store.sourcesPagination.pageSize).toBe(DEFAULT_PAGE_SIZE)
      expect(store.sourcesPagination.total).toBe(0)
    })

    it('should have default sorting settings', () => {
      expect(store.sourcesSorting.sortKey).toBe('createdTime')
      expect(store.sourcesSorting.sortOrder).toBe('desc')
    })

    it('should have empty search term initially', () => {
      expect(store.sourcesSearchTerm).toBe('')
    })
  })

  describe('fetchAllSourcesNames', () => {
    it('should fetch all source names successfully', async () => {
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockResolvedValue(mockSourceNames)

      await store.fetchAllSourcesNames()

      expect(getAllSnmpCollectionSourcesNamesAndIds).toHaveBeenCalledTimes(1)
      expect(store.uploadedSourceNames).toEqual(mockSourceNames)
      expect(store.isLoading).toBe(false)
    })

    it('should handle errors when fetching source names', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Failed to fetch source names')
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockRejectedValue(error)

      await store.fetchAllSourcesNames()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching all SNMP data collection source names:', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should set loading state correctly during fetch', async () => {
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockImplementation(
        () =>
          new Promise((resolve) => {
            expect(store.isLoading).toBe(true)
            resolve(mockSourceNames)
          })
      )

      await store.fetchAllSourcesNames()
      expect(store.isLoading).toBe(false)
    })

    it('should set isLoading to true before fetching', async () => {
      let loadingDuringFetch = false
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockImplementation(async () => {
        loadingDuringFetch = store.isLoading
        return mockSourceNames
      })

      await store.fetchAllSourcesNames()
      expect(loadingDuringFetch).toBe(true)
    })

    it('should update uploadedSourceNames with fetched data', async () => {
      const newSourceNames = [{ id: 10, name: 'NewSource' }]
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockResolvedValue(newSourceNames)

      await store.fetchAllSourcesNames()

      expect(store.uploadedSourceNames).toEqual(newSourceNames)
    })

    it('should handle empty response', async () => {
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockResolvedValue([])

      await store.fetchAllSourcesNames()

      expect(store.uploadedSourceNames).toEqual([])
      expect(store.isLoading).toBe(false)
    })

    it('should set isLoading to false on error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockRejectedValue(new Error('API Error'))

      await store.fetchAllSourcesNames()

      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('fetchSnmpCollectionSources', () => {
    it('should fetch SNMP collection sources successfully', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.fetchSnmpCollectionSources()

      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
      expect(store.sources).toEqual(mockSources)
      expect(store.sourcesPagination.total).toBe(2)
      expect(store.isLoading).toBe(false)
    })

    it('should fetch with correct pagination parameters', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      store.sourcesPagination.page = 3
      store.sourcesPagination.pageSize = 20

      await store.fetchSnmpCollectionSources()

      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(40, 20, '', 'createdTime', 'desc')
    })

    it('should fetch with search term', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      store.sourcesSearchTerm = 'test search'

      await store.fetchSnmpCollectionSources()

      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, 'test search', 'createdTime', 'desc')
    })

    it('should fetch with custom sorting', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      store.sourcesSorting.sortKey = 'name'
      store.sourcesSorting.sortOrder = 'asc'

      await store.fetchSnmpCollectionSources()

      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', 'name', 'asc')
    })

    it('should handle errors when fetching sources', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Failed to fetch sources')
      vi.mocked(filterSnmpCollectionSources).mockRejectedValue(error)

      await store.fetchSnmpCollectionSources()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection sources:', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should set loading state correctly during fetch', async () => {
      vi.mocked(filterSnmpCollectionSources).mockImplementation(
        () =>
          new Promise((resolve) => {
            expect(store.isLoading).toBe(true)
            resolve(mockFilterResponse)
          })
      )

      await store.fetchSnmpCollectionSources()
      expect(store.isLoading).toBe(false)
    })

    it('should update sources with fetched data', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.fetchSnmpCollectionSources()

      expect(store.sources).toEqual(mockSources)
      expect(store.sources.length).toBe(2)
    })

    it('should update total records in pagination', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue({
        sources: mockSources,
        totalRecords: 100
      })

      await store.fetchSnmpCollectionSources()

      expect(store.sourcesPagination.total).toBe(100)
    })

    it('should handle empty response', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue({
        sources: [],
        totalRecords: 0
      })

      await store.fetchSnmpCollectionSources()

      expect(store.sources).toEqual([])
      expect(store.sourcesPagination.total).toBe(0)
      expect(store.isLoading).toBe(false)
    })

    it('should calculate offset correctly for different pages', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      store.sourcesPagination.page = 5
      store.sourcesPagination.pageSize = 15

      await store.fetchSnmpCollectionSources()

      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(60, 15, '', 'createdTime', 'desc')
    })
  })

  describe('onChangeSourcesSearchTerm', () => {
    it('should update search term and fetch sources', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onChangeSourcesSearchTerm('new search')

      expect(store.sourcesSearchTerm).toBe('new search')
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, 'new search', 'createdTime', 'desc')
    })

    it('should handle empty search term', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)
      store.sourcesSearchTerm = 'existing'

      await store.onChangeSourcesSearchTerm('')

      expect(store.sourcesSearchTerm).toBe('')
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
    })

    it('should trigger fetch after updating search term', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onChangeSourcesSearchTerm('test')

      expect(filterSnmpCollectionSources).toHaveBeenCalledTimes(1)
      expect(store.sources).toEqual(mockSources)
    })

    it('should handle special characters in search term', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onChangeSourcesSearchTerm('test@#$%')

      expect(store.sourcesSearchTerm).toBe('test@#$%')
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, 'test@#$%', 'createdTime', 'desc')
    })
  })

  describe('onSourcesSortChange', () => {
    it('should update sort key and order then fetch sources', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcesSortChange('name', 'asc')

      expect(store.sourcesSorting.sortKey).toBe('name')
      expect(store.sourcesSorting.sortOrder).toBe('asc')
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', 'name', 'asc')
    })

    it('should update only sort key', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcesSortChange('vendor', 'desc')

      expect(store.sourcesSorting.sortKey).toBe('vendor')
      expect(store.sourcesSorting.sortOrder).toBe('desc')
    })

    it('should update only sort order', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)
      store.sourcesSorting.sortKey = 'name'

      await store.onSourcesSortChange('name', 'asc')

      expect(store.sourcesSorting.sortKey).toBe('name')
      expect(store.sourcesSorting.sortOrder).toBe('asc')
    })

    it('should trigger fetch after updating sort', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcesSortChange('description', 'asc')

      expect(filterSnmpCollectionSources).toHaveBeenCalledTimes(1)
      expect(store.sources).toEqual(mockSources)
    })

    it.each([
      { sortKey: 'name', sortOrder: 'asc' },
      { sortKey: 'vendor', sortOrder: 'desc' },
      { sortKey: 'createdTime', sortOrder: 'asc' },
      { sortKey: 'lastModified', sortOrder: 'desc' },
      { sortKey: 'uploadedBy', sortOrder: 'asc' }
    ])('should handle sort by $sortKey with order $sortOrder', async ({ sortKey, sortOrder }) => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcesSortChange(sortKey, sortOrder)

      expect(store.sourcesSorting.sortKey).toBe(sortKey)
      expect(store.sourcesSorting.sortOrder).toBe(sortOrder)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', sortKey, sortOrder)
    })
  })

  describe('onSourcePageChange', () => {
    it('should update page number and fetch sources', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageChange(3)

      expect(store.sourcesPagination.page).toBe(3)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(100, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
    })

    it('should handle page 1', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)
      store.sourcesPagination.page = 5

      await store.onSourcePageChange(1)

      expect(store.sourcesPagination.page).toBe(1)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
    })

    it('should calculate correct offset for different pages', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageChange(10)

      expect(store.sourcesPagination.page).toBe(10)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(450, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
    })

    it('should trigger fetch after updating page', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageChange(2)

      expect(filterSnmpCollectionSources).toHaveBeenCalledTimes(1)
      expect(store.sources).toEqual(mockSources)
    })

    it.each([
      { page: 1, expectedOffset: 0 },
      { page: 2, expectedOffset: 50 },
      { page: 3, expectedOffset: 100 },
      { page: 5, expectedOffset: 200 },
      { page: 10, expectedOffset: 450 }
    ])('should handle page $page with offset $expectedOffset', async ({ page, expectedOffset }) => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageChange(page)

      expect(store.sourcesPagination.page).toBe(page)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(expectedOffset, DEFAULT_PAGE_SIZE, '', 'createdTime', 'desc')
    })
  })

  describe('onSourcePageSizeChange', () => {
    it('should update page size, reset to page 1, and fetch sources', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)
      store.sourcesPagination.page = 3

      await store.onSourcePageSizeChange(25)

      expect(store.sourcesPagination.page).toBe(1)
      expect(store.sourcesPagination.pageSize).toBe(25)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, 25, '', 'createdTime', 'desc')
    })

    it('should reset page to 1 when changing page size', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)
      store.sourcesPagination.page = 5

      await store.onSourcePageSizeChange(50)

      expect(store.sourcesPagination.page).toBe(1)
    })

    it('should update pageSize correctly', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageSizeChange(100)

      expect(store.sourcesPagination.pageSize).toBe(100)
    })

    it('should trigger fetch after updating page size', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageSizeChange(20)

      expect(filterSnmpCollectionSources).toHaveBeenCalledTimes(1)
      expect(store.sources).toEqual(mockSources)
    })

    it.each([{ pageSize: 5 }, { pageSize: 10 }, { pageSize: 20 }, { pageSize: 50 }, { pageSize: 100 }])(
      'should handle page size $pageSize',
      async ({ pageSize }) => {
        vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

        await store.onSourcePageSizeChange(pageSize)

        expect(store.sourcesPagination.pageSize).toBe(pageSize)
        expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, pageSize, '', 'createdTime', 'desc')
      }
    )
  })

  describe('State Mutations', () => {
    it('should allow direct state mutation for selectedSource', () => {
      const mockSource: SnmpCollectionSource = {
        id: 1,
        name: 'Test',
        vendor: 'Vendor',
        description: 'Desc',
        enabled: true,
        uploadedBy: 'user',
        createdTime: new Date(),
        lastModified: new Date()
      }

      store.selectedSource = mockSource

      expect(store.selectedSource).toEqual(mockSource)
    })

    it('should allow direct state mutation for isLoading', () => {
      store.isLoading = true
      expect(store.isLoading).toBe(true)

      store.isLoading = false
      expect(store.isLoading).toBe(false)
    })

    it('should allow direct state mutation for sources', () => {
      store.sources = mockSources
      expect(store.sources).toEqual(mockSources)
      expect(store.sources.length).toBe(2)
    })
  })

  describe('Integration Tests', () => {
    it('should handle complete workflow: search, sort, and paginate', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onChangeSourcesSearchTerm('test')
      expect(store.sourcesSearchTerm).toBe('test')

      await store.onSourcesSortChange('name', 'asc')
      expect(store.sourcesSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })

      await store.onSourcePageChange(2)
      expect(store.sourcesPagination.page).toBe(2)

      expect(filterSnmpCollectionSources).toHaveBeenCalledTimes(3)
    })

    it('should maintain state consistency across multiple operations', async () => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcePageSizeChange(20)
      expect(store.sourcesPagination.pageSize).toBe(20)
      expect(store.sourcesPagination.page).toBe(1)

      await store.onChangeSourcesSearchTerm('test')
      expect(store.sourcesSearchTerm).toBe('test')
      expect(store.sourcesPagination.pageSize).toBe(20)
    })
  })

  describe('Error Handling', () => {
    it('should handle network errors gracefully in fetchAllSourcesNames', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getAllSnmpCollectionSourcesNamesAndIds).mockRejectedValue(new Error('Network error'))

      await store.fetchAllSourcesNames()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should handle network errors gracefully in fetchSnmpCollectionSources', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(filterSnmpCollectionSources).mockRejectedValue(new Error('Network error'))

      await store.fetchSnmpCollectionSources()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should maintain state integrity after error', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const initialSources = [...mockSources]
      store.sources = initialSources

      vi.mocked(filterSnmpCollectionSources).mockRejectedValue(new Error('Error'))

      await store.fetchSnmpCollectionSources()

      expect(store.sources).toEqual(initialSources)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Parametrized Tests - Pagination Calculations', () => {
    it.each([
      { page: 1, pageSize: 50, expectedOffset: 0 },
      { page: 2, pageSize: 50, expectedOffset: 50 },
      { page: 3, pageSize: 50, expectedOffset: 100 },
      { page: 5, pageSize: 50, expectedOffset: 200 },
      { page: 10, pageSize: 50, expectedOffset: 450 },
      { page: 1, pageSize: 100, expectedOffset: 0 },
      { page: 7, pageSize: 25, expectedOffset: 150 }
    ])(
      'should calculate offset correctly for page $page with pageSize $pageSize',
      async ({ page, pageSize, expectedOffset }) => {
        vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

        store.sourcesPagination.page = page
        store.sourcesPagination.pageSize = pageSize

        await store.fetchSnmpCollectionSources()

        expect(filterSnmpCollectionSources).toHaveBeenCalledWith(expectedOffset, pageSize, '', 'createdTime', 'desc')
      }
    )
  })

  describe('Parametrized Tests - Sort Combinations', () => {
    it.each([
      { sortKey: 'name', sortOrder: 'asc' },
      { sortKey: 'name', sortOrder: 'desc' },
      { sortKey: 'vendor', sortOrder: 'asc' },
      { sortKey: 'vendor', sortOrder: 'desc' },
      { sortKey: 'createdTime', sortOrder: 'asc' },
      { sortKey: 'createdTime', sortOrder: 'desc' },
      { sortKey: 'lastModified', sortOrder: 'asc' },
      { sortKey: 'lastModified', sortOrder: 'desc' },
      { sortKey: 'uploadedBy', sortOrder: 'asc' },
      { sortKey: 'uploadedBy', sortOrder: 'desc' },
      { sortKey: 'enabled', sortOrder: 'asc' },
      { sortKey: 'enabled', sortOrder: 'desc' }
    ])('should handle sorting by $sortKey with order $sortOrder', async ({ sortKey, sortOrder }) => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onSourcesSortChange(sortKey, sortOrder)

      expect(store.sourcesSorting.sortKey).toBe(sortKey)
      expect(store.sourcesSorting.sortOrder).toBe(sortOrder)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, 50, '', sortKey, sortOrder)
    })
  })

  describe('Parametrized Tests - Search Terms', () => {
    it.each([
      { term: 'simple' },
      { term: 'with spaces' },
      { term: 'special@chars#' },
      { term: '123numbers' },
      { term: 'MixedCase' },
      { term: '' },
      { term: '   whitespace   ' }
    ])('should handle search term: "$term"', async ({ term }) => {
      vi.mocked(filterSnmpCollectionSources).mockResolvedValue(mockFilterResponse)

      await store.onChangeSourcesSearchTerm(term)

      expect(store.sourcesSearchTerm).toBe(term)
      expect(filterSnmpCollectionSources).toHaveBeenCalledWith(0, 50, term, 'createdTime', 'desc')
    })
  })
})
