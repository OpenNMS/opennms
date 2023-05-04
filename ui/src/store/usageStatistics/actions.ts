import { VuexContext } from '@/types'
import API from '@/services'
import useSnackbar from '@/composables/useSnackbar'

const { showSnackBar } = useSnackbar()

const getStatistics = async (context: VuexContext) => {
  const data = await API.getUsageStatistics()

  if (data) {
    context.commit('SAVE_STATISTICS', data)
  }
}

const getMetadata = async (context: VuexContext) => {
  const metadata = await API.getUsageStatisticsMetadata()

  if (metadata) {
    context.commit('SAVE_METADATA', metadata)
  }
}

const getStatus = async (context: VuexContext) => {
  const status = await API.getUsageStatisticsStatus()
  
  if (status) {
    context.commit('SAVE_STATUS', status)
  }
}

const enableSharing = async (context: VuexContext) => {
  const success = await updateSharing(true)

  if (success) {
    getStatus(context)
  }
}

const disableSharing = async (context: VuexContext) => {
  const success = await updateSharing(false)

  if (success) {
    getStatus(context)
  }
}

const updateSharing = async (enable: boolean) => {
  const resp = await API.setUsageStatisticsStatus(enable)

  const success = !!(resp && (resp.status === 200 || resp.status === 202))

  if (success) {
    showSnackBar({
      msg: `Statistics sharing ${enable ? 'enabled' : 'disabled'}.`
    })
  } else {
    showSnackBar({
      msg: `Error attempting to ${enable ? 'enable' : 'disable'} statistics sharing.`,
      error: true
    })
  }

  return success
}

export default {
  getStatistics,
  getMetadata,
  getStatus,
  enableSharing,
  disableSharing
}
