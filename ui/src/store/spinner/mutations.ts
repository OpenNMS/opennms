import { State } from './state'

const SET_SPINNER_STATE = (state: State, bool: boolean) => {
  state.spinnerState = bool
}

export default {
  SET_SPINNER_STATE
}
