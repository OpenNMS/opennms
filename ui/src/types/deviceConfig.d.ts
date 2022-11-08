import { QueryParameters } from '.'

export interface DeviceConfigBackup {
  id: number
  ipInterfaceId: number
  ipAddress: string
  deviceName: string
  location: string
  lastBackupDate: number
  lastUpdatedDate: number
  lastSucceededDate: number
  lastFailedDate: number
  backupStatus: string
  scheduledInterval: Record<string, string>
  fileName: string
  failureReason: string
  encoding: string
  configType: string
  configName: string
  nodeId: number
  nodeLabel: string
  operatingSystem: string
  isSuccessfulBackup: boolean
  nextScheduledBackupDate: number
  config: string
  monitoredServiceId: number
  serviceName: string
}

export interface DeviceConfigQueryParams extends QueryParameters {
  deviceName?: string
  ipAddress?: string
  ipInterfaceId?: number
  configType?: string
  createdAfter?: number
  createdBefore?: number
  location?: string
  status?: status
  pageEnter?: boolean
}

export type status = 'NONE' | 'SUCCESS' | 'FAILED'
