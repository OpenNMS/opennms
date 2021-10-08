import { MonitoringLocation } from '@/types'

export interface State {
  locations: MonitoringLocation[]
}

const state: State = {
  locations: []
}

export default state
