import { QueryParameters, VuexContext } from '@/types'
import useDownload from '@/hooks/useDownload'
import API from '@/services'
import { State } from './state'

const { downloadFile } = useDownload()

interface ContextWithState extends VuexContext {
  state: State
}

const getDeviceConfigBackups = async (context: ContextWithState) => {
  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  context.commit('SAVE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups)
  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const getAndMergeDeviceConfigBackups = async (context: ContextWithState) => {
  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  context.commit('MERGE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups)
  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const downloadDeviceConfigById = async (context: VuexContext, id: number) => {
  const file = await API.downloadDeviceConfigById(id)
  downloadFile(file)
}

const backupDeviceConfigByIds = async (context: VuexContext, ids: number[]) => {
  return await API.backupDeviceConfigByIds(ids)
}

const updateDeviceConfigBackupQueryParams = async (context: VuexContext, newQueryParams: QueryParameters) => {
  context.commit('UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS', newQueryParams)
}

export default {
  getDeviceConfigBackups,
  getAndMergeDeviceConfigBackups,
  updateDeviceConfigBackupQueryParams,
  downloadDeviceConfigById,
  backupDeviceConfigByIds
}
