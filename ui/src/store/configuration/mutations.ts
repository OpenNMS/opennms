const SAVE_TYPES = (state: any, types: any) => {
  state.types = types
}

const SAVE_SCHEDULE_PERIOD = (state: any, schedulePeriod: any) => {
  state.schedulePeriod = schedulePeriod
}

const SAVE_ADVANCE_DROPDOWN = (state: any, advancedDropdown: any) => {
  state.advancedDropdown = advancedDropdown
}

const SAVE_PROVISION_SERVICE = (state: any, provisionDService: any) => {
  state.provisionDService = provisionDService
}

const SEND_MODIFIED_DATA = (state: any, sendModifiedData: any) => {
  state.sendModifiedData = sendModifiedData
}

export default { SAVE_TYPES, SAVE_SCHEDULE_PERIOD, SAVE_ADVANCE_DROPDOWN, SAVE_PROVISION_SERVICE, SEND_MODIFIED_DATA }
