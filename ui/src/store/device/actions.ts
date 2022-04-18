import { VuexContext } from '@/types'
import useDownload from '@/composables/useDownload'
import useSnackbar from '@/composables/useSnackbar'
import API from '@/services'
import { State } from './state'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const { downloadFile } = useDownload()
const { showSnackBar } = useSnackbar()

interface ContextWithState extends VuexContext {
  state: State
}

const getDeviceConfigBackupObjByIds = (deviceConfigs: DeviceConfigBackup[], ids: number[]) => {
  return deviceConfigs.filter((dcb) => ids.includes(dcb.id))
}

const getDeviceConfigBackups = async (context: ContextWithState) => {
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  if (deviceConfigBackups) {
    context.commit('SAVE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups.data || [])
    context.commit('SAVE_DEVICE_CONFIG_TOTAL', deviceConfigBackups.headers['content-range'])
  }
}

const getHistoryByIpInterface = async (context: ContextWithState) => {
  const modalDeviceConfigIpInterface = context.state.modalDeviceConfigBackup.ipInterfaceId
  const historyModalBackups = await API.getHistoryByIpInterface(modalDeviceConfigIpInterface)
  context.commit('SET_HISTORY_MODAL_BACKUPS', historyModalBackups)
}

const getAndMergeDeviceConfigBackups = async (context: ContextWithState) => {
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  context.commit('MERGE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups)
}

const downloadByConfig = async (context: VuexContext, config: DeviceConfigBackup | DeviceConfigBackup[]) => {
  const isSingleDeviceBackup = (config: DeviceConfigBackup | DeviceConfigBackup[]): config is DeviceConfigBackup => {
    return (config as DeviceConfigBackup).id !== undefined
  }

  if (isSingleDeviceBackup(config)) {
    const file = await API.downloadDeviceConfigs([config.id])
    if (file) downloadFile(file)
  } else {
    const ids = config.map((x) => x.id)
    const file = await API.downloadDeviceConfigs(ids)
    if (file) downloadFile(file)
  }
}

const downloadSelectedDevices = async (contextWithState: ContextWithState) => {
  const ids = contextWithState.state.selectedIds
  const file = await API.downloadDeviceConfigs(ids)
  if (file) downloadFile(file)
}

const backupSelectedDevices = async (contextWithState: ContextWithState) => {
  const ids = contextWithState.state.selectedIds
  const configs = contextWithState.state.deviceConfigBackups

  const configsForBackup = getDeviceConfigBackupObjByIds(configs, ids)
  const resp = await API.backupDeviceConfig(configsForBackup)
  const success = resp && (resp.status === 200 || resp.status === 202)

  if (success) {
    showSnackBar({
      msg: 'Device backup triggered.'
    })
  } else {
    showSnackBar({
      msg: 'Device backup not triggered.',
      error: true
    })
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
      contextWithState.commit('SET_MODAL_DEVICE_CONFIG_BACKUP', getDeviceConfigBackupObjByIds(configs, idsOrAll)[0])
    }
  }
}

const getVendorOptions = async (context: VuexContext) => {
  const vendorOptions = await API.getVendorOptions()
  context.commit('SAVE_VENDOR_OPTIONS', vendorOptions)
}

const getOsImageOptions = async (context: VuexContext) => {
  const osImageOptions = await API.getOsImageOptions()
  context.commit('SAVE_OS_IMAGE_OPTIONS', osImageOptions)
}

export default {
  getDeviceConfigBackups,
  getAndMergeDeviceConfigBackups,
  updateDeviceConfigBackupQueryParams,
  downloadSelectedDevices,
  downloadByConfig,
  backupSelectedDevices,
  setSelectedIds,
  setModalDeviceConfigBackup,
  getVendorOptions,
  getOsImageOptions,
  getHistoryByIpInterface
}
