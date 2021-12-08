import { Node, Alarm, Coordinates, FeatherSortObject } from '@/types'
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

const SAVE_SELECTED_SEVERITY = (state: State, selectedSeverity: string) => {
  state.selectedSeverity = selectedSeverity
}

const SAVE_SEARCHED_NODE_LABELS = (state: State, searchedNodeLabels: string[]) => {
  state.searchedNodeLabels = searchedNodeLabels
}

const SAVE_NODE_SORT_OBJECT = (state: State, nodeSortObject: FeatherSortObject) => {
  state.nodeSortObject = nodeSortObject
}
const SAVE_ALARM_SORT_OBJECT = (state: State, alarmSortObject: FeatherSortObject) => {
  state.alarmSortObject = alarmSortObject
}

export default {
  SAVE_NODES_TO_STATE,
  SAVE_ALARMS_TO_STATE,
  SAVE_INTERESTED_NODES_ID,
  SAVE_NODE_EDGES,
  SAVE_MAP_CENTER,
  SAVE_MAP_BOUNDS,
  SAVE_SELECTED_SEVERITY,
  SAVE_SEARCHED_NODE_LABELS,
  SAVE_NODE_SORT_OBJECT,
  SAVE_ALARM_SORT_OBJECT
}
