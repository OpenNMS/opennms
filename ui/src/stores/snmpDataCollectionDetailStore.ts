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
        // Simulate API call
        const response: SnmpCollectionSource = await new Promise((resolve) =>
          setTimeout(
            () =>
              resolve({
                id: Number(id),
                name: 'Sample Source',
                vendor: 'Sample Vendor',
                description: 'This is a sample SNMP collection source.',
                enabled: true,
                createdTime: new Date(),
                lastModified: new Date(),
                uploadedBy: 'admin'
              }),
            1000
          )
        )
        this.selectedCollectionSource = response
      } catch (error) {
        console.error('Error fetching SNMP collection source by ID:', id, error)
      }
    }
  }
})

