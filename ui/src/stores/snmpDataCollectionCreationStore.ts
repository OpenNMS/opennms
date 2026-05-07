import {
  getAllMibGroupNames,
  getAllResourceTypeNames,
  getAllSnmpCollectionSourcesNamesAndIds
} from '@/services/snmpDataCollectionService'
import { CreateEditMode } from '@/types'
import { SnmpDataCollectionCreationStoreState } from '@/types/snmpDataCollection'
import { IAutocompleteItemType } from '@featherds/autocomplete'
import { defineStore } from 'pinia'

export const useSnmpDataCollectionCreationStore = defineStore('useSnmpDataCollectionCreationStore', {
  state: (): SnmpDataCollectionCreationStoreState => ({
    selectedCollectionSource: null,
    isLoading: false,
    resourceTypeNames: [],
    mibGroupNames: [],
    configForm: {
      systemDef: [],
      mibGroup: [],
      resourceType: []
    },
    uploadedSourceNames: [],
    systemDefDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None,
      systemDefIndex: -1
    },
    resourceTypeDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None,
      resourceTypeIndex: -1
    },
    mibGroupDrawerState: {
      visible: false,
      isEditMode: CreateEditMode.None,
      mibGroupIndex: -1 
    }
  }),
  actions: {
    // initialization action to fetch necessary data for the creation form
    async initializeCreationForm() {
      try {
        await Promise.all([this.fetchAllSourcesNames(), this.fetchMibGroupNames(), this.fetchResourceTypeNames()])
      } catch (error) {
        console.error('Error initializing SNMP data collection creation form:', error)
        this.isLoading = false
      }
    },
    setSelectedCollectionSource(item: IAutocompleteItemType) {
      this.selectedCollectionSource = item
    },
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
    }
  }
})

