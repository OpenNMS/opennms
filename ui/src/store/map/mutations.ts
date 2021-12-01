import { Node, Alarm, Coordinates } from '@/types'
import { LatLngBounds } from 'leaflet'
import { State } from './state'

const SAVE_NODES_TO_STATE = (state: State, nodes: Node[]) => {
  state.nodesWithCoordinates = [...nodes]
}

const SAVE_ALARMS_TO_STATE = (state: State, alarms: Alarm[]) => {
  state.alarms = [...alarms]
}

const SAVE_INTERESTED_NODES_ID = (state: State, ids: string[]) => {
  state.interestedNodesID = [...ids]
}

const SAVE_NODE_EDGES = (state: State, edges: [number, number][]) => {
  state.edges = [...edges]
}

const SAVE_MAP_CENTER = (state: State, center: Coordinates) => {
  state.mapCenter = center
}

const SAVE_MAP_BOUNDS = (state: State, bounds: LatLngBounds) => {
  state.mapBounds = bounds
}

export default {
  SAVE_NODES_TO_STATE,
  SAVE_ALARMS_TO_STATE,
  SAVE_INTERESTED_NODES_ID,
  SAVE_NODE_EDGES,
  SAVE_MAP_CENTER,
  SAVE_MAP_BOUNDS
}
