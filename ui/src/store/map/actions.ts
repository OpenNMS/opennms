import API from '@/services'
import {
  QueryParameters,
  VuexContext,
  Coordinates,
  AlarmModificationQueryVariable,
  Node,
  FeatherSortObject
} from '@/types'
import { LatLngBounds } from 'leaflet'

const getNodes = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const defaultParams = queryParameters || { limit: 5000, offset: 0 }
  const resp = await API.getNodes(defaultParams)
  if (resp) {
    const nodes = resp.node.filter(
      (node) =>
        !(node.assetRecord.latitude == null || node.assetRecord.latitude.length === 0) &&
        !(node.assetRecord.longitude == null || node.assetRecord.longitude.length === 0)
    )
    context.commit('SAVE_NODES_TO_STATE', nodes)
    context.commit(
      'SAVE_INTERESTED_NODES_ID',
      nodes.map((node) => node.id)
    )
  }
}

const getAlarms = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const defaultParams = queryParameters || { limit: 5000, offset: 0 }
  const resp = await API.getAlarms(defaultParams)
  if (resp) {
    context.commit('SAVE_ALARMS_TO_STATE', resp.alarm)
  }
}

const resetInterestedNodesID = ({ commit, state }: any) => {
  commit(
    'SAVE_INTERESTED_NODES_ID',
    state.nodesWithCoordinates.map((node: Node) => node.id)
  )
}

const getNodesGraphEdges = async (context: VuexContext, queryParameters?: QueryParameters) => {
  const resp = await API.getGraphNodesNodes(queryParameters)
  if (resp) {
    const edges: number[][] = []
    resp.edges.forEach((e) => {
      let edge: number[] = []
      edge = [e.source.id, e.target.id]
      edges.push(edge)
    })

    context.commit('SAVE_NODE_EDGES', edges)
  }
}

const setInterestedNodesId = (context: VuexContext, ids: number[]) => {
  context.commit('SAVE_INTERESTED_NODES_ID', ids)
}

const setMapCenter = (context: VuexContext, center: Coordinates) => {
  context.commit('SAVE_MAP_CENTER', center)
}

const setMapBounds = (context: VuexContext, bounds: LatLngBounds) => {
  context.commit('SAVE_MAP_BOUNDS', bounds)
}

const modifyAlarm = async (context: VuexContext, alarmQueryVariable: AlarmModificationQueryVariable) => {
  const resp = await API.modifyAlarm(alarmQueryVariable.pathVariable, alarmQueryVariable.queryParameters)
  return resp
}

const setSelectedSeverity = (context: VuexContext, selectedSeverity: string) => {
  context.commit('SAVE_SELECTED_SEVERITY', selectedSeverity)
}

const setSearchedNodeLabels = (context: VuexContext, nodeLabels: string[]) => {
  context.commit('SAVE_SEARCHED_NODE_LABELS', nodeLabels)
}

const setNodeSortObject = (context: VuexContext, sortObj: FeatherSortObject) => {
  context.commit('SAVE_NODE_SORT_OBJECT', sortObj)
}

const setAlarmSortObject = (context: VuexContext, sortObj: FeatherSortObject) => {
  context.commit('SAVE_ALARM_SORT_OBJECT', sortObj)
}

export default {
  getNodes,
  getAlarms,
  resetInterestedNodesID,
  getNodesGraphEdges,
  setInterestedNodesId,
  setMapCenter,
  modifyAlarm,
  setMapBounds,
  setSelectedSeverity,
  setSearchedNodeLabels,
  setNodeSortObject,
  setAlarmSortObject
}
