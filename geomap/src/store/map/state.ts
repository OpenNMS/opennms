import { Node, Alarm, Coordinates } from '@/types'

export interface State {
  nodesWithCoordinates: Node[]
  alarms: Alarm[]
  interestedNodesID: string[]
  edges: [number, number][]
  mapCenter: Coordinates
}

const state: State = {
  nodesWithCoordinates: [],
  alarms: [],
  interestedNodesID: [],
  edges: [],
  mapCenter: { latitude: 37.776603506225115, longitude: -33.43824554266541 }, 
}

export default state
