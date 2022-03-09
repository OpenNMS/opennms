import { VuexContext } from '@/types'
import useDownload from '@/hooks/useDownload'
import API from '@/services'
import { State } from './state'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const { downloadFile } = useDownload()

interface ContextWithState extends VuexContext {
  state: State
}

const getDeviceConfigBackupObjById = (deviceConfigs: DeviceConfigBackup[], id: number) => {
  return deviceConfigs.filter((dcb) => dcb.id === id)[0]
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

const downloadSelectedDevices = async (contextWithState: ContextWithState) => {
  const ids = contextWithState.state.selectedIds
  const file = await API.downloadDeviceConfigs(ids)
  downloadFile(file)
}

const backupSelectedDevices = async (contextWithState: ContextWithState) => {
  const ids = contextWithState.state.selectedIds
  const configs = contextWithState.state.deviceConfigBackups

  if (ids.length === 1) {
    const config = getDeviceConfigBackupObjById(configs, ids[0])
    return await API.backupDeviceConfig(config)
  } else {
    // backup multiple configs?
  }

}

const updateDeviceConfigBackupQueryParams = async (context: VuexContext, newQueryParams: DeviceConfigQueryParams) => {
  context.commit('UPDATE_DEVICE_CONFIG_BACKUP_QUERY_PARAMS', newQueryParams)
}

const setModalDeviceConfigBackup = async (context: VuexContext, config: DeviceConfigBackup) => {
  context.commit('SET_MODAL_DEVICE_CONFIG_BACKUP', config)
}

const setSelectedIds = (contextWithState: ContextWithState, idsOrAll: number[] | 'all') => {
  const configs = contextWithState.state.deviceConfigBackups

  if (idsOrAll === 'all') {
    const selectedIds = configs.map((dcb) => dcb.id)
    contextWithState.commit('SET_SELECTED_IDS', selectedIds)

    if (configs.length === 1) {
      contextWithState.commit('SET_MODAL_DEVICE_CONFIG_BACKUP', configs[0])
    }
  } else {
    contextWithState.commit('SET_SELECTED_IDS', idsOrAll)
    if (idsOrAll.length === 1) {
      contextWithState.commit('SET_MODAL_DEVICE_CONFIG_BACKUP', getDeviceConfigBackupObjById(configs, idsOrAll[0]))
    }
  }
}

export default {
  getDeviceConfigBackups,
  getAndMergeDeviceConfigBackups,
  updateDeviceConfigBackupQueryParams,
  downloadSelectedDevices,
  backupSelectedDevices,
  setSelectedIds,
  setModalDeviceConfigBackup
}
