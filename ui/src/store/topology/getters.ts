import { ViewType, DisplayType } from '@/components/Topology/topology.constants'
import { formatTopologyGraphs, formatPowergridGraph } from '@/components/Topology/topology.helpers'
import { NodePoint, TopologyGraphList } from '@/types/topology'
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
  if (state.selectedView === ViewType.circle) {
    return {
      nodes: getCircleLayout(state)
    }
  }

  return {} as Layouts
}

/**
 * Return topology display graphs, if available.
 * Otherwise return object with empty graphs array.
 * 
 * API does not return proper layer order,
 * but the id is made up of proper ordered layer names.
 * This can be used during layer selection / context menu nav.
 * 
 * @param state topology store
 * @returns TopologyGraphList object with its sub layers list, if any
 */
const getGraphsDisplay = (state: State): TopologyGraphList => {
  let graph: TopologyGraphList = { graphs: [], id: 'N/A', label: 'N/A', type: 'N/A' }

  const topologyGraph: TopologyGraphList = formatTopologyGraphs(state.topologyGraphs).filter(({type}) => type === state.selectedDisplay)[0] || {}
  
  if(!topologyGraph.graphs?.length) {
    graph = { ...topologyGraph }
  } else {
    switch(topologyGraph.type){
      case DisplayType.powergrid:
        graph = {
          ...topologyGraph,
          ...formatPowergridGraph(topologyGraph.graphs, topologyGraph.id) // not certain if ordering still relevant; layer order from API response seems in good order
        }
        break
      case DisplayType.nodes:
        break
      default:
    }
    
  }
        
  return graph
}

const getGraphs = (state: State): TopologyGraphList[] => formatTopologyGraphs(state.topologyGraphs)

export default {
  getLayout,
  getGraphs,
  getGraphsDisplay
}
