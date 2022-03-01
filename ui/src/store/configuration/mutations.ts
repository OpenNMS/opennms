import { State } from './state'

const SAVE_PROVISION_SERVICE = (state: State, provisionDService: any) => {
  state.provisionDService = provisionDService
}
const UPDATE_TOAST_VALUE = (state: State, toastValue: string) => {
  state.toast = toastValue
}

export default { SAVE_PROVISION_SERVICE, UPDATE_TOAST_VALUE }
