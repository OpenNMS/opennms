import {
  filterSnmpCollectionSources,
  getAllSnmpCollectionSourcesNamesAndIds
} from '@/services/snmpDataCollectionService'
import { SnmpDataCollectionStoreState } from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useSnmpDataCollectionStore = defineStore('useSnmpDataCollectionStore', {
  state: (): SnmpDataCollectionStoreState => ({
    isLoading: false,
    sources: [],
    selectedSource: null,
    sourcesPagination: { ...defaultPagination },
    sourcesSearchTerm: '',
    uploadedSourceNames: [],
    activeTab: 0,
    sourcesSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    }
  }),
  actions: {
    async fetchAllSourcesNames() {
      this.isLoading = true
      try {
        const response = await getAllSnmpCollectionSourcesNamesAndIds()
        this.uploadedSourceNames = response
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching all SNMP data collection source names:', error)
        this.isLoading = false
      }
    },
    async fetchSnmpCollectionSources() {
      this.isLoading = true
      try {
        const response = await filterSnmpCollectionSources(
          (this.sourcesPagination.page - 1) * this.sourcesPagination.pageSize,
          this.sourcesPagination.pageSize,
          this.sourcesSearchTerm,
          this.sourcesSorting.sortKey,
          this.sourcesSorting.sortOrder
        )
        this.sources = response.sources
        this.sourcesPagination.total = response.totalRecords
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching SNMP collection sources:', error)
        this.isLoading = false
      }
    },
    async onChangeSourcesSearchTerm(searchTerm: string) {
      this.sourcesSearchTerm = searchTerm
      await this.fetchSnmpCollectionSources()
    },
    async onSourcesSortChange(sortKey: string, sortOrder: string) {
      this.sourcesSorting.sortKey = sortKey
      this.sourcesSorting.sortOrder = sortOrder
      await this.fetchSnmpCollectionSources()
    },
    async onSourcePageChange(page: number) {
      this.sourcesPagination.page = page
      await this.fetchSnmpCollectionSources()
    },
    async onSourcePageSizeChange(pageSize: number) {
      this.sourcesPagination.page = 1
      this.sourcesPagination.pageSize = pageSize
      await this.fetchSnmpCollectionSources()
    },
    async refreshSourcesfilters() {
      this.sourcesPagination = { ...defaultPagination }
      this.sourcesSearchTerm = ''
      this.sourcesSorting.sortKey = 'createdTime'
      this.sourcesSorting.sortOrder = 'desc'
      await this.fetchSnmpCollectionSources()
    }
  }
})

