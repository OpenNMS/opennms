import { State } from './state'

const SET_OPEN_API = (state: State, openApi: Record<string, unknown>) => {
  state.openApi = openApi
}

const SET_OPEN_API_V1 = (state: State, openApi: Record<string, unknown>) => {
  state.openApiV1 = openApi
}

export default {
  SET_OPEN_API,
  SET_OPEN_API_V1
}
