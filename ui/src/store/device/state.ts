import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

export interface State {
  deviceConfigBackups: DeviceConfigBackup[]
  deviceConfigBackupQueryParams: DeviceConfigQueryParams
  modalDeviceConfigBackup: DeviceConfigBackup
  selectedIds: number[]
  vendorOptions: string[]
  backupStatusOptions: string[]
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
  backupStatusOptions: ['success', 'failed', 'no backup'],
  osImageOptions: [],
  deviceConfigTotal: 'N/A',
  historyModalBackups: []
}

export default state
