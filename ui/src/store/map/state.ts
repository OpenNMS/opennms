import { Node, Alarm, Coordinates } from '@/types'
import { latLng, LatLngBounds } from 'leaflet'

export interface State {
  nodesWithCoordinates: Node[]
  alarms: Alarm[]
  interestedNodesID: string[]
  edges: [number, number][]
  mapCenter: Coordinates
  mapBounds: LatLngBounds
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
  mapBounds: getDefaultBounds()
}

export default state
