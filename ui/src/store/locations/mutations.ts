import { MonitoringLocation } from '@/types'
import { State } from './state'

const SAVE_LOCATIONS_TO_STATE = (state: State, locations: MonitoringLocation[]) => {
  state.locations = locations
}

export default {
  SAVE_LOCATIONS_TO_STATE,
}
