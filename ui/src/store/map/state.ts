import { Node, Alarm, Coordinates, FeatherSortObject } from '@/types'
import { SORT } from '@featherds/table'
import { LatLngBounds } from 'leaflet'

export interface State {
  nodesWithCoordinates: Node[]
  alarms: Alarm[]
  interestedNodesID: string[]
  mapCenter: Coordinates
  mapBounds: LatLngBounds | undefined
  selectedSeverity: string
  searchedNodeLabels: string[]
  nodeSortObject: FeatherSortObject
  alarmSortObject: FeatherSortObject
}

const state: State = {
  nodesWithCoordinates: [],
  alarms: [],
  interestedNodesID: [],
  mapCenter: { latitude: 37.776603506225115, longitude: -33.43824554266541 },
  mapBounds: undefined,
  selectedSeverity: 'NORMAL',
  searchedNodeLabels: [],
  nodeSortObject: { property: 'label', value: SORT.ASCENDING },
  alarmSortObject: { property: 'id', value: SORT.DESCENDING }
}

export default state
