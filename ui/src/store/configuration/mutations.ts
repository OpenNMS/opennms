const getDropdownTypes = (state: any, types: any) => {
  state.types = types
}

const getSchedulePeriod = (state: any, schedulePeriod: any) => {
  state.schedulePeriod = schedulePeriod
}

const getAdvancedDropdown = (state: any, advancedDropdown: any) => {
  state.advancedDropdown = advancedDropdown
}

const getProvisionDService = (state: any, provisionDService: any) => {
  state.provisionDService = provisionDService
}

const sendEditData = (state: any, sendEditData: any) => {
  state.sendEditData = sendEditData
}

export default { getDropdownTypes, getSchedulePeriod, getAdvancedDropdown, getProvisionDService, sendEditData }
