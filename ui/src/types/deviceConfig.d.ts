import { QueryParameters } from '.'

export interface DeviceConfigBackup {
  id: number
  ipInterfaceId: number
  ipAddress: string
  deviceName: string
  location: string
  createdTime: string
  lastUpdatedDate: string
  lastSucceededDate: string
  lastFailedDate: string
  backupStatus: string
  scheduleDate: string
  scheduleInterval: string
  fileName: string
  failureReason: string
  encoding: string
  configType?: defaultConfig | runningConfig
  nodeId: number
  nodeLabel: string
  operatingSystem: string
  isSuccessfulBackup: boolean
  nextScheduledBackupDate: string
  config: string
}

export interface DeviceConfigQueryParams extends QueryParameters {
  deviceName?: string
  ipAddress?: string
  ipInterfaceId?: number
  configType?: defaultConfig | runningConfig
  createdAfter?: number
  createdBefore?: number
}

type defaultConfig = 'default'
type runningConfig = 'running'
