import { UsageStatisticsMetadata } from '@/types/usageStatistics';
export interface UsageStatisticsStatus {
  enabled: boolean | null,
  initialNoticeAcknowledged?: boolean | null
}

export interface UsageStatisticsData {
  [key: string]: any
}

export interface UsageStatisticsMetadataItem {
  key: string
  name: string
  description: string
  datatype: string
}

export interface UsageStatisticsMetadata {
  metadata: UsageStatisticsMetadataItem[]
}
