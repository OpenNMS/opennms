import { data } from '@/components/SnmpDataCollection/data'
import { SnmpDataCollectionStoreState } from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 10,
  total: 0
}

export const useSnmpDataCollectionStore = defineStore('useSnmpDataCollectionStore', {
  state: (): SnmpDataCollectionStoreState => ({
    sources: [],
    selectedSource: null,
    sourcesPagination: { ...defaultPagination },
    sourcesSearchTerm: '',
    sourcesSorting: {
      sortOrder: 'desc',
      sortKey: 'createdTime'
    }
  }),
  actions: {
    async fetchSnmpCollectionSources() {
      // Placeholder for fetching SNMP collection sources from an API
      // You would typically make an API call here and update the state accordingly
      console.log('Fetching SNMP Collection Sources...')
      this.sources = data
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

