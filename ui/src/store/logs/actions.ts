import API from '@/services'
import { VuexContext } from '@/types'
import { State } from './state'

interface ContextWithState extends VuexContext {
  state: State
}

const getLogs = async (context: VuexContext) => {
  const logs = await API.getLogs()
  context.commit('SAVE_LOGS_TO_STATE', logs)
}

const setReverseLog = async (context: VuexContext, reverseLog: boolean) => {
  context.commit('SAVE_REVERSE_LOG_TO_STATE', reverseLog)
}

const getLog = async (context: ContextWithState, name: string) => {
  const log = await API.getLog(name, context.state.reverseLog)

  context.commit('SAVE_LOG_TO_STATE', log)
  context.commit('SAVE_SELECTED_FILE_NAME_TO_STATE', name)
}

const setSearchValue = async (context: VuexContext, searchValue: string) => {
  context.commit('SAVE_SEARCH_VALUE_TO_STATE', searchValue)
}

export default {
  getLog,
  getLogs,
  setSearchValue,
  setReverseLog
}
