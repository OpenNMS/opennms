import { defineStore } from 'pinia'
import API from '@/services'
import {
  UsageStatisticsData,
  UsageStatisticsMetadata,
  UsageStatisticsMetadataItem,
  UsageStatisticsStatus
} from '@/types/usageStatistics'
import useSnackbar from '@/composables/useSnackbar'

const { showSnackBar } = useSnackbar()

export const useUsageStatisticsStore = defineStore('usageStatisticsStore', () => {
  const status = ref({ enabled: false } as UsageStatisticsStatus)
  const metadata = ref({ metadata: [] as UsageStatisticsMetadataItem[] } as UsageStatisticsMetadata)
  const statistics = ref({} as UsageStatisticsData)

  const getStatistics = async () => {
    const data = await API.getUsageStatistics()

    if (data) {
      statistics.value = data
    }
  }

  const getMetadata = async () => {
    const resp = await API.getUsageStatisticsMetadata()

    if (resp) {
      metadata.value = resp
    }
  }

  const getStatus = async () => {
    const resp = await API.getUsageStatisticsStatus()
    
    if (resp) {
      status.value = resp
    }
  }

  const enableSharing = async () => {
    const success = await updateSharing(true)

    if (success) {
      getStatus()
    }
  }

  const disableSharing = async () => {
    const success = await updateSharing(false)

    if (success) {
      getStatus()
    }
  }

  const updateSharing = async (enable: boolean) => {
    const resp = await API.setUsageStatisticsStatus(enable)

    const success = !!(resp && (resp.status === 200 || resp.status === 202))

    if (success) {
      if (enable) {
        showSnackBar({ msg: 'Usage Statistics Sharing is now enabled. Thank you for helping us improve OpenNMS.' })
      } else {
        showSnackBar({ msg: 'Usage Statistics Sharing is now disabled.' })
      }
    } else {
      showSnackBar({
        msg: `Error attempting to ${enable ? 'enable' : 'disable'} Usage Statistics Sharing.`,
        error: true
      })
    }

    return success
  }

  return {
    status,
    metadata,
    statistics,
    getStatistics,
    getMetadata,
    getStatus,
    enableSharing,
    disableSharing
  }
})
