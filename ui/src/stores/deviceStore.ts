import { defineStore } from 'pinia'
import API from '@/services'
import useDownload from '@/composables/useDownload'
import useSnackbar from '@/composables/useSnackbar'
import { DeviceConfigBackup, DeviceConfigQueryParams, status } from '@/types/deviceConfig'

const { downloadFile } = useDownload()
const { showSnackBar } = useSnackbar()

export const useDeviceStore = defineStore('deviceStore', () => {
  const deviceConfigBackups = ref([] as DeviceConfigBackup[])
  const deviceConfigBackupQueryParams = ref({ offset: 0, limit: 20 } as DeviceConfigQueryParams)
  const modalDeviceConfigBackup = ref({} as DeviceConfigBackup)
  const selectedIds = ref([] as number[])
  const vendorOptions = ref([] as string[])
  const backupStatusOptions = ref(['SUCCESS', 'FAILED', 'NONE'] as status[])
  const osImageOptions = ref([] as string[])
  const deviceConfigTotal = ref('N/A')
  const historyModalBackups = ref([] as DeviceConfigBackup[])

  const getDeviceConfigBackupObjByIds = (deviceConfigs: DeviceConfigBackup[], ids: number[]) => {
    return deviceConfigs.filter((dcb) => ids.includes(dcb.id))
  }

  const getDeviceConfigBackups = async (pageEnter?: boolean) => {
    const queryParams : DeviceConfigQueryParams =
      (pageEnter && { ...deviceConfigBackupQueryParams.value, pageEnter: true }) ||
        deviceConfigBackupQueryParams.value

    const resp = await API.getDeviceConfigBackups(queryParams)

    if (resp) {
      deviceConfigBackups.value = resp.data || []
      deviceConfigTotal.value = resp.headers['content-range']
    }
  }

  const getHistoryByIpInterface = async () => {
    const modalDeviceConfigIpInterface = modalDeviceConfigBackup.value.ipInterfaceId
    const modalDeviceConfigConfigType = modalDeviceConfigBackup.value.configType

    const resp = await API.getHistoryByIpInterface(modalDeviceConfigIpInterface, modalDeviceConfigConfigType)
    historyModalBackups.value = resp
  }

  const getAndMergeDeviceConfigBackups = async () => {
    const backups = await API.getDeviceConfigBackups(deviceConfigBackupQueryParams.value)

    if (backups && backups.data) {
      deviceConfigBackups.value = backups.data
    }
  }

  const downloadByConfig = async (config: DeviceConfigBackup | DeviceConfigBackup[]) => {
    const isSingleDeviceBackup = (config: DeviceConfigBackup | DeviceConfigBackup[]): config is DeviceConfigBackup => {
      return (config as DeviceConfigBackup).id !== undefined
    }

    if (isSingleDeviceBackup(config)) {
      const file = await API.downloadDeviceConfigs([config.id])
      if (file) {
        downloadFile(file)
      }
    } else {
      const ids = config.map((x) => x.id)
      const file = await API.downloadDeviceConfigs(ids)
      if (file) {
        downloadFile(file)
      }
    }
  }

  const downloadSelectedDevices = async () => {
    const ids = selectedIds.value
    const file = await API.downloadDeviceConfigs(ids)
    if (file) {
      downloadFile(file)
    }
  }

  const backupSelectedDevices = async () => {
    const ids = selectedIds.value
    const configs = deviceConfigBackups.value

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

  const updateDeviceConfigBackupQueryParams = async (newQueryParams: DeviceConfigQueryParams) => {
    deviceConfigBackupQueryParams.value = { ...deviceConfigBackupQueryParams.value, ...newQueryParams }
  }

  const setModalDeviceConfigBackup = async (config: DeviceConfigBackup) => {
    modalDeviceConfigBackup.value = config
  }

  // TODO: Refactor to have 2 params: number[] and isAll: boolean
  const setSelectedIds = (idsOrAll: number[] | 'all') => {
    const configs = deviceConfigBackups.value

    if (idsOrAll === 'all') {
      const selIds = configs.map((dcb) => dcb.id)
      selectedIds.value = selIds

      if (configs.length === 1) {
        modalDeviceConfigBackup.value = configs[0]
      }
    } else {
      selectedIds.value = idsOrAll as number[]

      if (idsOrAll.length === 1) {
        modalDeviceConfigBackup.value = getDeviceConfigBackupObjByIds(configs, idsOrAll)[0]
      }
    }
  }

  const getVendorOptions = async () => {
    const resp = await API.getVendorOptions()
    vendorOptions.value = resp
  }

  const getOsImageOptions = async () => {
    const resp = await API.getOsImageOptions()
    osImageOptions.value = resp
  }

  return {
    deviceConfigBackups,
    deviceConfigBackupQueryParams,
    modalDeviceConfigBackup,
    selectedIds,
    vendorOptions,
    backupStatusOptions,
    osImageOptions ,
    deviceConfigTotal,
    historyModalBackups,
    getDeviceConfigBackupObjByIds,
    getDeviceConfigBackups,
    getHistoryByIpInterface,
    getAndMergeDeviceConfigBackups,
    downloadByConfig,
    downloadSelectedDevices,
    backupSelectedDevices,
    updateDeviceConfigBackupQueryParams,
    setModalDeviceConfigBackup,
    setSelectedIds,
    getVendorOptions,
    getOsImageOptions
  }
})
