import {
  getSnmpDataCollectionMibGroups,
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
    mibGroups: [],
    mibGroupsPagination: { ...defaultPagination },
    mibGroupsSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    },
    mibGroupsSearchTerm: ''
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
    }
  }
})

