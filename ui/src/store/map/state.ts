import { Node, Alarm, Coordinates, FeatherSortObject } from '@/types'
import { SORT } from '@featherds/table'
import { LatLngBounds } from 'leaflet'

export interface State {
  nodesWithCoordinates: Node[]
  alarms: Alarm[]
  interestedNodesID: string[]
  edges: [number, number][]
  mapCenter: Coordinates
  mapBounds: LatLngBounds | undefined
  selectedSeverity: string
  searchedNodeLabels: string[]
  nodeSortObject: FeatherSortObject
  alarmSortObject: FeatherSortObject
  nodeSearchTerm: string
}

const state: State = {
  nodesWithCoordinates: [],
  alarms: [],
  interestedNodesID: [],
  edges: [],
  mapCenter: { latitude: 37.776603506225115, longitude: -33.43824554266541 },
  mapBounds: undefined,
  selectedSeverity: 'NORMAL',
  searchedNodeLabels: [],
  nodeSortObject: { property: 'label', value: SORT.ASCENDING },
  alarmSortObject: { property: 'id', value: SORT.DESCENDING },
  nodeSearchTerm: ''
}

export default state
