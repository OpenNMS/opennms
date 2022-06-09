import { PowerGrid } from '@/components/Topology/topology.constants'
import { NodePoint, TopologyGraphList } from '@/types/topology'
import { orderBy } from 'lodash'
import { Layouts } from 'v-network-graph'
import { State } from './state'

const getCircleLayout = (state: State): Record<string, NodePoint> => {
  const centerY = 350
  const centerX = 350
  const radius = 250

  const vertexNames = Object.keys(state.vertices)
  const layout = {} as Record<string, NodePoint>

  for (let i = 0; i < vertexNames.length; i++) {
    layout[vertexNames[i]] = {
      x: Number((centerX + radius * Math.cos((2 * Math.PI * i) / vertexNames.length)).toFixed(0)),
      y: Number((centerY + radius * Math.sin((2 * Math.PI * i) / vertexNames.length)).toFixed(0))
    }
  }

  return layout
}

const getLayout = (state: State): Layouts => {
  if (state.selectedView === 'circle') {
    return {
      nodes: getCircleLayout(state)
    }
  }

  return {} as Layouts
}

/**
 * Determine whether powergrid graphs are available
 *
 * @param state topology store
 * @returns boolean
 */
const hasPowerGridGraphs = (state: State): boolean => {
  for (const graphs of state.topologyGraphs) {
    if (graphs.label === PowerGrid) {
      return true
    }
  }
  return false
}

/**
 * Return powergrid graphs, if available.
 * Otherwise return object with empty graphs array.
 * 
 * API does not return proper layer order,
 * but the id is made up of proper ordered layer names.
 * This can be used during layer selection / context menu nav.
 *
 * @param state topology store
 * @returns TopologyGraphList
 */
const getPowerGridGraphs = (state: State): TopologyGraphList => {
  if (hasPowerGridGraphs(state)) {
    const powerGridGraphs = state.topologyGraphs.filter((graphs) => graphs.label === PowerGrid)[0]
    const orderedLayers = powerGridGraphs.id.split('.')

    for (const graph of powerGridGraphs.graphs) {
      graph.index = orderedLayers.indexOf(graph.label.toLowerCase())
    }

    powerGridGraphs.graphs = orderBy(powerGridGraphs.graphs, 'index', 'asc')

    return powerGridGraphs
  }
  return { graphs: [], id: 'N/A', label: 'N/A' }
}

export default {
  getLayout,
  hasPowerGridGraphs,
  getPowerGridGraphs
}
