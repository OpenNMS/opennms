import {
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
    systemDefsSearchTerm: ''
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
    }
  }
})

