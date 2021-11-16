import { Summary } from '@/types'
import { State } from './state'

const SAVE_SUMMARY_TO_STATE = (state: State, summary: Summary) => {
  state.summary = summary
}

export default {
  SAVE_SUMMARY_TO_STATE
}
