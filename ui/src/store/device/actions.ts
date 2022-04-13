import { VuexContext } from '@/types'
import useDownload from '@/hooks/useDownload'
import API from '@/services'
import { State } from './state'
import { DeviceConfigBackup, DeviceConfigQueryParams } from '@/types/deviceConfig'

const { downloadFile } = useDownload()

interface ContextWithState extends VuexContext {
  state: State
}

const getDeviceConfigBackupObjByIds = (deviceConfigs: DeviceConfigBackup[], ids: number[]) => {
  return deviceConfigs.filter((dcb) => ids.includes(dcb.id))
}

const getDeviceConfigBackups = async (context: ContextWithState) => {
  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  if (deviceConfigBackups) {
    context.commit('SAVE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups.data || [])
    context.commit('SAVE_DEVICE_CONFIG_TOTAL', deviceConfigBackups.headers['content-range'])
  }
  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const getHistoryByIpInterface = async (context: ContextWithState) => {
  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })
  const modalDeviceConfigIpInterface = context.state.modalDeviceConfigBackup.ipInterfaceId
  const historyModalBackups = await API.getHistoryByIpInterface(modalDeviceConfigIpInterface)
  context.commit('SET_HISTORY_MODAL_BACKUPS', historyModalBackups)
  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const getAndMergeDeviceConfigBackups = async (context: ContextWithState) => {
  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })
  const deviceConfigBackups = await API.getDeviceConfigBackups(context.state.deviceConfigBackupQueryParams)
  context.commit('MERGE_DEVICE_CONFIG_BACKUPS', deviceConfigBackups)
  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const downloadByConfig = async (context: VuexContext, config: DeviceConfigBackup | DeviceConfigBackup[]) => {
  const isSingleDeviceBackup = (config: DeviceConfigBackup | DeviceConfigBackup[]): config is DeviceConfigBackup => {
    return (config as DeviceConfigBackup).id !== undefined
  }

  context.dispatch('spinnerModule/setSpinnerState', true, { root: true })

  if (isSingleDeviceBackup(config)) {
    const file = await API.downloadDeviceConfigs([config.id])
    if (file) downloadFile(file)
  } else {
    const ids = config.map((x) => x.id)
    const file = await API.downloadDeviceConfigs(ids)
    if (file) downloadFile(file)
  }

  context.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const downloadSelectedDevices = async (contextWithState: ContextWithState) => {
  contextWithState.dispatch('spinnerModule/setSpinnerState', true, { root: true })

  const ids = contextWithState.state.selectedIds
  const file = await API.downloadDeviceConfigs(ids)
  if (file) downloadFile(file)

  contextWithState.dispatch('spinnerModule/setSpinnerState', false, { root: true })
}

const backupSelectedDevices = async (contextWithState: ContextWithState) => {
  const ids = contextWithState.state.selectedIds
  const configs = contextWithState.state.deviceConfigBackups
  contextWithState.dispatch('spinnerModule/setSpinnerState', true, { root: true })

  const configsForBackup = getDeviceConfigBackupObjByIds(configs, ids)
  const resp = await API.backupDeviceConfig(configsForBackup)
  contextWithState.dispatch('spinnerModule/setSpinnerState', false, { root: true })
  const success = resp && (resp.status === 200 || resp.status === 202)

  if (success) {
    const successToast = {
      basic: 'Success!',
      detail: 'Device backup successful.',
      hasErrors: false
    }
    contextWithState.dispatch('notificationModule/setToast', successToast, { root: true })
  } else {
    const failedToast = {
      basic: 'Failed:',
      detail: 'Device backup unsuccessful.',
      hasErrors: true
    }
    contextWithState.dispatch('notificationModule/setToast', failedToast, { root: true })
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
