import { PowerGrid } from '@/components/Topology/topology.constants'
import { NodePoint, TopologyGraphList } from '@/types/topology'
import { Layouts } from 'v-network-graph'
import { State } from './state'

const getCircleLayout = (state: State): Record<string, NodePoint> => {
  const centerY = 350
  const centerX = 350
  const radius = 250

  const vertexNames = Object.keys(state.verticies)
  const layout = {} as Record<string, NodePoint>

  for (let i = 0;i < vertexNames.length;i++) {
    layout[vertexNames[i]] = {
      x: centerX + radius * Math.cos((2 * Math.PI * i) / vertexNames.length),
      y: centerY + radius * Math.sin((2 * Math.PI * i) / vertexNames.length)
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
 * @param state topology store 
 * @returns TopologyGraphList
 */
const getPowerGridGraphs = (state: State): TopologyGraphList => {
  if (hasPowerGridGraphs(state)) {
    return state.topologyGraphs.filter((graphs) => graphs.label === PowerGrid)[0]
  }
  return { graphs: [], id: 'N/A', label: 'N/A' }
}

export default {
  getLayout,
  hasPowerGridGraphs,
  getPowerGridGraphs
}
