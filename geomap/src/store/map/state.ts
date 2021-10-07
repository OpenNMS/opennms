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
  mapCenter: { latitude: 35.849613, longitude: -78.794882 }, //OpenNMS Head Quarter
}

export default state
