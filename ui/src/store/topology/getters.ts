import { PowerGrid, DisplayType } from '@/components/Topology/topology.constants'
import { graphsPowergrid } from '@/components/Topology/topology.helpers'
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
const hasPowergridGraphs = (state: State): boolean => {
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
  if (hasPowergridGraphs(state)) {
    const powergridGraphs = state.topologyGraphs.filter(({id = ''}) => DisplayType[id] === DisplayType.powergrid)[0]

    if(powergridGraphs.graphs?.length) {
      const orderedLayers = powergridGraphs.id?.split('.') || []

      for (const graph of powergridGraphs.graphs) {
        graph.index = orderedLayers.indexOf(graph.label.toLowerCase())
      }
  
      powergridGraphs.graphs = orderBy(powergridGraphs.graphs, 'index', 'asc')
  
      return powergridGraphs
    }
    
  }
  
  return { graphs: [], id: 'N/A', label: 'N/A', type: 'N/A' }
}

const getGraphsDisplay = (state: State): TopologyGraphList => {
  let graph: TopologyGraphList = { graphs: [], id: 'N/A', label: 'N/A', type: 'N/A' }
  const graphsDisplay: TopologyGraphList = getGraphs(state).filter(({type}) => type === state.selectedDisplay)[0]
  
  switch(state.selectedDisplay){
    case DisplayType.powergrid:
      graph = {
        ...graph,
        ...graphsPowergrid(graphsDisplay)
      }
      break
    case DisplayType.nodes:
      break
    default:
  }

  return graph
}

const getGraphs = (state: State): TopologyGraphList[] => {
  const topologyGraphs = state.topologyGraphs.map(({graphs = [], id = '', label}) => {
    const gs = graphs.map((g, index) => ({
      ...g,
      index 
    }))

    return {
      graphs: gs,
      id,
      label,
      type: DisplayType[id]
    }
  })

  return topologyGraphs
}

export default {
  getLayout,
  getGraphs,
  getGraphsDisplay,
  hasPowergridGraphs,
  getPowerGridGraphs
}
