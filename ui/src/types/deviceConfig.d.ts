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
  configType: defaultConfig | runningConfig
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
  configType?: defaultConfig | runningConfig
  createdAfter?: number
  createdBefore?: number
  status?: status
}

export type status = 'none' | 'success' | 'failed'
type defaultConfig = 'default'
type runningConfig = 'running'
