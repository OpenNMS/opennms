import { DeviceConfigBackup, DeviceConfigQueryParams, status } from '@/types/deviceConfig'

export interface State {
  deviceConfigBackups: DeviceConfigBackup[]
  deviceConfigBackupQueryParams: DeviceConfigQueryParams
  modalDeviceConfigBackup: DeviceConfigBackup
  selectedIds: number[]
  vendorOptions: string[]
  backupStatusOptions: status[]
  osImageOptions: string[]
  deviceConfigTotal: string
  historyModalBackups: DeviceConfigBackup[]
}

const state: State = {
  deviceConfigBackups: [],
  deviceConfigBackupQueryParams: { offset: 0, limit: 20 },
  modalDeviceConfigBackup: {} as DeviceConfigBackup,
  selectedIds: [],
  vendorOptions: [],
  backupStatusOptions: ['SUCCESS', 'FAILED', 'NONE'],
  osImageOptions: [],
  deviceConfigTotal: 'N/A',
  historyModalBackups: []
}

export default state
