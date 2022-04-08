import { VuexContext } from '@/types'

const setSpinnerState = (context: VuexContext, bool: boolean) => {
  context.commit('SET_SPINNER_STATE', bool)
}

export default {
  setSpinnerState
}
