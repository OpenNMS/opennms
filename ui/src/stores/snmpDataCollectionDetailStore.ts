import {
  getAllMibGroupNames,
  getAllResourceTypeNames,
  getSnmpDataCollectionMibGroups,
  getSnmpDataCollectionResourceTypes,
  getSnmpDataCollectionSourceById,
  getSnmpDataCollectionSystemDefinitions
} from '@/services/snmpDataCollectionService'
import { CreateEditMode } from '@/types'
import {
  SnmpCollectionDetailStoreState,
  SnmpCollectionMibGroup,
  SnmpCollectionResourceType,
  SnmpCollectionSource,
  SnmpCollectionSystemDef
} from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useSnmpDataCollectionDetailStore = defineStore('useSnmpDataCollectionDetailStore', {
  state: (): SnmpCollectionDetailStoreState => ({
    isLoading: false,
    selectedCollectionSource: null,
    systemDefinitions: [],
    systemDefsPagination: { ...defaultPagination },
    systemDefsSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
    systemDefsSearchTerm: '',
    resourceTypes: [],
    resourceTypesPagination: { ...defaultPagination },
    resourceTypesSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
    resourceTypesSearchTerm: '',
    mibGroups: [],
    mibGroupsPagination: { ...defaultPagination },
    mibGroupsSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
    mibGroupsSearchTerm: '',
    resourceTypeNames: [],
    mibGroupNames: [],
    selectedSystemDef: null,
    selectedMibGroup: null,
    selectedResourceType: null,
    activeTab: 0,
    systemDefDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None
    },
    resourceTypeDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None
    },
    mibGroupDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None
    }
  }),
  actions: {
    setSelectedCollectionSource(source: SnmpCollectionSource | null) {
      this.selectedCollectionSource = source
    },
    async fetchResourceTypeNames() {
      this.isLoading = true
      try {
        const response = await getAllResourceTypeNames()
        this.resourceTypeNames = response
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching SNMP collection resource type names:', error)
        this.isLoading = false
      }
    },
    async fetchMibGroupNames() {
      this.isLoading = true
      try {
        const response = await getAllMibGroupNames()
        this.mibGroupNames = response
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching SNMP collection MIB group names:', error)
        this.isLoading = false
      }
    },
    async fetchCollectionSourceById(id: string) {
      this.isLoading = true
      try {
        const response = await getSnmpDataCollectionSourceById(Number(id))
        this.selectedCollectionSource = response
        await this.fetchResourceTypes()
        await this.fetchMibGroups()
        await this.fetchSystemDefinitions()
        await this.fetchResourceTypeNames()
        await this.fetchMibGroupNames()
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching SNMP collection source by ID:', id, error)
        this.isLoading = false
      }
    },
    async fetchSystemDefinitions() {
      if (this.selectedCollectionSource) {
        this.isLoading = true
        try {
          const response = await getSnmpDataCollectionSystemDefinitions(
            this.selectedCollectionSource.id,
            (this.systemDefsPagination.page - 1) * this.systemDefsPagination.pageSize,
            this.systemDefsPagination.pageSize,
            this.systemDefsSearchTerm,
            this.systemDefsSorting.sortKey,
            this.systemDefsSorting.sortOrder
          )
          this.systemDefinitions = response.systemDefinitions
          this.systemDefsPagination.total = response.totalRecords
          this.isLoading = false
        } catch (error) {
          console.error('Error fetching SNMP collection system definitions:', error)
          this.isLoading = false
        }
      }
    },
    async onSystemDefsPageChange(page: number) {
      this.systemDefsPagination.page = page
      await this.fetchSystemDefinitions()
    },
    async onSystemDefsPageSizeChange(pageSize: number) {
      this.systemDefsPagination.pageSize = pageSize
      this.systemDefsPagination.page = 1
      await this.fetchSystemDefinitions()
    },

    async onChangeSystemDefsSearchTerm(value: string) {
      this.systemDefsSearchTerm = value
      this.systemDefsPagination.page = 1
      await this.fetchSystemDefinitions()
    },
    async onSystemDefsSortChange(sortKey: string, sortOrder: string) {
      this.systemDefsSorting.sortKey = sortKey
      this.systemDefsSorting.sortOrder = sortOrder
      await this.fetchSystemDefinitions()
    },
    async fetchMibGroups() {
      if (this.selectedCollectionSource) {
        this.isLoading = true
        try {
          const response = await getSnmpDataCollectionMibGroups(
            this.selectedCollectionSource.id,
            (this.mibGroupsPagination.page - 1) * this.mibGroupsPagination.pageSize,
            this.mibGroupsPagination.pageSize,
            this.mibGroupsSearchTerm,
            this.mibGroupsSorting.sortKey,
            this.mibGroupsSorting.sortOrder
          )
          this.mibGroups = response.mibGroups
          this.mibGroupsPagination.total = response.totalRecords
          this.isLoading = false
        } catch (error) {
          console.error('Error fetching SNMP collection MIB groups:', error)
          this.isLoading = false
        }
      }
    },
    async onMibGroupsPageChange(page: number) {
      this.mibGroupsPagination.page = page
      await this.fetchMibGroups()
    },
    async onMibGroupsPageSizeChange(pageSize: number) {
      this.mibGroupsPagination.pageSize = pageSize
      this.mibGroupsPagination.page = 1
      await this.fetchMibGroups()
    },
    async onChangeMibGroupsSearchTerm(value: string) {
      this.mibGroupsSearchTerm = value
      this.mibGroupsPagination.page = 1
      await this.fetchMibGroups()
    },
    async onMibGroupsSortChange(sortKey: string, sortOrder: string) {
      this.mibGroupsSorting.sortKey = sortKey
      this.mibGroupsSorting.sortOrder = sortOrder
      await this.fetchMibGroups()
    },
    async resetMibGroupsFilters() {
      this.mibGroupsSearchTerm = ''
      this.mibGroupsPagination.page = 1
      this.mibGroupsSorting.sortKey = 'createdTime'
      this.mibGroupsSorting.sortOrder = 'desc'
      await this.fetchMibGroups()
    },
    async resetSystemDefinitionsFilters() {
      this.systemDefinitions = []
      this.systemDefsPagination = { ...defaultPagination }
      this.systemDefsSorting = {
        sortOrder: 'desc',
        sortKey: 'createdTime'
      }
      this.systemDefsSearchTerm = ''
      await this.fetchSystemDefinitions()
    },
    async fetchResourceTypes() {
      if (this.selectedCollectionSource) {
        this.isLoading = true
        try {
          const response = await getSnmpDataCollectionResourceTypes(
            this.selectedCollectionSource.id,
            (this.resourceTypesPagination.page - 1) * this.resourceTypesPagination.pageSize,
            this.resourceTypesPagination.pageSize,
            this.resourceTypesSearchTerm,
            this.resourceTypesSorting.sortKey,
            this.resourceTypesSorting.sortOrder
          )
          // Assuming the API returns resource types in a similar manner
          this.resourceTypes = response.resourceTypes
          this.resourceTypesPagination.total = response.totalRecords
          this.isLoading = false
        } catch (error) {
          console.error('Error fetching SNMP collection resource types:', error)
          this.isLoading = false
        }
      }
    },
    async onResourceTypesPageChange(page: number) {
      this.resourceTypesPagination.page = page
      await this.fetchResourceTypes()
    },
    async onResourceTypesPageSizeChange(pageSize: number) {
      this.resourceTypesPagination.pageSize = pageSize
      this.resourceTypesPagination.page = 1
      await this.fetchResourceTypes()
    },
    async onChangeResourceTypesSearchTerm(value: string) {
      this.resourceTypesSearchTerm = value
      this.resourceTypesPagination.page = 1
      await this.fetchResourceTypes()
    },
    async onResourceTypesSortChange(sortKey: string, sortOrder: string) {
      this.resourceTypesSorting.sortKey = sortKey
      this.resourceTypesSorting.sortOrder = sortOrder
      await this.fetchResourceTypes()
    },
    async resetResourceTypesFilters() {
      this.resourceTypes = []
      this.resourceTypesPagination = { ...defaultPagination }
      this.resourceTypesSorting = {
        sortOrder: 'desc',
        sortKey: 'createdTime'
      }
      this.resourceTypesSearchTerm = ''
      await this.fetchResourceTypes()
    },
    openSystemDefCreationDrawer(systemDef: SnmpCollectionSystemDef | null = null, isEditMode: CreateEditMode) {
      this.selectedSystemDef = systemDef
      this.systemDefDrawerState.visible = true
      this.systemDefDrawerState.isEditMode = isEditMode
    },
    closeSystemDefDrawer() {
      this.selectedSystemDef = null
      this.systemDefDrawerState.visible = false
      this.systemDefDrawerState.isEditMode = CreateEditMode.None
    },
    openMibGroupCreationDrawer(mibGroup: SnmpCollectionMibGroup | null = null, isEditMode: CreateEditMode) {
      this.selectedMibGroup = mibGroup
      this.mibGroupDrawerState.visible = true
      this.mibGroupDrawerState.isEditMode = isEditMode
    },
    async closeMibGroupDrawer() {
      this.selectedMibGroup = null
      this.mibGroupDrawerState.visible = false
      this.mibGroupDrawerState.isEditMode = CreateEditMode.None
      await this.fetchMibGroupNames()
    },
    openResourceTypeCreationDrawer(resourceType: SnmpCollectionResourceType | null = null, isEditMode: CreateEditMode) {
      this.selectedResourceType = resourceType
      this.resourceTypeDrawerState.visible = true
      this.resourceTypeDrawerState.isEditMode = isEditMode
    },
    async closeResourceTypeDrawer() {
      this.selectedResourceType = null
      this.resourceTypeDrawerState.visible = false
      this.resourceTypeDrawerState.isEditMode = CreateEditMode.None
      await this.fetchResourceTypeNames()
    }
  }
})

