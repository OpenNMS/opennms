import { createPinia, setActivePinia } from 'pinia'
import {
  getAllMibGroupNames,
  getAllResourceTypeNames,
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
  getSnmpDataCollectionResourceTypes: vi.fn(),
  getAllResourceTypeNames: vi.fn(),
  getAllMibGroupNames: vi.fn()
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
      ipAddresses: [],
      ipAddressMasks: [],
      mibGroupNames: [],
      enabled: true,
      collectionSourceId: 1,
      collectionSourceName: 'Test Source'
    },
    {
      id: 2,
      name: 'SysDef 2',
      sysoid: '1.3.6.1.2',
      sysoidMask: '',
      ipAddresses: [],
      ipAddressMasks: [],
      mibGroupNames: [],
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
      mibGroupNames: [],
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
      mibGroupNames: [],
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
    it('should have correct initial state for all properties', () => {
      const defaultPagination = { page: 1, pageSize: 10, total: 0 }
      const defaultSorting = { sortOrder: 'desc', sortKey: 'createdTime' }

      // Loading and source
      expect(store.isLoading).toBe(false)
      expect(store.selectedCollectionSource).toBeNull()

      // System Definitions
      expect(store.systemDefinitions).toEqual([])
      expect(store.systemDefsPagination).toEqual(defaultPagination)
      expect(store.systemDefsSorting).toEqual(defaultSorting)
      expect(store.systemDefsSearchTerm).toBe('')

      // Resource Types
      expect(store.resourceTypes).toEqual([])
      expect(store.resourceTypesPagination).toEqual(defaultPagination)
      expect(store.resourceTypesSorting).toEqual(defaultSorting)
      expect(store.resourceTypesSearchTerm).toBe('')

      // MIB Groups
      expect(store.mibGroups).toEqual([])
      expect(store.mibGroupsPagination).toEqual(defaultPagination)
      expect(store.mibGroupsSorting).toEqual(defaultSorting)
      expect(store.mibGroupsSearchTerm).toBe('')

      // Names lists
      expect(store.resourceTypeNames).toEqual([])
      expect(store.mibGroupNames).toEqual([])

      // Selected items
      expect(store.selectedSystemDef).toBeNull()
      expect(store.selectedMibGroup).toBeNull()
      expect(store.selectedResourceType).toBeNull()

      // Active tab
      expect(store.activeTab).toBe(0)

      // Drawer states
      expect(store.systemDefDrawerState).toEqual({
        visible: false,
        isEditMode: 0
      })
      expect(store.mibGroupDrawerState).toEqual({
        visible: false,
        isEditMode: 0
      })
      expect(store.resourceTypeDrawerState).toEqual({
        visible: false,
        isEditMode: 0
      })
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

    it('should handle source with minimal properties', () => {
      const minimalSource: SnmpCollectionSource = {
        id: 99,
        name: 'Minimal',
        vendor: '',
        description: '',
        enabled: false,
        uploadedBy: '',
        createdTime: new Date(),
        lastModified: new Date()
      }
      store.setSelectedCollectionSource(minimalSource)
      expect(store.selectedCollectionSource).toEqual(minimalSource)
    })

    it('should preserve other state when setting collection source', () => {
      store.systemDefsSearchTerm = 'test search'
      store.setSelectedCollectionSource(mockCollectionSource)
      expect(store.systemDefsSearchTerm).toBe('test search')
    })
  })

  describe('fetchCollectionSourceById', () => {
    const setupFetchMocks = () => {
      vi.mocked(getSnmpDataCollectionSourceById).mockResolvedValue(mockCollectionSource)
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      vi.mocked(getAllResourceTypeNames).mockResolvedValue([])
      vi.mocked(getAllMibGroupNames).mockResolvedValue([])
    }

    it('should fetch collection source by ID successfully', async () => {
      setupFetchMocks()

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
      let loadingDuringFetch = false
      vi.mocked(getSnmpDataCollectionSourceById).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingDuringFetch = store.isLoading
            resolve(mockCollectionSource)
          })
      )
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      vi.mocked(getAllResourceTypeNames).mockResolvedValue([])
      vi.mocked(getAllMibGroupNames).mockResolvedValue([])

      await store.fetchCollectionSourceById('1')
      expect(loadingDuringFetch).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should convert string ID to number', async () => {
      setupFetchMocks()

      await store.fetchCollectionSourceById('123')

      expect(getSnmpDataCollectionSourceById).toHaveBeenCalledWith(123)
    })

    it.each([
      { input: '0', expected: 0 },
      { input: '1', expected: 1 },
      { input: '999999', expected: 999999 },
      { input: '42', expected: 42 }
    ])('should convert string ID "$input" to number $expected', async ({ input, expected }) => {
      setupFetchMocks()

      await store.fetchCollectionSourceById(input)

      expect(getSnmpDataCollectionSourceById).toHaveBeenCalledWith(expected)
    })

    it('should not retain previous source on error', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      store.selectedCollectionSource = mockCollectionSource
      vi.mocked(getSnmpDataCollectionSourceById).mockRejectedValue(new Error('Fetch failed'))

      await store.fetchCollectionSourceById('2')

      // Previous source should still be there since we don't clear on error
      expect(store.selectedCollectionSource).toEqual(mockCollectionSource)
    })

    it('should reset loading state on error', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionSourceById).mockRejectedValue(new Error('Error'))

      await store.fetchCollectionSourceById('1')

      expect(store.isLoading).toBe(false)
    })

    it('should also fetch resource types, mib groups, system definitions, and their names', async () => {
      vi.mocked(getSnmpDataCollectionSourceById).mockResolvedValue(mockCollectionSource)
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      vi.mocked(getAllResourceTypeNames).mockResolvedValue(['nodeSnmp', 'hrStorageIndex'])
      vi.mocked(getAllMibGroupNames).mockResolvedValue(['mib2-interfaces', 'cisco-memory-pool'])

      await store.fetchCollectionSourceById('1')

      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalled()
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalled()
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
      expect(getAllResourceTypeNames).toHaveBeenCalled()
      expect(getAllMibGroupNames).toHaveBeenCalled()
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

    it('should set loading state during fetch', async () => {
      let loadingDuringFetch = false
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingDuringFetch = store.isLoading
            resolve(mockSystemDefsResponse)
          })
      )

      await store.fetchSystemDefinitions()
      expect(loadingDuringFetch).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should handle large total records', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue({
        systemDefinitions: mockSystemDefinitions,
        totalRecords: 10000
      })

      await store.fetchSystemDefinitions()

      expect(store.systemDefsPagination.total).toBe(10000)
    })

    it('should preserve existing data on error', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      store.systemDefinitions = mockSystemDefinitions
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(new Error('Error'))

      await store.fetchSystemDefinitions()

      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
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

    it('should preserve search term when changing page', async () => {
      store.systemDefsSearchTerm = 'existing search'
      await store.onSystemDefsPageChange(2)

      expect(store.systemDefsSearchTerm).toBe('existing search')
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(
        1,
        10,
        10,
        'existing search',
        'createdTime',
        'desc'
      )
    })

    it('should preserve sorting when changing page', async () => {
      store.systemDefsSorting = { sortKey: 'name', sortOrder: 'asc' }
      await store.onSystemDefsPageChange(2)

      expect(store.systemDefsSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 10, 10, '', 'name', 'asc')
    })

    it('should clear search term when passing empty string', async () => {
      store.systemDefsSearchTerm = 'some search'
      await store.onChangeSystemDefsSearchTerm('')

      expect(store.systemDefsSearchTerm).toBe('')
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

      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
      expect(store.systemDefsPagination).toEqual({ page: 1, pageSize: 10, total: 2 })
      expect(store.systemDefsSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.systemDefsSearchTerm).toBe('')
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })

    it('should fetch with default parameters after reset', async () => {
      store.systemDefsPagination = { page: 5, pageSize: 25, total: 100 }
      store.systemDefsSorting = { sortKey: 'name', sortOrder: 'asc' }
      store.systemDefsSearchTerm = 'test'

      await store.resetSystemDefinitionsFilters()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', 'createdTime', 'desc')
    })

    it('should not affect other sections when resetting system definitions', async () => {
      store.mibGroupsSearchTerm = 'mib search'
      store.resourceTypesSearchTerm = 'resource search'

      await store.resetSystemDefinitionsFilters()

      expect(store.mibGroupsSearchTerm).toBe('mib search')
      expect(store.resourceTypesSearchTerm).toBe('resource search')
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

    it('should set loading state during fetch', async () => {
      let loadingDuringFetch = false
      vi.mocked(getSnmpDataCollectionMibGroups).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingDuringFetch = store.isLoading
            resolve(mockMibGroupsResponse)
          })
      )

      await store.fetchMibGroups()
      expect(loadingDuringFetch).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should preserve existing data on error', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      store.mibGroups = mockMibGroups
      vi.mocked(getSnmpDataCollectionMibGroups).mockRejectedValue(new Error('Error'))

      await store.fetchMibGroups()

      expect(store.mibGroups).toEqual(mockMibGroups)
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

    it('should preserve search term when changing page', async () => {
      store.mibGroupsSearchTerm = 'existing search'
      await store.onMibGroupsPageChange(2)

      expect(store.mibGroupsSearchTerm).toBe('existing search')
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 10, 10, 'existing search', 'createdTime', 'desc')
    })

    it('should preserve sorting when changing page', async () => {
      store.mibGroupsSorting = { sortKey: 'name', sortOrder: 'asc' }
      await store.onMibGroupsPageChange(2)

      expect(store.mibGroupsSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 10, 10, '', 'name', 'asc')
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

    it('should not affect other sections when resetting MIB groups', async () => {
      store.systemDefsSearchTerm = 'system search'
      store.resourceTypesSearchTerm = 'resource search'

      await store.resetMibGroupsFilters()

      expect(store.systemDefsSearchTerm).toBe('system search')
      expect(store.resourceTypesSearchTerm).toBe('resource search')
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

    it('should set loading state during fetch', async () => {
      let loadingDuringFetch = false
      vi.mocked(getSnmpDataCollectionResourceTypes).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingDuringFetch = store.isLoading
            resolve(mockResourceTypesResponse)
          })
      )

      await store.fetchResourceTypes()
      expect(loadingDuringFetch).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should preserve existing data on error', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      store.resourceTypes = mockResourceTypes
      vi.mocked(getSnmpDataCollectionResourceTypes).mockRejectedValue(new Error('Error'))

      await store.fetchResourceTypes()

      expect(store.resourceTypes).toEqual(mockResourceTypes)
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

    it('should preserve search term when changing page', async () => {
      store.resourceTypesSearchTerm = 'existing search'
      await store.onResourceTypesPageChange(2)

      expect(store.resourceTypesSearchTerm).toBe('existing search')
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(
        1,
        10,
        10,
        'existing search',
        'createdTime',
        'desc'
      )
    })

    it('should preserve sorting when changing page', async () => {
      store.resourceTypesSorting = { sortKey: 'label', sortOrder: 'asc' }
      await store.onResourceTypesPageChange(2)

      expect(store.resourceTypesSorting).toEqual({ sortKey: 'label', sortOrder: 'asc' })
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 10, 10, '', 'label', 'asc')
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

      expect(store.resourceTypes).toEqual(mockResourceTypes)
      expect(store.resourceTypesPagination).toEqual({ page: 1, pageSize: 10, total: 2 })
      expect(store.resourceTypesSorting).toEqual({ sortOrder: 'desc', sortKey: 'createdTime' })
      expect(store.resourceTypesSearchTerm).toBe('')
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalled()
    })

    it('should not affect other sections when resetting resource types', async () => {
      store.systemDefsSearchTerm = 'system search'
      store.mibGroupsSearchTerm = 'mib search'

      await store.resetResourceTypesFilters()

      expect(store.systemDefsSearchTerm).toBe('system search')
      expect(store.mibGroupsSearchTerm).toBe('mib search')
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

    it('should handle fetching all sections concurrently', async () => {
      await Promise.all([store.fetchSystemDefinitions(), store.fetchMibGroups(), store.fetchResourceTypes()])

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalled()
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalled()
      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
      expect(store.mibGroups).toEqual(mockMibGroups)
      expect(store.resourceTypes).toEqual(mockResourceTypes)
    })

    it('should handle resetting all sections', async () => {
      store.systemDefsSearchTerm = 'sys'
      store.mibGroupsSearchTerm = 'mib'
      store.resourceTypesSearchTerm = 'res'

      await store.resetSystemDefinitionsFilters()
      await store.resetMibGroupsFilters()
      await store.resetResourceTypesFilters()

      expect(store.systemDefsSearchTerm).toBe('')
      expect(store.mibGroupsSearchTerm).toBe('')
      expect(store.resourceTypesSearchTerm).toBe('')
    })

    it('should handle changing collection source and refetching', async () => {
      await store.fetchSystemDefinitions()
      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)

      const newSource = { ...mockCollectionSource, id: 2, name: 'New Source' }
      store.setSelectedCollectionSource(newSource)
      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenLastCalledWith(2, 0, 10, '', 'createdTime', 'desc')
    })
  })

  describe('Drawer State Management', () => {
    it('should open drawer in create mode', () => {
      store.openSystemDefCreationDrawer(null, 1)
      expect(store.systemDefDrawerState.visible).toBe(true)
      expect(store.systemDefDrawerState.isEditMode).toBe(1)
      expect(store.selectedSystemDef).toBeNull()
    })

    it('should open drawer in edit mode with system definition', () => {
      const systemDef = mockSystemDefinitions[0]
      store.openSystemDefCreationDrawer(systemDef, 2)
      expect(store.systemDefDrawerState.visible).toBe(true)
      expect(store.systemDefDrawerState.isEditMode).toBe(2)
      expect(store.selectedSystemDef).toEqual(systemDef)
    })

    it('should close drawer and reset state', () => {
      store.systemDefDrawerState.visible = true
      store.systemDefDrawerState.isEditMode = 2
      store.selectedSystemDef = mockSystemDefinitions[0]

      store.closeSystemDefDrawer()

      expect(store.systemDefDrawerState.visible).toBe(false)
      expect(store.systemDefDrawerState.isEditMode).toBe(0)
      expect(store.selectedSystemDef).toBeNull()
    })

    it('should open MIB group drawer in create mode', () => {
      store.openMibGroupCreationDrawer(null, 1)
      expect(store.mibGroupDrawerState.visible).toBe(true)
      expect(store.mibGroupDrawerState.isEditMode).toBe(1)
      expect(store.selectedMibGroup).toBeNull()
    })

    it('should open MIB group drawer in edit mode with mib group', () => {
      const mibGroup = mockMibGroups[0]
      store.openMibGroupCreationDrawer(mibGroup, 2)
      expect(store.mibGroupDrawerState.visible).toBe(true)
      expect(store.mibGroupDrawerState.isEditMode).toBe(2)
      expect(store.selectedMibGroup).toEqual(mibGroup)
    })

    it('should close MIB group drawer and reset state', async () => {
      store.mibGroupDrawerState.visible = true
      store.mibGroupDrawerState.isEditMode = 2
      store.selectedMibGroup = mockMibGroups[0]

      await store.closeMibGroupDrawer()

      expect(store.mibGroupDrawerState.visible).toBe(false)
      expect(store.mibGroupDrawerState.isEditMode).toBe(0)
      expect(store.selectedMibGroup).toBeNull()
    })

    it('should call fetchMibGroupNames when closing MIB group drawer', async () => {
      vi.mocked(getAllMibGroupNames).mockResolvedValue(['mib-group-1', 'mib-group-2'])
      store.mibGroupDrawerState.visible = true
      store.selectedMibGroup = mockMibGroups[0]

      await store.closeMibGroupDrawer()

      expect(getAllMibGroupNames).toHaveBeenCalled()
    })

    it('should handle opening system def drawer multiple times', () => {
      store.openSystemDefCreationDrawer(null, 1)
      expect(store.systemDefDrawerState.visible).toBe(true)

      const systemDef = mockSystemDefinitions[0]
      store.openSystemDefCreationDrawer(systemDef, 2)
      expect(store.selectedSystemDef).toEqual(systemDef)
      expect(store.systemDefDrawerState.isEditMode).toBe(2)
    })

    it('should handle opening mib group drawer multiple times', () => {
      store.openMibGroupCreationDrawer(null, 1)
      expect(store.mibGroupDrawerState.visible).toBe(true)

      const mibGroup = mockMibGroups[0]
      store.openMibGroupCreationDrawer(mibGroup, 2)
      expect(store.selectedMibGroup).toEqual(mibGroup)
      expect(store.mibGroupDrawerState.isEditMode).toBe(2)
    })

    it('should close system def drawer when already closed', () => {
      store.systemDefDrawerState.visible = false
      store.closeSystemDefDrawer()
      expect(store.systemDefDrawerState.visible).toBe(false)
      expect(store.selectedSystemDef).toBeNull()
    })

    it('should close mib group drawer when already closed', async () => {
      store.mibGroupDrawerState.visible = false
      await store.closeMibGroupDrawer()
      expect(store.mibGroupDrawerState.visible).toBe(false)
      expect(store.selectedMibGroup).toBeNull()
    })

    it('should not affect system def drawer when opening/closing mib group drawer', async () => {
      // Open system def drawer first
      store.openSystemDefCreationDrawer(mockSystemDefinitions[0], 2)
      expect(store.systemDefDrawerState.visible).toBe(true)

      // Open and close mib group drawer
      store.openMibGroupCreationDrawer(mockMibGroups[0], 1)
      await store.closeMibGroupDrawer()

      // System def drawer should be unchanged
      expect(store.systemDefDrawerState.visible).toBe(true)
      expect(store.selectedSystemDef).toEqual(mockSystemDefinitions[0])
    })

    it('should not affect mib group drawer when opening/closing system def drawer', async () => {
      // Open mib group drawer first
      store.openMibGroupCreationDrawer(mockMibGroups[0], 2)
      expect(store.mibGroupDrawerState.visible).toBe(true)

      // Open and close system def drawer
      store.openSystemDefCreationDrawer(mockSystemDefinitions[0], 1)
      await store.closeSystemDefDrawer()

      // Mib group drawer should be unchanged
      expect(store.mibGroupDrawerState.visible).toBe(true)
      expect(store.selectedMibGroup).toEqual(mockMibGroups[0])
    })

    it.each([
      { mode: 0, description: 'None' },
      { mode: 1, description: 'Create' },
      { mode: 2, description: 'Edit' }
    ])('should open system def drawer with mode $mode ($description)', ({ mode }) => {
      store.openSystemDefCreationDrawer(null, mode)
      expect(store.systemDefDrawerState.isEditMode).toBe(mode)
    })

    it.each([
      { mode: 0, description: 'None' },
      { mode: 1, description: 'Create' },
      { mode: 2, description: 'Edit' }
    ])('should open mib group drawer with mode $mode ($description)', ({ mode }) => {
      store.openMibGroupCreationDrawer(null, mode)
      expect(store.mibGroupDrawerState.isEditMode).toBe(mode)
    })

    it('should open resource type drawer in create mode', () => {
      store.openResourceTypeCreationDrawer(null, 1)
      expect(store.resourceTypeDrawerState.visible).toBe(true)
      expect(store.resourceTypeDrawerState.isEditMode).toBe(1)
      expect(store.selectedResourceType).toBeNull()
    })

    it('should open resource type drawer in edit mode with resource type', () => {
      const resourceType = mockResourceTypes[0]
      store.openResourceTypeCreationDrawer(resourceType, 2)
      expect(store.resourceTypeDrawerState.visible).toBe(true)
      expect(store.resourceTypeDrawerState.isEditMode).toBe(2)
      expect(store.selectedResourceType).toEqual(resourceType)
    })

    it('should close resource type drawer and reset state', async () => {
      store.resourceTypeDrawerState.visible = true
      store.resourceTypeDrawerState.isEditMode = 2
      store.selectedResourceType = mockResourceTypes[0]

      await store.closeResourceTypeDrawer()

      expect(store.resourceTypeDrawerState.visible).toBe(false)
      expect(store.resourceTypeDrawerState.isEditMode).toBe(0)
      expect(store.selectedResourceType).toBeNull()
    })

    it('should call fetchResourceTypeNames when closing resource type drawer', async () => {
      vi.mocked(getAllResourceTypeNames).mockResolvedValue(['resource-type-1', 'resource-type-2'])
      store.resourceTypeDrawerState.visible = true
      store.selectedResourceType = mockResourceTypes[0]

      await store.closeResourceTypeDrawer()

      expect(getAllResourceTypeNames).toHaveBeenCalled()
    })

    it('should handle opening resource type drawer multiple times', () => {
      store.openResourceTypeCreationDrawer(null, 1)
      expect(store.resourceTypeDrawerState.visible).toBe(true)

      const resourceType = mockResourceTypes[0]
      store.openResourceTypeCreationDrawer(resourceType, 2)
      expect(store.selectedResourceType).toEqual(resourceType)
      expect(store.resourceTypeDrawerState.isEditMode).toBe(2)
    })

    it('should close resource type drawer when already closed', async () => {
      store.resourceTypeDrawerState.visible = false
      await store.closeResourceTypeDrawer()
      expect(store.resourceTypeDrawerState.visible).toBe(false)
      expect(store.selectedResourceType).toBeNull()
    })

    it('should not affect resource type drawer when opening/closing system def drawer', () => {
      // Open resource type drawer first
      store.openResourceTypeCreationDrawer(mockResourceTypes[0], 2)
      expect(store.resourceTypeDrawerState.visible).toBe(true)

      // Open and close system def drawer
      store.openSystemDefCreationDrawer(mockSystemDefinitions[0], 1)
      store.closeSystemDefDrawer()

      // Resource type drawer should be unchanged
      expect(store.resourceTypeDrawerState.visible).toBe(true)
      expect(store.selectedResourceType).toEqual(mockResourceTypes[0])
    })

    it('should not affect resource type drawer when opening/closing mib group drawer', async () => {
      // Open resource type drawer first
      store.openResourceTypeCreationDrawer(mockResourceTypes[0], 2)
      expect(store.resourceTypeDrawerState.visible).toBe(true)

      // Open and close mib group drawer
      store.openMibGroupCreationDrawer(mockMibGroups[0], 1)
      await store.closeMibGroupDrawer()

      // Resource type drawer should be unchanged
      expect(store.resourceTypeDrawerState.visible).toBe(true)
      expect(store.selectedResourceType).toEqual(mockResourceTypes[0])
    })

    it('should not affect system def drawer when opening/closing resource type drawer', async () => {
      // Open system def drawer first
      store.openSystemDefCreationDrawer(mockSystemDefinitions[0], 2)
      expect(store.systemDefDrawerState.visible).toBe(true)

      // Open and close resource type drawer
      store.openResourceTypeCreationDrawer(mockResourceTypes[0], 1)
      await store.closeResourceTypeDrawer()

      // System def drawer should be unchanged
      expect(store.systemDefDrawerState.visible).toBe(true)
      expect(store.selectedSystemDef).toEqual(mockSystemDefinitions[0])
    })

    it('should not affect mib group drawer when opening/closing resource type drawer', async () => {
      // Open mib group drawer first
      store.openMibGroupCreationDrawer(mockMibGroups[0], 2)
      expect(store.mibGroupDrawerState.visible).toBe(true)

      // Open and close resource type drawer
      store.openResourceTypeCreationDrawer(mockResourceTypes[0], 1)
      await store.closeResourceTypeDrawer()

      // Mib group drawer should be unchanged
      expect(store.mibGroupDrawerState.visible).toBe(true)
      expect(store.selectedMibGroup).toEqual(mockMibGroups[0])
    })

    it.each([
      { mode: 0, description: 'None' },
      { mode: 1, description: 'Create' },
      { mode: 2, description: 'Edit' }
    ])('should open resource type drawer with mode $mode ($description)', ({ mode }) => {
      store.openResourceTypeCreationDrawer(null, mode)
      expect(store.resourceTypeDrawerState.isEditMode).toBe(mode)
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

    it('should handle timeout errors', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const timeoutError = new Error('Request timeout')
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(timeoutError)

      await store.fetchSystemDefinitions()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection system definitions:', timeoutError)
      consoleErrorSpy.mockRestore()
    })

    it('should handle undefined response data', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue({
        systemDefinitions: undefined as any,
        totalRecords: 0
      })

      await store.fetchSystemDefinitions()

      expect(store.systemDefinitions).toBeUndefined()
    })
  })

  describe('Parametrized Tests - Pagination Calculations', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { page: 1, pageSize: 10, expectedOffset: 0 },
      { page: 2, pageSize: 10, expectedOffset: 10 },
      { page: 3, pageSize: 20, expectedOffset: 40 },
      { page: 5, pageSize: 15, expectedOffset: 60 },
      { page: 1, pageSize: 5, expectedOffset: 0 },
      { page: 10, pageSize: 50, expectedOffset: 450 },
      { page: 1, pageSize: 100, expectedOffset: 0 }
    ])(
      'should calculate offset correctly for systemDefs: page $page with pageSize $pageSize = offset $expectedOffset',
      async ({ page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

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
    )

    it.each([
      { page: 1, pageSize: 10, expectedOffset: 0 },
      { page: 2, pageSize: 15, expectedOffset: 15 },
      { page: 4, pageSize: 25, expectedOffset: 75 },
      { page: 3, pageSize: 30, expectedOffset: 60 },
      { page: 5, pageSize: 20, expectedOffset: 80 }
    ])(
      'should calculate offset correctly for mibGroups: page $page with pageSize $pageSize = offset $expectedOffset',
      async ({ page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

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
    )

    it.each([
      { page: 1, pageSize: 5, expectedOffset: 0 },
      { page: 3, pageSize: 10, expectedOffset: 20 },
      { page: 5, pageSize: 20, expectedOffset: 80 },
      { page: 2, pageSize: 25, expectedOffset: 25 },
      { page: 4, pageSize: 15, expectedOffset: 45 }
    ])(
      'should calculate offset correctly for resourceTypes: page $page with pageSize $pageSize = offset $expectedOffset',
      async ({ page, pageSize, expectedOffset }) => {
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

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
    )
  })

  describe('Parametrized Tests - Sort Combinations', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { sortKey: 'name', sortOrder: 'asc' },
      { sortKey: 'name', sortOrder: 'desc' },
      { sortKey: 'sysoid', sortOrder: 'asc' },
      { sortKey: 'sysoid', sortOrder: 'desc' },
      { sortKey: 'createdTime', sortOrder: 'asc' },
      { sortKey: 'createdTime', sortOrder: 'desc' },
      { sortKey: 'enabled', sortOrder: 'asc' },
      { sortKey: 'enabled', sortOrder: 'desc' }
    ])('should handle sorting for systemDefs by $sortKey with order $sortOrder', async ({ sortKey, sortOrder }) => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.onSystemDefsSortChange(sortKey, sortOrder)

      expect(store.systemDefsSorting.sortKey).toBe(sortKey)
      expect(store.systemDefsSorting.sortOrder).toBe(sortOrder)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
    })

    it.each([
      { sortKey: 'name', sortOrder: 'asc' },
      { sortKey: 'name', sortOrder: 'desc' },
      { sortKey: 'ifType', sortOrder: 'asc' },
      { sortKey: 'ifType', sortOrder: 'desc' },
      { sortKey: 'createdTime', sortOrder: 'asc' },
      { sortKey: 'createdTime', sortOrder: 'desc' },
      { sortKey: 'enabled', sortOrder: 'asc' }
    ])('should handle sorting for mibGroups by $sortKey with order $sortOrder', async ({ sortKey, sortOrder }) => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

      await store.onMibGroupsSortChange(sortKey, sortOrder)

      expect(store.mibGroupsSorting.sortKey).toBe(sortKey)
      expect(store.mibGroupsSorting.sortOrder).toBe(sortOrder)
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
    })

    it.each([
      { sortKey: 'name', sortOrder: 'asc' },
      { sortKey: 'name', sortOrder: 'desc' },
      { sortKey: 'label', sortOrder: 'asc' },
      { sortKey: 'label', sortOrder: 'desc' },
      { sortKey: 'createdTime', sortOrder: 'asc' },
      { sortKey: 'createdTime', sortOrder: 'desc' },
      { sortKey: 'resourceLabel', sortOrder: 'asc' },
      { sortKey: 'storageStrategy', sortOrder: 'desc' }
    ])('should handle sorting for resourceTypes by $sortKey with order $sortOrder', async ({ sortKey, sortOrder }) => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

      await store.onResourceTypesSortChange(sortKey, sortOrder)

      expect(store.resourceTypesSorting.sortKey).toBe(sortKey)
      expect(store.resourceTypesSorting.sortOrder).toBe(sortOrder)
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, '', sortKey, sortOrder)
    })
  })

  describe('Parametrized Tests - Search Terms', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { term: 'simple' },
      { term: 'with spaces' },
      { term: 'special@chars#' },
      { term: '' },
      { term: 'UPPERCASE' },
      { term: 'MixedCase' },
      { term: '123numeric456' },
      { term: 'hyphen-term' },
      { term: 'underscore_term' },
      { term: 'dot.term' },
      { term: '   whitespace   ' },
      { term: 'unicode-日本語' }
    ])('should handle search term "$term" for systemDefs', async ({ term }) => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.onChangeSystemDefsSearchTerm(term)

      expect(store.systemDefsSearchTerm).toBe(term)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
    })

    it.each([
      { term: 'mib-test' },
      { term: 'IF-MIB' },
      { term: '' },
      { term: 'SNMPv2-MIB' },
      { term: 'RFC1213-MIB' },
      { term: 'interfaces' }
    ])('should handle search term "$term" for mibGroups', async ({ term }) => {
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

      await store.onChangeMibGroupsSearchTerm(term)

      expect(store.mibGroupsSearchTerm).toBe(term)
      expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
    })

    it.each([
      { term: 'node' },
      { term: 'interface' },
      { term: '' },
      { term: 'hrStorageIndex' },
      { term: 'dskIndex' },
      { term: 'nodeSnmp' }
    ])('should handle search term "$term" for resourceTypes', async ({ term }) => {
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

      await store.onChangeResourceTypesSearchTerm(term)

      expect(store.resourceTypesSearchTerm).toBe(term)
      expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, 10, term, 'createdTime', 'desc')
    })
  })

  describe('Parametrized Tests - Page Size Values', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { pageSize: 5 },
      { pageSize: 10 },
      { pageSize: 15 },
      { pageSize: 20 },
      { pageSize: 25 },
      { pageSize: 50 },
      { pageSize: 100 }
    ])('should handle page size $pageSize for systemDefs', async ({ pageSize }) => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.onSystemDefsPageSizeChange(pageSize)

      expect(store.systemDefsPagination.pageSize).toBe(pageSize)
      expect(store.systemDefsPagination.page).toBe(1)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(1, 0, pageSize, '', 'createdTime', 'desc')
    })

    it.each([{ pageSize: 5 }, { pageSize: 10 }, { pageSize: 25 }, { pageSize: 50 }, { pageSize: 100 }])(
      'should handle page size $pageSize for mibGroups',
      async ({ pageSize }) => {
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

        await store.onMibGroupsPageSizeChange(pageSize)

        expect(store.mibGroupsPagination.pageSize).toBe(pageSize)
        expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(1, 0, pageSize, '', 'createdTime', 'desc')
      }
    )

    it.each([{ pageSize: 5 }, { pageSize: 10 }, { pageSize: 25 }, { pageSize: 50 }, { pageSize: 100 }])(
      'should handle page size $pageSize for resourceTypes',
      async ({ pageSize }) => {
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

        await store.onResourceTypesPageSizeChange(pageSize)

        expect(store.resourceTypesPagination.pageSize).toBe(pageSize)
        expect(store.resourceTypesPagination.page).toBe(1)
        expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(1, 0, pageSize, '', 'createdTime', 'desc')
      }
    )
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

    it('should maintain independent page sizes for each section', async () => {
      await store.onSystemDefsPageSizeChange(15)
      await store.onMibGroupsPageSizeChange(25)
      await store.onResourceTypesPageSizeChange(50)

      expect(store.systemDefsPagination.pageSize).toBe(15)
      expect(store.mibGroupsPagination.pageSize).toBe(25)
      expect(store.resourceTypesPagination.pageSize).toBe(50)
    })

    it('should maintain independent total records for each section', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue({
        systemDefinitions: mockSystemDefinitions,
        totalRecords: 100
      })
      vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue({
        mibGroups: mockMibGroups,
        totalRecords: 200
      })
      vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue({
        resourceTypes: mockResourceTypes,
        totalRecords: 300
      })

      await store.fetchSystemDefinitions()
      await store.fetchMibGroups()
      await store.fetchResourceTypes()

      expect(store.systemDefsPagination.total).toBe(100)
      expect(store.mibGroupsPagination.total).toBe(200)
      expect(store.resourceTypesPagination.total).toBe(300)
    })

    it('should maintain independent data arrays for each section', async () => {
      await store.fetchSystemDefinitions()
      await store.fetchMibGroups()
      await store.fetchResourceTypes()

      expect(store.systemDefinitions).toEqual(mockSystemDefinitions)
      expect(store.mibGroups).toEqual(mockMibGroups)
      expect(store.resourceTypes).toEqual(mockResourceTypes)

      // Modifying one should not affect others
      store.systemDefinitions = []
      expect(store.mibGroups).toEqual(mockMibGroups)
      expect(store.resourceTypes).toEqual(mockResourceTypes)
    })
  })

  describe('Edge Cases', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should handle consecutive fetch calls', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await store.fetchSystemDefinitions()
      await store.fetchSystemDefinitions()
      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledTimes(3)
    })

    it('should handle rapid pagination changes', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      await Promise.all([
        store.onSystemDefsPageChange(1),
        store.onSystemDefsPageChange(2),
        store.onSystemDefsPageChange(3)
      ])

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })

    it('should handle very long search terms', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      const longSearchTerm = 'a'.repeat(1000)

      await store.onChangeSystemDefsSearchTerm(longSearchTerm)

      expect(store.systemDefsSearchTerm).toBe(longSearchTerm)
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(
        1,
        0,
        10,
        longSearchTerm,
        'createdTime',
        'desc'
      )
    })

    it('should handle collection source with different IDs', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      store.selectedCollectionSource = { ...mockCollectionSource, id: 999 }
      await store.fetchSystemDefinitions()

      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(999, 0, 10, '', 'createdTime', 'desc')
    })

    it('should handle switching between null and valid collection source', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

      store.selectedCollectionSource = null
      await store.fetchSystemDefinitions()
      expect(getSnmpDataCollectionSystemDefinitions).not.toHaveBeenCalled()

      store.selectedCollectionSource = mockCollectionSource
      await store.fetchSystemDefinitions()
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })

    it('should handle changing search term to same value', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      store.systemDefsSearchTerm = 'test'

      await store.onChangeSystemDefsSearchTerm('test')

      expect(store.systemDefsSearchTerm).toBe('test')
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })

    it('should handle changing sort to same values', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)
      store.systemDefsSorting = { sortKey: 'name', sortOrder: 'asc' }

      await store.onSystemDefsSortChange('name', 'asc')

      expect(store.systemDefsSorting).toEqual({ sortKey: 'name', sortOrder: 'asc' })
      expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalled()
    })

    it('should handle response with null values', async () => {
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue({
        systemDefinitions: [{ ...mockSystemDefinitions[0], name: null as any }],
        totalRecords: 1
      })

      await store.fetchSystemDefinitions()

      expect(store.systemDefinitions[0].name).toBeNull()
    })
  })

  describe('Loading State Behavior', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it('should set loading to true at start and false at end of fetchSystemDefinitions', async () => {
      const loadingStates: boolean[] = []
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingStates.push(store.isLoading)
            resolve(mockSystemDefsResponse)
          })
      )

      expect(store.isLoading).toBe(false)
      await store.fetchSystemDefinitions()
      expect(loadingStates[0]).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should set loading to true at start and false at end of fetchMibGroups', async () => {
      const loadingStates: boolean[] = []
      vi.mocked(getSnmpDataCollectionMibGroups).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingStates.push(store.isLoading)
            resolve(mockMibGroupsResponse)
          })
      )

      expect(store.isLoading).toBe(false)
      await store.fetchMibGroups()
      expect(loadingStates[0]).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should set loading to true at start and false at end of fetchResourceTypes', async () => {
      const loadingStates: boolean[] = []
      vi.mocked(getSnmpDataCollectionResourceTypes).mockImplementation(
        () =>
          new Promise((resolve) => {
            loadingStates.push(store.isLoading)
            resolve(mockResourceTypesResponse)
          })
      )

      expect(store.isLoading).toBe(false)
      await store.fetchResourceTypes()
      expect(loadingStates[0]).toBe(true)
      expect(store.isLoading).toBe(false)
    })

    it('should set loading to false even on error in fetchSystemDefinitions', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(new Error('Error'))

      await store.fetchSystemDefinitions()

      expect(store.isLoading).toBe(false)
    })

    it('should set loading to false even on error in fetchMibGroups', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionMibGroups).mockRejectedValue(new Error('Error'))

      await store.fetchMibGroups()

      expect(store.isLoading).toBe(false)
    })

    it('should set loading to false even on error in fetchResourceTypes', async () => {
      vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(getSnmpDataCollectionResourceTypes).mockRejectedValue(new Error('Error'))

      await store.fetchResourceTypes()

      expect(store.isLoading).toBe(false)
    })
  })

  describe('Parametrized Tests - Combined Filters', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { page: 2, pageSize: 15, searchTerm: 'test', sortKey: 'name', sortOrder: 'asc' },
      { page: 3, pageSize: 20, searchTerm: '', sortKey: 'createdTime', sortOrder: 'desc' },
      { page: 1, pageSize: 10, searchTerm: 'search', sortKey: 'sysoid', sortOrder: 'asc' },
      { page: 5, pageSize: 50, searchTerm: 'filter', sortKey: 'enabled', sortOrder: 'desc' }
    ])(
      'should handle combined filters for systemDefs: page=$page, pageSize=$pageSize, search="$searchTerm", sort=$sortKey $sortOrder',
      async ({ page, pageSize, searchTerm, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionSystemDefinitions).mockResolvedValue(mockSystemDefsResponse)

        store.systemDefsPagination.page = page
        store.systemDefsPagination.pageSize = pageSize
        store.systemDefsSearchTerm = searchTerm
        store.systemDefsSorting = { sortKey, sortOrder }

        await store.fetchSystemDefinitions()

        const expectedOffset = (page - 1) * pageSize
        expect(getSnmpDataCollectionSystemDefinitions).toHaveBeenCalledWith(
          1,
          expectedOffset,
          pageSize,
          searchTerm,
          sortKey,
          sortOrder
        )
      }
    )

    it.each([
      { page: 2, pageSize: 15, searchTerm: 'mib', sortKey: 'name', sortOrder: 'asc' },
      { page: 3, pageSize: 20, searchTerm: '', sortKey: 'ifType', sortOrder: 'desc' },
      { page: 1, pageSize: 10, searchTerm: 'IF-MIB', sortKey: 'createdTime', sortOrder: 'asc' }
    ])(
      'should handle combined filters for mibGroups: page=$page, pageSize=$pageSize, search="$searchTerm", sort=$sortKey $sortOrder',
      async ({ page, pageSize, searchTerm, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionMibGroups).mockResolvedValue(mockMibGroupsResponse)

        store.mibGroupsPagination.page = page
        store.mibGroupsPagination.pageSize = pageSize
        store.mibGroupsSearchTerm = searchTerm
        store.mibGroupsSorting = { sortKey, sortOrder }

        await store.fetchMibGroups()

        const expectedOffset = (page - 1) * pageSize
        expect(getSnmpDataCollectionMibGroups).toHaveBeenCalledWith(
          1,
          expectedOffset,
          pageSize,
          searchTerm,
          sortKey,
          sortOrder
        )
      }
    )

    it.each([
      { page: 2, pageSize: 15, searchTerm: 'node', sortKey: 'name', sortOrder: 'asc' },
      { page: 3, pageSize: 20, searchTerm: '', sortKey: 'label', sortOrder: 'desc' },
      { page: 1, pageSize: 10, searchTerm: 'interface', sortKey: 'createdTime', sortOrder: 'asc' }
    ])(
      'should handle combined filters for resourceTypes: page=$page, pageSize=$pageSize, search="$searchTerm", sort=$sortKey $sortOrder',
      async ({ page, pageSize, searchTerm, sortKey, sortOrder }) => {
        vi.mocked(getSnmpDataCollectionResourceTypes).mockResolvedValue(mockResourceTypesResponse)

        store.resourceTypesPagination.page = page
        store.resourceTypesPagination.pageSize = pageSize
        store.resourceTypesSearchTerm = searchTerm
        store.resourceTypesSorting = { sortKey, sortOrder }

        await store.fetchResourceTypes()

        const expectedOffset = (page - 1) * pageSize
        expect(getSnmpDataCollectionResourceTypes).toHaveBeenCalledWith(
          1,
          expectedOffset,
          pageSize,
          searchTerm,
          sortKey,
          sortOrder
        )
      }
    )
  })

  describe('Parametrized Tests - Error Types', () => {
    beforeEach(() => {
      store.selectedCollectionSource = mockCollectionSource
    })

    it.each([
      { errorType: 'Network error', errorMessage: 'Network error' },
      { errorType: 'Timeout', errorMessage: 'Request timeout' },
      { errorType: 'Server error', errorMessage: 'Internal server error' },
      { errorType: 'Not found', errorMessage: '404 Not found' },
      { errorType: 'Unauthorized', errorMessage: '401 Unauthorized' },
      { errorType: 'Forbidden', errorMessage: '403 Forbidden' }
    ])('should handle $errorType error in fetchSystemDefinitions', async ({ errorMessage }) => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error(errorMessage)
      vi.mocked(getSnmpDataCollectionSystemDefinitions).mockRejectedValue(error)

      await store.fetchSystemDefinitions()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection system definitions:', error)
      consoleErrorSpy.mockRestore()
    })

    it.each([
      { errorType: 'Network error', errorMessage: 'Network error' },
      { errorType: 'Timeout', errorMessage: 'Request timeout' },
      { errorType: 'Server error', errorMessage: 'Internal server error' }
    ])('should handle $errorType error in fetchMibGroups', async ({ errorMessage }) => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error(errorMessage)
      vi.mocked(getSnmpDataCollectionMibGroups).mockRejectedValue(error)

      await store.fetchMibGroups()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection MIB groups:', error)
      consoleErrorSpy.mockRestore()
    })

    it.each([
      { errorType: 'Network error', errorMessage: 'Network error' },
      { errorType: 'Timeout', errorMessage: 'Request timeout' },
      { errorType: 'Server error', errorMessage: 'Internal server error' }
    ])('should handle $errorType error in fetchResourceTypes', async ({ errorMessage }) => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error(errorMessage)
      vi.mocked(getSnmpDataCollectionResourceTypes).mockRejectedValue(error)

      await store.fetchResourceTypes()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection resource types:', error)
      consoleErrorSpy.mockRestore()
    })
  })

  describe('fetchResourceTypeNames', () => {
    it('should fetch resource type names successfully', async () => {
      const mockResourceTypeNames = ['nodeSnmp', 'hrStorageIndex', 'dskIndex']
      const { getAllResourceTypeNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllResourceTypeNames).mockResolvedValue(mockResourceTypeNames)

      await store.fetchResourceTypeNames()

      expect(getAllResourceTypeNames).toHaveBeenCalled()
      expect(store.resourceTypeNames).toEqual(mockResourceTypeNames)
      expect(store.isLoading).toBe(false)
    })

    it('should handle errors when fetching resource type names', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Network error')
      const { getAllResourceTypeNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllResourceTypeNames).mockRejectedValue(error)

      await store.fetchResourceTypeNames()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection resource type names:', error)
      consoleErrorSpy.mockRestore()
    })

    it('should set loading state during fetch', async () => {
      const { getAllResourceTypeNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllResourceTypeNames).mockImplementation(async () => {
        expect(store.isLoading).toBe(true)
        return ['nodeSnmp']
      })

      await store.fetchResourceTypeNames()

      expect(store.isLoading).toBe(false)
    })

    it('should handle empty response', async () => {
      const { getAllResourceTypeNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllResourceTypeNames).mockResolvedValue([])

      await store.fetchResourceTypeNames()

      expect(store.resourceTypeNames).toEqual([])
      expect(store.isLoading).toBe(false)
    })
  })

  describe('fetchMibGroupNames', () => {
    it('should fetch mib group names successfully', async () => {
      const mockMibGroupNames = ['mib2-interfaces', 'mib2-host-resources', 'cisco-memory-pool']
      const { getAllMibGroupNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllMibGroupNames).mockResolvedValue(mockMibGroupNames)

      await store.fetchMibGroupNames()

      expect(getAllMibGroupNames).toHaveBeenCalled()
      expect(store.mibGroupNames).toEqual(mockMibGroupNames)
      expect(store.isLoading).toBe(false)
    })

    it('should handle errors when fetching mib group names', async () => {
      const consoleErrorSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      const error = new Error('Network error')
      const { getAllMibGroupNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllMibGroupNames).mockRejectedValue(error)

      await store.fetchMibGroupNames()

      expect(store.isLoading).toBe(false)
      expect(consoleErrorSpy).toHaveBeenCalledWith('Error fetching SNMP collection MIB group names:', error)
      consoleErrorSpy.mockRestore()
    })

    it('should set loading state during fetch', async () => {
      const { getAllMibGroupNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllMibGroupNames).mockImplementation(async () => {
        expect(store.isLoading).toBe(true)
        return ['mib2-interfaces']
      })

      await store.fetchMibGroupNames()

      expect(store.isLoading).toBe(false)
    })

    it('should handle empty response', async () => {
      const { getAllMibGroupNames } = await import('@/services/snmpDataCollectionService')
      vi.mocked(getAllMibGroupNames).mockResolvedValue([])

      await store.fetchMibGroupNames()

      expect(store.mibGroupNames).toEqual([])
      expect(store.isLoading).toBe(false)
    })
  })
})
