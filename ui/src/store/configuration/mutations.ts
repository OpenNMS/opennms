const SAVE_PROVISION_SERVICE = (state: any, provisionDService: any) => {
  state.provisionDService = provisionDService
}
const UPDATE_TOAST_VALUE = (state: any, toastValue: any) => {
  state.toast = toastValue
}

export default { SAVE_PROVISION_SERVICE, UPDATE_TOAST_VALUE }
