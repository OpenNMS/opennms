import { State } from './state'

const getFilteredLogs = (state: State) =>
  state.logs.filter((logName) => !state.searchValue || (state.searchValue && logName.includes(state.searchValue)))

export default {
  getFilteredLogs
}
