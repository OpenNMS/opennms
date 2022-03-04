import { QueryParameters } from '.'

export interface DeviceConfigBackup {
  id: number
  ipInterfaceId: number
  ipAddress: string
  deviceName: string
  location: string
  createdTime: string
  lastUpdated: string
  lastSucceeded: string
  lastFailed: string
  backupStatus: string
  scheduleDate: string
  scheduleInterval: string
  fileName: string
  failureReason: string
  encoding: string
}

export interface DeviceConfigQueryParams extends QueryParameters {
  deviceName?: string
  ipAddress?: string
  ipInterfaceId?: number
  configType?: string
  createdAfter?: number
  createdBefore?: number
}
