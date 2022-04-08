import { Node, Alarm } from '@/types'
import { latLng } from 'leaflet'
import { State } from './state'
import { numericSeverityLevel } from '@/components/Map/utils'
import { orderBy } from 'lodash'

const getNodes = (state: State): Node[] => {
  const severityMap = getNodeAlarmSeverityMap(state)
  const selectedNumericSeverityLevel = numericSeverityLevel(state.selectedSeverity)

  // copy the vuex nodes
  let nodes: Node[] = JSON.parse(JSON.stringify(state.nodesWithCoordinates))

  // sort the nodes
  nodes = orderBy(nodes, state.nodeSortObject.property, state.nodeSortObject.value)

  // filter for nodes within map view-port
  nodes = nodes.filter((node) => {
    const lat = Number(node.assetRecord.latitude)
    const lng = Number(node.assetRecord.longitude)
    const nodeLatLng = latLng(lat, lng)

    if (state.mapBounds) {
      return state.mapBounds.contains(nodeLatLng)
    }

    return false
  })

  // filter for nodes that meet selected severity
  if (state.selectedSeverity !== 'NORMAL') {
    nodes = nodes.filter((node) => {
      const nodeNumericSeverityLevel = numericSeverityLevel(severityMap[node.label])
      return state.selectedSeverity === 'NORMAL' || nodeNumericSeverityLevel >= selectedNumericSeverityLevel
    })
  }

  // filter for nodes that have been searched for
  if (state.searchedNodeLabels.length) {
    nodes = nodes.filter((node) => state.searchedNodeLabels.includes(node.label))
  }

  return nodes
}

const getNodeLabels = (state: State) => getNodes(state).map((node: Node) => node.label)

const getAlarms = (state: State): Alarm[] => {
  const nodeLabels = getNodeLabels(state)
  const alarms = state.alarms.filter((alarm: Alarm) => nodeLabels.includes(alarm.nodeLabel))

  // sort and return the alarms
  return orderBy(alarms, state.alarmSortObject.property, state.alarmSortObject.value)
}

const getNodeAlarmSeverityMap = (state: State) => {
  const map: { [x: string]: string } = {}

  state.alarms.forEach((alarm: Alarm) => {
    if (numericSeverityLevel(alarm.severity) > numericSeverityLevel(map[alarm.nodeLabel])) {
      map[alarm.nodeLabel] = alarm.severity.toUpperCase()
    }
  })

  return map
}

export default {
  getNodes,
  getAlarms,
  getNodeAlarmSeverityMap
}
