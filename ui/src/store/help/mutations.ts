import { State } from './state'

const SET_OPEN_API = (state: State, openApi: any) => {
  state.openApi = openApi
}

export default {
  SET_OPEN_API
}
