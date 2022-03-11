import { State } from './state'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const SAVE_DEVICE_CONFIG_BACKUPS = (state: State, deviceConfigBackups: DeviceConfigBackup[]) => {
  state.deviceConfigBackups = deviceConfigBackups
}

const MERGE_DEVICE_CONFIG_BACKUPS = (state: State, deviceConfigBackups: DeviceConfigBackup[]) => {
  state.deviceConfigBackups = [...state.deviceConfigBackups, ...deviceConfigBackups]
}

const UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS = (state: State, newQueryParams: DeviceConfigQueryParams) => {
  state.deviceConfigBackupQueryParams = { ...state.deviceConfigBackupQueryParams, ...newQueryParams }
}

const SET_MODAL_DEVICE_CONFIG_BACKUP = (state: State, config: DeviceConfigBackup) => {
  state.modalDeviceConfigBackup = config
}

const SET_SELECTED_IDS = (state: State, ids: number[]) => {
  state.selectedIds = ids
}

const SAVE_VENDOR_OPTIONS = (state: State, options: string[]) => {
  state.vendorOptions = options
}

const SAVE_OS_IMAGE_OPTIONS = (state: State, options: string[]) => {
  state.osImageOptions = options
}

export default {
  SAVE_DEVICE_CONFIG_BACKUPS,
  MERGE_DEVICE_CONFIG_BACKUPS,
  UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS,
  SET_MODAL_DEVICE_CONFIG_BACKUP,
  SET_SELECTED_IDS,
  SAVE_VENDOR_OPTIONS,
  SAVE_OS_IMAGE_OPTIONS
}
