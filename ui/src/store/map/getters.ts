import { Node, Alarm } from '@/types'
import { latLng } from 'leaflet'
import { State } from './state'

const getNodes = (state: State): Node[] => {
  return state.nodesWithCoordinates.filter((node: Node) => {
    const lat = Number(node.assetRecord.latitude)
    const lng = Number (node.assetRecord.longitude)
    const nodeLatLng = latLng(lat, lng)
    return state.mapBounds.contains(nodeLatLng)
  })
}

const getNodeLabels = (state: State) => getNodes(state).map((node: Node) => node.label)

const getAlarms = (state: State): Alarm[] => {
  const nodeLabels = getNodeLabels(state)
  return state.alarms.filter((alarm: Alarm) => nodeLabels.includes(alarm.nodeLabel))
}

export default {
  getNodes,
  getAlarms
}
