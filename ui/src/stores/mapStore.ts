///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { defineStore } from 'pinia'
import { SORT } from '@featherds/table'
import { latLng, LatLngBounds } from 'leaflet'
import { orderBy } from 'lodash'
import { numericSeverityLevel } from '@/components/Map/utils'
import API from '@/services'
import {
  Alarm,
  AlarmModificationQueryVariable,
  Coordinates,
  FeatherSortObject,
  Node,
  QueryParameters
} from '@/types'

export const useMapStore = defineStore('mapStore', () => {
  const nodesWithCoordinates = ref([] as Node[])
  const alarms = ref([] as Alarm[])
  const interestedNodesID = ref([] as string[])
  const edges = ref([] as [number, number][])
  const mapCenter = ref({ latitude: 37.776603506225115, longitude: -33.43824554266541 } as Coordinates)
  const mapBounds = ref<LatLngBounds | undefined>(undefined)
  const selectedSeverity = ref('NORMAL')
  const searchedNodeLabels = ref([] as string[])
  const nodeSortObject = ref({ property: 'label', value: SORT.ASCENDING } as FeatherSortObject)
  const alarmSortObject = ref({ property: 'id', value: SORT.DESCENDING } as FeatherSortObject)
  const nodeSearchTerm = ref('')

  const getNodeAlarmSeverityMap = () => {
    const map: { [x: string]: string } = {}

    alarms.value.forEach((alarm: Alarm) => {
      if (numericSeverityLevel(alarm.severity) > numericSeverityLevel(map[alarm.nodeLabel])) {
        map[alarm.nodeLabel] = alarm.severity.toUpperCase()
      }
    })

    return map
  }

  // Get current list of filtered and sorted nodes, does NOT make API call
  // previously getters: getNodes
  const getNodes = (): Node[] => {
    const severityMap = getNodeAlarmSeverityMap()
    const selectedNumericSeverityLevel = numericSeverityLevel(selectedSeverity.value)

    // copy the nodes from the store
    let nodes: Node[] = JSON.parse(JSON.stringify(nodesWithCoordinates.value))

    // sort the nodes
    nodes = orderBy(nodes, nodeSortObject.value.property, nodeSortObject.value.value)

    // filter for nodes within map view-port
    nodes = nodes.filter((node) => {
      const lat = Number(node.assetRecord.latitude)
      const lng = Number(node.assetRecord.longitude)
      const nodeLatLng = latLng(lat, lng)

      if (mapBounds.value) {
        return mapBounds.value.contains(nodeLatLng)
      }

      return false
    })

    // filter for nodes that meet selected severity
    if (selectedSeverity.value !== 'NORMAL') {
      nodes = nodes.filter((node) => {
        const nodeNumericSeverityLevel = numericSeverityLevel(severityMap[node.label])
        return selectedSeverity.value === 'NORMAL' || nodeNumericSeverityLevel >= selectedNumericSeverityLevel
      })
    }

    // filter for nodes that have been searched for
    if (searchedNodeLabels.value.length) {
      nodes = nodes.filter((node) => searchedNodeLabels.value.includes(node.label))
    }

    return nodes
  }

  const getNodeLabels = () => {
    return getNodes().map((node: Node) => node.label)
  }

  // Get current list of filtered and sorted alarms, does NOT make API call
  // previously getters: getAlarms
  const getAlarms = (): Alarm[] => {
    const nodeLabels = getNodeLabels()
    const filteredAlarms = alarms.value.filter((alarm: Alarm) => nodeLabels.includes(alarm.nodeLabel))

    // sort and return the alarms
    return orderBy(filteredAlarms, alarmSortObject.value.property, alarmSortObject.value.value)
  }

  // Make API call to get nodes
  // previously actions: getNodes
  const fetchNodes = async (queryParameters?: QueryParameters) => {
    const defaultParams = queryParameters || { limit: 5000, offset: 0 }
    const resp = await API.getNodes(defaultParams)

    if (resp) {
      const nodes = resp.node.filter(
        (node) =>
          !(node.assetRecord.latitude == null || node.assetRecord.latitude.length === 0) &&
          !(node.assetRecord.longitude == null || node.assetRecord.longitude.length === 0)
      )

      nodesWithCoordinates.value = [...nodes]
      interestedNodesID.value = nodes.map(node => node.id)
    }
  }

  // Make API call to get alarms
  // previously actions: getAlarms
  const fetchAlarms = async (queryParameters?: QueryParameters) => {
    const defaultParams = queryParameters || { limit: 5000, offset: 0 }
    const resp = await API.getAlarms(defaultParams)

    if (resp) {
      alarms.value = [...resp.alarm]
    }
  }

  const resetInterestedNodesID = () => {
    interestedNodesID.value = nodesWithCoordinates.value.map((node: Node) => node.id)
  }

  const getNodesGraphEdges = async (queryParameters?: QueryParameters) => {
    const resp = await API.getGraphNodesNodes(queryParameters)

    if (resp) {
      const edgeValues: [number, number][] = []

      resp.edges.forEach((e) => {
        const edge: [number,number] = [e.source.id, e.target.id]
        edgeValues.push(edge)
      })

      edges.value = edgeValues
    }
  }

  const setInterestedNodesId = (ids: number[]) => {
    interestedNodesID.value = ids.map(n => n.toString())
  }

  const setMapCenter = (center: Coordinates) => {
    mapCenter.value = center
  }

  const setMapBounds = (bounds: LatLngBounds) => {
    mapBounds.value = bounds
  }

  const modifyAlarm = async (alarmQueryVariable: AlarmModificationQueryVariable) => {
    const resp = await API.modifyAlarm(alarmQueryVariable.pathVariable, alarmQueryVariable.queryParameters)
    return resp
  }

  const setSelectedSeverity = (severity: string) => {
    selectedSeverity.value = severity
  }

  const setSearchedNodeLabels = (nodeLabels: string[]) => {
    searchedNodeLabels.value = nodeLabels
  }

  const setNodeSortObject = (sortObj: FeatherSortObject) => {
    nodeSortObject.value = sortObj
  }

  const setAlarmSortObject = (sortObj: FeatherSortObject) => {
    alarmSortObject.value = sortObj
  }

  const setNodeSearchTerm = (searchTerm: string) => {
    nodeSearchTerm.value = searchTerm
  }

  return {
    nodesWithCoordinates,
    alarms,
    interestedNodesID,
    edges,
    mapCenter,
    mapBounds,
    selectedSeverity,
    searchedNodeLabels,
    nodeSortObject,
    alarmSortObject,
    nodeSearchTerm,
    getNodes,
    getNodeAlarmSeverityMap,
    getNodeLabels,
    getAlarms,
    fetchNodes,
    fetchAlarms,
    resetInterestedNodesID,
    getNodesGraphEdges,
    setInterestedNodesId,
    setMapCenter,
    setMapBounds,
    modifyAlarm,
    setSelectedSeverity,
    setSearchedNodeLabels,
    setNodeSortObject,
    setAlarmSortObject,
    setNodeSearchTerm
  }
})
