const getDropdownTypes = (state: any, types: any) => {
  state.types = types
}

const getSchedulePeriod = (state: any, schedulePeriod: any) => {
  state.schedulePeriod = schedulePeriod
}

const getAdvancedDropdown = (state: any, advancedDropdown: any) => {
  state.advancedDropdown = advancedDropdown
}

export default { getDropdownTypes, getSchedulePeriod, getAdvancedDropdown }
