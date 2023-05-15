import {
  UsageStatisticsData,
  UsageStatisticsMetadata,
  UsageStatisticsMetadataItem,
  UsageStatisticsStatus
} from '@/types/usageStatistics'

export interface State {
  status: UsageStatisticsStatus
  metadata: UsageStatisticsMetadata
  statistics: object
}

const state: State = {
  status: { enabled: false } as UsageStatisticsStatus,
  metadata: {
    metadata: [] as UsageStatisticsMetadataItem[]
  } as UsageStatisticsMetadata,
  statistics: {} as UsageStatisticsData
}

export default state
