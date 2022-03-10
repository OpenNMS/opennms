import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

export interface State {
  deviceConfigBackups: DeviceConfigBackup[]
  deviceConfigBackupQueryParams: DeviceConfigQueryParams,
  modalDeviceConfigBackup: DeviceConfigBackup
  selectedIds: number[]
  vendorOptions: string[]
  backupStatusOptions: string[],
  osImageOptions: string[]
}

const state: State = {
  deviceConfigBackups: [],
  deviceConfigBackupQueryParams: { offset: 0, limit: 20 },
  modalDeviceConfigBackup: {} as DeviceConfigBackup,
  selectedIds: [],
  vendorOptions: ['Aruba', 'Cisco', 'Juniper', 'OpenNMS'],
  backupStatusOptions: [],
  osImageOptions: []
}

export default state
