import { VuexContext } from '@/types'
import API from '@/services'

const getProvisionDService = async (context: VuexContext) => {
  const resp = await API.getProvisionDService()
  if (resp) {
    context.commit('SAVE_PROVISION_SERVICE', resp)
  }
}

const updateToastValue = async (context: VuexContext, toastVal: string) => {
  context.commit('UPDATE_TOAST_VALUE', toastVal)
}

export default {
  getProvisionDService,
  updateToastValue
}
