import { State } from './state'

const SET_OPEN_API = (state: State, openApi: Record<string, unknown>) => {
  state.openApi = openApi
}

export default {
  SET_OPEN_API
}
