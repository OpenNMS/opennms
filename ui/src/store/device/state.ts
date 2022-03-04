import { DeviceConfigBackup, QueryParameters } from '@/types'

export interface State {
  deviceConfigBackups: DeviceConfigBackup[]
  deviceConfigBackupQueryParams: QueryParameters
}

const state: State = {
  deviceConfigBackups: [],
  deviceConfigBackupQueryParams: { offset: 0, limit: 20 }
}

export default state
