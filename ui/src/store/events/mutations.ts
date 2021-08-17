import { State } from './state'

const SAVE_EVENTS_TO_STATE = (state: State, events: any) => {
  state.events = events
}

const SAVE_TOTAL_COUNT = (state: State, totalCount: number) => {
  state.totalCount = totalCount
}

export default {
  SAVE_EVENTS_TO_STATE,
  SAVE_TOTAL_COUNT
}
