import API from '@/services'
import { VuexContext } from '@/types'

const getWhoAmI = async (context: VuexContext) => {
  const whoAmI = await API.getWhoAmI()

  if (whoAmI) {
    context.commit('SAVE_WHO_AM_I_TO_STATE', whoAmI)
  }
}

export default {
  getWhoAmI
}
