import { State } from './state'

const SAVE_LOGS_TO_STATE = (state: State, logs: string[]) => {
  state.logs = logs
}

const SAVE_LOG_TO_STATE = (state: State, log: string) => {
  state.log = log
}

const SAVE_SEARCH_VALUE_TO_STATE = (state: State, searchValue: string) => {
  state.searchValue = searchValue
}

const SAVE_SELECTED_FILE_NAME_TO_STATE = (state: State, selectedLog: string) => {
  state.selectedLog = selectedLog
}

const SAVE_REVERSE_LOG_TO_STATE = (state: State, reverseLog: boolean) => {
  state.reverseLog = reverseLog
}

export default {
  SAVE_SEARCH_VALUE_TO_STATE,
  SAVE_SELECTED_FILE_NAME_TO_STATE,
  SAVE_LOGS_TO_STATE,
  SAVE_LOG_TO_STATE,
  SAVE_REVERSE_LOG_TO_STATE
}
