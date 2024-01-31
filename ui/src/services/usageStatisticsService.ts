import { rest } from './axiosInstances'
import { UsageStatisticsData, UsageStatisticsMetadata, UsageStatisticsStatus } from '@/types/usageStatistics'

const endpoint = '/datachoices'

const getUsageStatistics = async (): Promise<UsageStatisticsData | false> => {
  try {
    const url = `${endpoint}`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsMetadata = async (): Promise<UsageStatisticsMetadata | false> => {
  try {
    const url = `${endpoint}/meta`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const getUsageStatisticsStatus = async (): Promise<UsageStatisticsStatus | false> => {
  try {
    const url = `${endpoint}/status`
    const resp = await rest.get(url)
    return resp.data
  } catch (err) {
    return false
  }
}

const setUsageStatisticsStatus = async (enabled: boolean) : Promise<any | false> => {
  try {
    const status = {
      enabled
    }
    const url = `${endpoint}/status`
    const resp = await rest.post(url, status)
    return resp
  } catch (err) {
    return false
  }
}

export {
  getUsageStatistics,
  getUsageStatisticsMetadata,
  getUsageStatisticsStatus,
  setUsageStatisticsStatus
}
