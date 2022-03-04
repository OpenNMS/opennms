import { State } from './state'
import { DeviceConfigBackup, QueryParameters } from '@/types'

const SAVE_DEVICE_CONFIG_BACKUPS = (state: State, deviceConfigBackups: DeviceConfigBackup[]) => {
  state.deviceConfigBackups = deviceConfigBackups
}

const MERGE_DEVICE_CONFIG_BACKUPS = (state: State, deviceConfigBackups: DeviceConfigBackup[]) => {
  state.deviceConfigBackups = [...state.deviceConfigBackups, ...deviceConfigBackups]
}

const UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS = (state: State, newQueryParams: QueryParameters) => {
  state.deviceConfigBackupQueryParams = { ...state.deviceConfigBackupQueryParams, ...newQueryParams }
}

export default {
  SAVE_DEVICE_CONFIG_BACKUPS,
  MERGE_DEVICE_CONFIG_BACKUPS,
  UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS
}
