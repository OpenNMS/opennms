import { createPinia, setActivePinia } from 'pinia'
import {
  getSnmpDataCollectionMibGroups,
  getSnmpDataCollectionResourceTypes,
  getSnmpDataCollectionSourceById,
  getSnmpDataCollectionSystemDefinitions
} from '@/services/snmpDataCollectionService'
import { useSnmpDataCollectionDetailStore } from '@/stores/snmpDataCollectionDetailStore'
import { SnmpCollectionSource } from '@/types/snmpDataCollection'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('@/services/snmpDataCollectionService', () => ({
  getSnmpDataCollectionSourceById: vi.fn(),
  getSnmpDataCollectionSystemDefinitions: vi.fn(),
  getSnmpDataCollectionMibGroups: vi.fn(),
  getSnmpDataCollectionResourceTypes: vi.fn()
}))

describe('useSnmpDataCollectionDetailStore', () => {
  let store: ReturnType<typeof useSnmpDataCollectionDetailStore>

  const mockCollectionSource: SnmpCollectionSource = {
    id: 1,
    name: 'Test Source',
    vendor: 'Test Vendor',
    description: 'Test Description',
    enabled: true,
    uploadedBy: 'testuser',
    createdTime: new Date('2024-01-01'),
    lastModified: new Date('2024-01-02')
  }

  const mockSystemDefinitions = [
    {
      id: 1,
      name: 'SysDef 1',
      sysoid: '1.3.6.1.1',
      sysoidMask: '',
      ipAddresses: '',
      ipAddressMasks: '',
      mibGroupNames: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    },
    {
      id: 2,
      name: 'SysDef 2',
      sysoid: '1.3.6.1.2',
      sysoidMask: '',
      ipAddresses: '',
      ipAddressMasks: '',
      mibGroupNames: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
  ]

  const mockResourceTypes = [
    {
      id: 1,
      name: 'ResourceType 1',
      label: 'RT1',
      resourceLabel: '',
      persistenceSelectorStrategy: '',
      persistenceSelectorParams: '',
      storageStrategy: '',
      storageStrategyParams: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    },
    {
      id: 2,
      name: 'ResourceType 2',
      label: 'RT2',
      resourceLabel: '',
      persistenceSelectorStrategy: '',
      persistenceSelectorParams: '',
      storageStrategy: '',
      storageStrategyParams: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
  ]

  const mockMibGroups = [
    {
      id: 1,
      name: 'MibGroup 1',
      ifType: 'all',
      mibGroupNames: '',
      mibObjects: '',
      mibObjProperties: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    },
    {
      id: 2,
      name: 'MibGroup 2',
      ifType: 'ignore',
      mibGroupNames: '',
      mibObjects: '',
      mibObjProperties: '',
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    }
  ]

  const mockSystemDefsResponse = {
    systemDefinitions: mockSystemDefinitions,
    totalRecords: 2
  }

  const mockResourceTypesResponse = {
    resourceTypes: mockResourceTypes,
    totalRecords: 2
  }

  const mockMibGroupsResponse = {
    mibGroups: mockMibGroups,
    totalRecords: 2
  }

  beforeEach(() => {
    setActivePinia(createPinia())
    store = useSnmpDataCollectionDetailStore()
    vi.clearAllMocks()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Initial State', () => {
    it('should have correct initial state', () => {
      expect(store.isLoading).toBe(false)
      expect(store.selectedCollectionSource).toBeNull()
      expect(store.systemDefinitions).toEqual([])
      expect(store.systemDefsPagination).toEqual({ page: 1, pageSize: 10, total: 0 })
      expect(store.systemDefsSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.systemDefsSearchTerm).toBe('')
      expect(store.resourceTypes).toEqual([])
      expect(store.resourceTypesPagination).toEqual({ page: 1, pageSize: 10, total: 0 })
      expect(store.resourceTypesSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.resourceTypesSearchTerm).toBe('')
      expect(store.mibGroups).toEqual([])
      expect(store.mibGroupsPagination).toEqual({ page: 1, pageSize: 10, total: 0 })
      expect(store.mibGroupsSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.mibGroupsSearchTerm).toBe('')
    })

    it('should have isLoading set to false initially', () => {
      expect(store.isLoading).toBe(false)
    })

    it('should have null selectedCollectionSource initially', () => {
      expect(store.selectedCollectionSource).toBeNull()
    })

    it('should have empty arrays for all data collections', () => {
      expect(store.systemDefinitions).toEqual([])
      expect(store.resourceTypes).toEqual([])
      expect(store.mibGroups).toEqual([])
    })

    it('should have default pagination for all sections', () => {
      const defaultPagination = { page: 1, pageSize: 10, total: 0 }
      expect(store.systemDefsPagination).toEqual(defaultPagination)
      expect(store.resourceTypesPagination).toEqual(defaultPagination)
      expect(store.mibGroupsPagination).toEqual(defaultPagination)
    })

    it('should have default sorting for all sections', () => {
      const defaultSorting = { sortOrder: 'desc', sortKey: 'createdTime' }
      expect(store.systemDefsSorting).toEqual(defaultSorting)
      expect(store.resourceTypesSorting).toEqual(defaultSorting)
      expect(store.mibGroupsSorting).toEqual(defaultSorting)
    })

    it('should have empty search terms for all sections', () => {
      expect(store.systemDefsSearchTerm).toBe('')
      expect(store.resourceTypesSearchTerm).toBe('')
      expect(store.mibGroupsSearchTerm).toBe('')
    })
  })

  describe('setSelectedCollectionSource', () => {
    it('should set selected collection source', () => {
      store.setSelectedCollectionSource(mockCollectionSource)
      expect(store.selectedCollectionSource).toEqual(mockCollectionSource)
    })

    it('should set selected collection source to null', () => {
      store.selectedCollectionSource = mockCollectionSource
      store.setSelectedCollectionSource(null)
      expect(store.selectedCollectionSource).toBeNull()
    })

    it('should update existing selected collection source', () => {
      store.setSelectedCollectionSource(mockCollectionSource)
      const newSource = { ...mockCollectionSource, id: 2, name: 'New Source' }
      store.setSelectedCollectionSource(newSource)
      expect(store.selectedCollectionSource).toEqual(newSource)
    })
  })

  describe('fetchCollectionSourceById', () => {
    it('should fetch collection source by ID successfully', async () => {
      vi.mocked(getSnmpDataCollectionSourceById).mockResolvedValue(mockCollectionSource)

      await store.fetchCollectionSourceById('1')

      expect(getSnmpDataCollectionSourceById).toHaveBeenCalledWith(1)
      expect(store.selectedCollectionSource).toEqual(mockCollectionSource)
      expect(store.isLoading).toBe(false)
    })

    it('should handle errors when fetching source by ID', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Failed to fetch source')
      vi.mocked(getSnmpDataCollectionSourceById).mockRejectedValue(error)

      await store.fetchCollectionSourceById('1')

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection source by ID:', '1', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should set loading state during fetch', async () => {
      vi.mocked(getSnmpDataCollectionSourceById).mockImplementation(
        () =>
          new Promise((resolve) => {
            expect(store.isLoading).toBe(true)
            resolve(mockCollectionSource)
          })
      )

      await store.fetchCollectionSourceById('1')
      expect(store.isLoading).toBe(false)
    })

    it('should convert string ID to number', async () => {
      vi.mocked(getSnmpDataCollectionSourceById).mockResolvedValue(mockCollectionSource)

      await store.fetchCollectionSourceById('123')

      expect(getSnmpDataCollectionSourceById).toHaveBeenCalledWith(123)
    })
  })

  describe('System Definitions - fetchSystemDefinitions', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should fetch system definitions successfully', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
      expect(store.systemDefsPagination.total).toBe(2)
      expect(store.isLoading).toBe(false)
    })

    it('should not fetch if selectedCollectionSource is null', async () => {
      store.selectedCollectionSource = null
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).not.toHaveBeenCalled()
    })

    it('should fetch with correct pagination parameters', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      store.systemDefsPagination.page = 3
      store.systemDefsPagination.pageSize = 20

      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 40, 20, '', 'createdTime', 'desc')
    })

    it('should fetch with search term', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      store.systemDefsSearchTerm = 'test search'

      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(
        1,
        0,
        10,
        'test search',
        'createdTime',
        'desc'
      )
    })

    it('should fetch with custom sorting', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      store.systemDefsSorting = { sortKey: 'name', sortOrder: 'asc' }

      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', 'name', 'asc')
    })

    it('should handle errors when fetching', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Fetch failed')
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(error)

      await store.fetchSystemDefinitions()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection system definitions:', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should handle empty response', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue({
        systemDefinitions: [],
        totalRecords: 0
      })

      await store.fetchSystemDefinitions()

      expect(store.systemDefinitions).toEqual([])
      expect(store.systemDefsPagination.total).toBe(0)
    })
  })

  describe('System Definitions - Page Actions', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
    })

    it('should change page and fetch', async () => {
      await store.onSystemDefsPageChange(3)

      expect(store.systemDefsPagination.page).toBe(3)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 20, 10, '', 'createdTime', 'desc')
    })

    it('should change page size, reset to page 1, and fetch', async () => {
      store.systemDefsPagination.page = 5

      await store.onSystemDefsPageSizeChange(25)

      expect(store.systemDefsPagination.pageSize).toBe(25)
      expect(store.systemDefsPagination.page).toBe(1)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 25, '', 'createdTime', 'desc')
    })

    it('should change search term, reset to page 1, and fetch', async () => {
      store.systemDefsPagination.page = 3

      await store.onChangeSystemDefsSearchTerm('search')

      expect(store.systemDefsSearchTerm).toBe('search')
      expect(store.systemDefsPagination.page).toBe(1)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, 'search', 'createdTime', 'desc')
    })

    it('should change sort and fetch', async () => {
      await store.onSystemDefsSortChange('name', 'asc')

      expect(store.systemDefsSorting.sortKey).toBe('name')
      expect(store.systemDefsSorting.sortOrder).toBe('asc')
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', 'name', 'asc')
    })
  })

  describe('System Definitions - Reset Filters', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
    })

    it('should reset all system definitions filters', async () => {
      store.systemDefinitions = mockSystemDefinitions
      store.systemDefsPagination = { page: 5, pageSize: 25, total: 100 }
      store.systemDefsSorting = { sortKey: 'name', sortOrder: 'asc' }
      store.systemDefsSearchTerm = 'test'

      await store.resetSystemDefinitionsFilters()

      // After reset, it fetches data so it will have the mocked results
      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
      expect(store.systemDefsPagination).toEqual({ page: 1, pageSize: 10, total: 2 })
      expect(store.systemDefsSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.systemDefsSearchTerm).toBe('')
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })
  })

  describe('MIB Groups - fetchMibGroups', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should fetch MIB groups successfully', async () => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

      await store.fetchMibGroups()

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
      expect(store.mibGroups).toEqual(mockMibGroups)
      expect(store.mibGroupsPagination.total).toBe(2)
      expect(store.isLoading).toBe(false)
    })

    it('should not fetch if selectedCollectionSource is null', async () => {
      store.selectedCollectionSource = null
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

      await store.fetchMibGroups()

      expect(getSnmpDataCollectionMibGroups).not.toHaveBeenCalled()
    })

    it('should fetch with correct pagination parameters', async () => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      store.mibGroupsPagination.page = 2
      store.mibGroupsPagination.pageSize = 15

      await store.fetchMibGroups()

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 15, 15, '', 'createdTime', 'desc')
    })

    it('should fetch with search term', async () => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      store.mibGroupsSearchTerm = 'mib search'

      await store.fetchMibGroups()

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, 'mib search', 'createdTime', 'desc')
    })

    it('should fetch with custom sorting', async () => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      store.mibGroupsSorting = { sortKey: 'name', sortOrder: 'asc' }

      await store.fetchMibGroups()

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', 'name', 'asc')
    })

    it('should handle errors when fetching', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Fetch failed')
      vi.mocked(getSnmpDataCollectionMibGroups).mockRejectedValue(error)

      await store.fetchMibGroups()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection MIB groups:', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should handle empty response', async () => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue({
        mibGroups: [],
        totalRecords: 0
      })

      await store.fetchMibGroups()

      expect(store.mibGroups).toEqual([])
      expect(store.mibGroupsPagination.total).toBe(0)
    })
  })

  describe('MIB Groups - Page Actions', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
    })

    it('should change page and fetch', async () => {
      await store.onMibGroupsPageChange(4)

      expect(store.mibGroupsPagination.page).toBe(4)
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 30, 10, '', 'createdTime', 'desc')
    })

    it('should change page size and fetch', async () => {
      await store.onMibGroupsPageSizeChange(50)

      expect(store.mibGroupsPagination.pageSize).toBe(50)
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 50, '', 'createdTime', 'desc')
    })

    it('should change search term, reset to page 1, and fetch', async () => {
      store.mibGroupsPagination.page = 3

      await store.onChangeMibGroupsSearchTerm('search')

      expect(store.mibGroupsSearchTerm).toBe('search')
      expect(store.mibGroupsPagination.page).toBe(1)
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, 'search', 'createdTime', 'desc')
    })

    it('should change sort and fetch', async () => {
      await store.onMibGroupsSortChange('ifType', 'asc')

      expect(store.mibGroupsSorting.sortKey).toBe('ifType')
      expect(store.mibGroupsSorting.sortOrder).toBe('asc')
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', 'ifType', 'asc')
    })
  })

  describe('MIB Groups - Reset Filters', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
    })

    it('should reset all MIB groups filters', async () => {
      store.mibGroupsSearchTerm = 'test'
      store.mibGroupsPagination.page = 5
      store.mibGroupsSorting = { sortKey: 'name', sortOrder: 'asc' }

      await store.resetMibGroupsFilters()

      expect(store.mibGroupsSearchTerm).toBe('')
      expect(store.mibGroupsPagination.page).toBe(1)
      expect(store.mibGroupsSorting).toEqual({ sortKey: 'createdTime', sortOrder: 'desc' })
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalled()
    })

    it('should fetch after resetting filters', async () => {
      await store.resetMibGroupsFilters()

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
    })
  })

  describe('Resource Types - fetchResourceTypes', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should fetch resource types successfully', async () => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

      await store.fetchResourceTypes()

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
      expect(store.resourceTypes).toEqual(mockResourceTypes)
      expect(store.resourceTypesPagination.total).toBe(2)
      expect(store.isLoading).toBe(false)
    })

    it('should not fetch if selectedCollectionSource is null', async () => {
      store.selectedCollectionSource = null
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

      await store.fetchResourceTypes()

      expect(getSnmpDataCollectionResourceTypes).not.toHaveBeenCalled()
    })

    it('should fetch with correct pagination parameters', async () => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      store.resourceTypesPagination.page = 2
      store.resourceTypesPagination.pageSize = 20

      await store.fetchResourceTypes()

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 20, 20, '', 'createdTime', 'desc')
    })

    it('should fetch with search term', async () => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      store.resourceTypesSearchTerm = 'resource search'

      await store.fetchResourceTypes()

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(
        1,
        0,
        10,
        'resource search',
        'createdTime',
        'desc'
      )
    })

    it('should fetch with custom sorting', async () => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      store.resourceTypesSorting = { sortKey: 'label', sortOrder: 'asc' }

      await store.fetchResourceTypes()

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, '', 'label', 'asc')
    })

    it('should handle errors when fetching', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Fetch failed')
      vi.mocked(getSnmpDataCollectionResourceTypes).mockRejectedValue(error)

      await store.fetchResourceTypes()

      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection resource types:', error)
      expect(store.isLoading).toBe(false)
      consoleErrorSpy.mockRestore()
    })

    it('should handle empty response', async () => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue({
        resourceTypes: [],
        totalRecords: 0
      })

      await store.fetchResourceTypes()

      expect(store.resourceTypes).toEqual([])
      expect(store.resourceTypesPagination.total).toBe(0)
    })
  })

  describe('Resource Types - Page Actions', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
    })

    it('should change page and fetch', async () => {
      await store.onResourceTypesPageChange(2)

      expect(store.resourceTypesPagination.page).toBe(2)
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 10, 10, '', 'createdTime', 'desc')
    })

    it('should change page size, reset to page 1, and fetch', async () => {
      store.resourceTypesPagination.page = 3

      await store.onResourceTypesPageSizeChange(30)

      expect(store.resourceTypesPagination.pageSize).toBe(30)
      expect(store.resourceTypesPagination.page).toBe(1)
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 30, '', 'createdTime', 'desc')
    })

    it('should change search term, reset to page 1, and fetch', async () => {
      store.resourceTypesPagination.page = 2

      await store.onChangeResourceTypesSearchTerm('resource')

      expect(store.resourceTypesSearchTerm).toBe('resource')
      expect(store.resourceTypesPagination.page).toBe(1)
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, 'resource', 'createdTime', 'desc')
    })

    it('should change sort and fetch', async () => {
      await store.onResourceTypesSortChange('name', 'desc')

      expect(store.resourceTypesSorting.sortKey).toBe('name')
      expect(store.resourceTypesSorting.sortOrder).toBe('desc')
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, '', 'name', 'desc')
    })
  })

  describe('Resource Types - Reset Filters', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
    })

    it('should reset all resource types filters', async () => {
      store.resourceTypes = mockResourceTypes
      store.resourceTypesPagination = { page: 5, pageSize: 25, total: 100 }
      store.resourceTypesSorting = { sortKey: 'name', sortOrder: 'asc' }
      store.resourceTypesSearchTerm = 'test'

      await store.resetResourceTypesFilters()

      // After reset, it fetches data so it will have the mocked results
      expect(store.resourceTypes).toEqual(mockResourceTypes)
      expect(store.resourceTypesPagination).toEqual({ page: 1, pageSize: 10, total: 2 })
      expect(store.resourceTypesSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.resourceTypesSearchTerm).toBe('')
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalled()
    })
  })

  describe('Integration Tests', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
    })

    it('should handle complete workflow for system definitions', async () => {
      await store.onChangeSystemDefsSearchTerm('test')
      expect(store.systemDefsSearchTerm).toBe('test')

      await store.onSystemDefsSortChange('name', 'asc')
      expect(store.systemDefsSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })

      await store.onSystemDefsPageChange(2)
      expect(store.systemDefsPagination.page).toBe(2)

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledTimes(3)
    })

    it('should handle complete workflow for MIB groups', async () => {
      await store.onChangeMibGroupsSearchTerm('mib')
      expect(store.mibGroupsSearchTerm).toBe('mib')

      await store.onMibGroupsSortChange('ifType', 'asc')
      expect(store.mibGroupsSorting).toEqual({ sortKey: 'ifType', sortOrder: 'asc' })

      await store.onMibGroupsPageChange(3)
      expect(store.mibGroupsPagination.page).toBe(3)

      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledTimes(3)
    })

    it('should handle complete workflow for resource types', async () => {
      await store.onChangeResourceTypesSearchTerm('resource')
      expect(store.resourceTypesSearchTerm).toBe('resource')

      await store.onResourceTypesSortChange('label', 'desc')
      expect(store.resourceTypesSorting).toEqual({ sortKey: 'label', sortOrder: 'desc' })

      await store.onResourceTypesPageChange(2)
      expect(store.resourceTypesPagination.page).toBe(2)

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledTimes(3)
    })

    it('should maintain independent state for all three sections', async () => {
      await store.onChangeSystemDefsSearchTerm('sys')
      await store.onChangeMibGroupsSearchTerm('mib')
      await store.onChangeResourceTypesSearchTerm('res')

      expect(store.systemDefsSearchTerm).toBe('sys')
      expect(store.mibGroupsSearchTerm).toBe('mib')
      expect(store.resourceTypesSearchTerm).toBe('res')
    })

    it('should handle reset for one section without affecting others', async () => {
      await store.onChangeSystemDefsSearchTerm('sys')
      await store.onChangeMibGroupsSearchTerm('mib')

      await store.resetSystemDefinitionsFilters()

      expect(store.systemDefsSearchTerm).toBe('')
      expect(store.mibGroupsSearchTerm).toBe('mib')
    })
  })

  describe('Error Handling', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should handle network errors gracefully in fetchCollectionSourceById', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionSourceById).mockRejectedValue(new Error('Network error'))

      await store.fetchCollectionSourceById('1')

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should handle network errors gracefully in fetchSystemDefinitions', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(new Error('Network error'))

      await store.fetchSystemDefinitions()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should handle network errors gracefully in fetchMibGroups', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionMibGroups).mockRejectedValue(new Error('Network error'))

      await store.fetchMibGroups()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })

    it('should handle network errors gracefully in fetchResourceTypes', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionResourceTypes).mockRejectedValue(new Error('Network error'))

      await store.fetchResourceTypes()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalled()
      consoleErrorSpy.mockRestore()
    })
  })

  describe('Parametrized Tests - Pagination Calculations', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { section: 'systemDefs', page: 1, pageSize: 10, expectedOffset: 0 },
      { section: 'systemDefs', page: 2, pageSize: 10, expectedOffset: 10 },
      { section: 'systemDefs', page: 3, pageSize: 20, expectedOffset: 40 },
      { section: 'systemDefs', page: 5, pageSize: 15, expectedOffset: 60 }
    ])(
      'should calculate offset correctly for $section: page $page with pageSize $pageSize',
      async ({ section, page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

        if (section === 'systemDefs') {
          store.systemDefsPagination.page = page
          store.systemDefsPagination.pageSize = pageSize
          await store.fetchSystemDefinitions()
          expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(
            1,
            expectedOffset,
            pageSize,
            '',
            'createdTime',
            'desc'
          )
        }
      }
    )

    it.each([
      { section: 'mibGroups', page: 1, pageSize: 10, expectedOffset: 0 },
      { section: 'mibGroups', page: 2, pageSize: 15, expectedOffset: 15 },
      { section: 'mibGroups', page: 4, pageSize: 25, expectedOffset: 75 }
    ])(
      'should calculate offset correctly for $section: page $page with pageSize $pageSize',
      async ({ section, page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

        if (section === 'mibGroups') {
          store.mibGroupsPagination.page = page
          store.mibGroupsPagination.pageSize = pageSize
          await store.fetchMibGroups()
          expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(
            1,
            expectedOffset,
            pageSize,
            '',
            'createdTime',
            'desc'
          )
        }
      }
    )

    it.each([
      { section: 'resourceTypes', page: 1, pageSize: 5, expectedOffset: 0 },
      { section: 'resourceTypes', page: 3, pageSize: 10, expectedOffset: 20 },
      { section: 'resourceTypes', page: 5, pageSize: 20, expectedOffset: 80 }
    ])(
      'should calculate offset correctly for $section: page $page with pageSize $pageSize',
      async ({ section, page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

        if (section === 'resourceTypes') {
          store.resourceTypesPagination.page = page
          store.resourceTypesPagination.pageSize = pageSize
          await store.fetchResourceTypes()
          expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(
            1,
            expectedOffset,
            pageSize,
            '',
            'createdTime',
            'desc'
          )
        }
      }
    )
  })

  describe('Parametrized Tests - Sort Combinations', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { section: 'systemDefs', sortKey: 'name', sortOrder: 'asc' },
      { section: 'systemDefs', sortKey: 'name', sortOrder: 'desc' },
      { section: 'systemDefs', sortKey: 'oid', sortOrder: 'asc' },
      { section: 'systemDefs', sortKey: 'createdTime', sortOrder: 'desc' }
    ])(
      'should handle sorting for $section by $sortKey with order $sortOrder',
      async ({ section, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

        if (section === 'systemDefs') {
          await store.onSystemDefsSortChange(sortKey, sortOrder)
          expect(store.systemDefsSorting.sortKey).toBe(sortKey)
          expect(store.systemDefsSorting.sortOrder).toBe(sortOrder)
          expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
        }
      }
    )

    it.each([
      { section: 'mibGroups', sortKey: 'name', sortOrder: 'asc' },
      { section: 'mibGroups', sortKey: 'ifType', sortOrder: 'desc' },
      { section: 'mibGroups', sortKey: 'createdTime', sortOrder: 'asc' }
    ])(
      'should handle sorting for $section by $sortKey with order $sortOrder',
      async ({ section, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

        if (section === 'mibGroups') {
          await store.onMibGroupsSortChange(sortKey, sortOrder)
          expect(store.mibGroupsSorting.sortKey).toBe(sortKey)
          expect(store.mibGroupsSorting.sortOrder).toBe(sortOrder)
          expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
        }
      }
    )

    it.each([
      { section: 'resourceTypes', sortKey: 'name', sortOrder: 'asc' },
      { section: 'resourceTypes', sortKey: 'label', sortOrder: 'desc' },
      { section: 'resourceTypes', sortKey: 'createdTime', sortOrder: 'asc' }
    ])(
      'should handle sorting for $section by $sortKey with order $sortOrder',
      async ({ section, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

        if (section === 'resourceTypes') {
          await store.onResourceTypesSortChange(sortKey, sortOrder)
          expect(store.resourceTypesSorting.sortKey).toBe(sortKey)
          expect(store.resourceTypesSorting.sortOrder).toBe(sortOrder)
          expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
        }
      }
    )
  })

  describe('Parametrized Tests - Search Terms', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { section: 'systemDefs', term: 'simple' },
      { section: 'systemDefs', term: 'with spaces' },
      { section: 'systemDefs', term: 'special@chars#' },
      { section: 'systemDefs', term: '' }
    ])('should handle search term "$term" for $section', async ({ section, term }) => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      if (section === 'systemDefs') {
        await store.onChangeSystemDefsSearchTerm(term)
        expect(store.systemDefsSearchTerm).toBe(term)
        expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
      }
    })

    it.each([
      { section: 'mibGroups', term: 'mib-test' },
      { section: 'mibGroups', term: 'IF-MIB' },
      { section: 'mibGroups', term: '' }
    ])('should handle search term "$term" for $section', async ({ section, term }) => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

      if (section === 'mibGroups') {
        await store.onChangeMibGroupsSearchTerm(term)
        expect(store.mibGroupsSearchTerm).toBe(term)
        expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
      }
    })

    it.each([
      { section: 'resourceTypes', term: 'node' },
      { section: 'resourceTypes', term: 'interface' },
      { section: 'resourceTypes', term: '' }
    ])('should handle search term "$term" for $section', async ({ section, term }) => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

      if (section === 'resourceTypes') {
        await store.onChangeResourceTypesSearchTerm(term)
        expect(store.resourceTypesSearchTerm).toBe(term)
        expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
      }
    })
  })

  describe('State Independence', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
    })

    it('should maintain independent pagination for each section', async () => {
      await store.onSystemDefsPageChange(2)
      await store.onMibGroupsPageChange(3)
      await store.onResourceTypesPageChange(4)

      expect(store.systemDefsPagination.page).toBe(2)
      expect(store.mibGroupsPagination.page).toBe(3)
      expect(store.resourceTypesPagination.page).toBe(4)
    })

    it('should maintain independent sorting for each section', async () => {
      await store.onSystemDefsSortChange('name', 'asc')
      await store.onMibGroupsSortChange('ifType', 'desc')
      await store.onResourceTypesSortChange('label', 'asc')

      expect(store.systemDefsSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })
      expect(store.mibGroupsSorting).toEqual({ sortKey: 'ifType', sortOrder: 'desc' })
      expect(store.resourceTypesSorting).toEqual({ sortKey: 'label', sortOrder: 'asc' })
    })

    it('should maintain independent search terms for each section', async () => {
      await store.onChangeSystemDefsSearchTerm('sys')
      await store.onChangeMibGroupsSearchTerm('mib')
      await store.onChangeResourceTypesSearchTerm('res')

      expect(store.systemDefsSearchTerm).toBe('sys')
      expect(store.mibGroupsSearchTerm).toBe('mib')
      expect(store.resourceTypesSearchTerm).toBe('res')
    })
  })
})

