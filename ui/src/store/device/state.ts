import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

export interface State {
  deviceConfigBackups: DeviceConfigBackup[]
  deviceConfigBackupQueryParams: DeviceConfigQueryParams,
  modalDeviceConfigBackup: DeviceConfigBackup
  selectedIds: number[]
}

const state: State = {
  deviceConfigBackups: [],
  deviceConfigBackupQueryParams: { offset: 0, limit: 20 },
  modalDeviceConfigBackup: {} as DeviceConfigBackup,
  selectedIds: []
}

export default state
