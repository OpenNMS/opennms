import {
  getSnmpDataCollectionResourceTypes,
  getSnmpDataCollectionSourceById,
  getSnmpDataCollectionSystemDefinitions
} from '@/services/snmpDataCollectionService'
import { SnmpCollectionDetailState, SnmpCollectionSource } from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useSnmpDataCollectionDetailStore = defineStore('useSnmpDataCollectionDetailStore', {
  state: (): SnmpCollectionDetailState => ({
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
    resourceTypesSearchTerm: ''
  }),
  actions: {
    setSelectedCollectionSource(source: SnmpCollectionSource | null) {
      this.selectedCollectionSource = source
    },
    async fetchCollectionSourceById(id: string) {
      this.isLoading = true
      try {
        const response = await getSnmpDataCollectionSourceById(Number(id))
        this.selectedCollectionSource = response
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
    }
  }
})

