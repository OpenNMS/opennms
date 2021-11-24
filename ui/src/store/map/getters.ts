import { Node, Alarm } from '@/types'
import { State } from './state'

const getInterestedNodes = (state: State) => {
  return state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id))
}

const getAlarmsFromSelectedNodes = (state: State) => {
  let selectedNodesLabel = state.nodesWithCoordinates.filter((node: Node) => state.interestedNodesID.includes(node.id)).map(
    (node: Node) => node.label
  )
  return state.alarms.filter((alarm: Alarm) =>
    selectedNodesLabel.includes(alarm.nodeLabel)
  )
}

export default {
  getInterestedNodes,
  getAlarmsFromSelectedNodes
}
