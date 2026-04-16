import { UsageStatisticsMetadata } from '@/types/usageStatistics'
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

export interface ProductUpdateEnrollmentStatus {
  optedIn: boolean | null
  noticeAcknowledged: boolean | null
}

export interface ProductUpdateEnrollmentFormData {
  firstName: string
  lastName: string
  email: string
  company: string
  consent: boolean
}
