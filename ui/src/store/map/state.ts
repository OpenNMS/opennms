import { Node, Alarm, Coordinates, FeatherSortObject } from '@/types'
import { SORT } from '@featherds/table'
import { latLng, LatLngBounds } from 'leaflet'

export interface State {
  nodesWithCoordinates: Node[]
  alarms: Alarm[]
  interestedNodesID: string[]
  edges: [number, number][]
  mapCenter: Coordinates
  mapBounds: LatLngBounds
  selectedSeverity: string
  searchedNodeLabels: string[]
  nodeSortObject: FeatherSortObject
  alarmSortObject: FeatherSortObject
}

const getDefaultBounds = (): LatLngBounds => {
  const southWest = latLng(-68, -202)
  const northEast = latLng(84, 135)
  return new LatLngBounds(southWest, northEast)
}

const state: State = {
  nodesWithCoordinates: [],
  alarms: [],
  interestedNodesID: [],
  edges: [],
  mapCenter: { latitude: 37.776603506225115, longitude: -33.43824554266541 },
  mapBounds: getDefaultBounds(),
  selectedSeverity: 'NORMAL',
  searchedNodeLabels: [],
  nodeSortObject: { property: 'label', value: SORT.ASCENDING },
  alarmSortObject: { property: 'id', value: SORT.DESCENDING }
}

export default state
