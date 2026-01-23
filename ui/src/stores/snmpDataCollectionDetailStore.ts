import { getSnmpDataCollectionSourceById } from '@/services/snmpDataCollectionService'
import { SnmpCollectionDetailState, SnmpCollectionSource } from '@/types/snmpDataCollection'
import { defineStore } from 'pinia'

export const useSnmpDataCollectionDetailStore = defineStore('useSnmpDataCollectionDetailStore', {
  state: (): SnmpCollectionDetailState => ({
    selectedCollectionSource: null
  }),
  actions: {
    setSelectedCollectionSource(source: SnmpCollectionSource | null) {
      this.selectedCollectionSource = source
    },
    async fetchCollectionSourceById(id: string) {
      // Placeholder for actual API call to fetch SNMP collection source by ID
      try {
        const response = await getSnmpDataCollectionSourceById(Number(id))
        this.selectedCollectionSource = response
      } catch (error) {
        console.error('Error fetching SNMP collection source by ID:', id, error)
      }
    }
  }
})

