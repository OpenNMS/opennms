import { Node, Alarm } from '@/types'
import { latLng } from 'leaflet'
import { State } from './state'

const getAlarmsFromSelectedNodes = (state: State) => {
  const selectedNodesLabel = state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id)).map(
    (node: Node) => node.label
  )
  return state.alarms.filter((alarm: Alarm) =>
    selectedNodesLabel.includes(alarm.nodeLabel)
  )
}

const getInterestedNodes = (state: State) => {
  const nodes = state.nodesWithCoordinates.filter((node: Node) => {
    const lat = Number(node.assetRecord.latitude)
    const lng = Number (node.assetRecord.longitude)
    const nodeLatLng = latLng(lat, lng)
    return state.mapBounds.contains(nodeLatLng)
  })

  return nodes.filter((node: Node) => state.interestedNodesID.includes(node.id))
}

export default {
  getInterestedNodes,
  getAlarmsFromSelectedNodes
}
