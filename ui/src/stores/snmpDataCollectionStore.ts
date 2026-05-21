import {
  filterSnmpCollectionSources,
  getAllSnmpCollectionSourcesNamesAndIds,
  getAllSnmpCollectionProfiles,
  deleteSnmpDataCollectionProfiles,
  createSnmpCollectionSource
} from '@/services/snmpDataCollectionService'
import { SnmpCollectionProfile, SnmpDataCollectionStoreState } from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

const defaultPagination = {
  page: 1,
  pageSize: 50,
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
    },
    profiles: [],
    selectedProfile: null,
    profilesPagination: { ...defaultPagination },
    profilesSearchTerm: '',
    profilesSorting: {
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
    async fetchSnmpCollectionProfiles() {
      this.isLoading = true
      try {
        const response = await getAllSnmpCollectionProfiles()
        this.profiles = response
        this.isLoading = false
      } catch (error) {
        console.error('Error fetching SNMP collection profiles:', error)
        this.isLoading = false
      }
    },
    onChangeProfilesSearchTerm(searchTerm: string) {
      this.profilesSearchTerm = searchTerm
      this.profilesPagination.page = 1
    },
    onProfilesSortChange(sortKey: string, sortOrder: string) {
      this.profilesSorting.sortKey = sortKey
      this.profilesSorting.sortOrder = sortOrder
      this.profilesPagination.page = 1
    },
    onProfilePageChange(page: number) {
      this.profilesPagination.page = page
    },
    onProfilePageSizeChange(pageSize: number) {
      this.profilesPagination.page = 1
      this.profilesPagination.pageSize = pageSize
    },
    async removeSnmpCollectionProfiles(ids: number[]): Promise<boolean> {
      return deleteSnmpDataCollectionProfiles(ids)
    },
    profilesForSource(sourceName: string): SnmpCollectionProfile[] {
      const normalizedSourceName = sourceName.trim().toLowerCase()

      if (!normalizedSourceName) {
        return []
      }

      return this.profiles.filter(profile => {
        if (!Array.isArray(profile.sourceNames)) {
          return false
        }
        return profile.sourceNames.some(profileSourceName =>
          profileSourceName.trim().toLowerCase() === normalizedSourceName
        )
      })
    },
    async createSnmpDataCollectionSource(name: string, profiles: string[]): Promise<number | null> {
      return createSnmpCollectionSource(name, profiles)
    }
  }
})
