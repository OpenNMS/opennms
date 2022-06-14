
import { orderBy } from 'lodash'
import { TopologyGraphList, TopologyGraph } from '@/types/topology'
import { DisplayType } from './topology.constants'

/**
 * Add display type (for quick reference) and index (for ordering) properties to the list
 * @param graphList list from API response
 * @returns list of TopologyGraphList
 */
export const formatTopologyGraphs = (graphList: TopologyGraphList[]): TopologyGraphList[] => {
  if(!graphList.length) return []
  
  const topologyGraphs = graphList.map(({graphs = [], id = '', label}) => {
    const graphsWithIndex = graphs.map((g, index) => ({
      ...g,
      index 
    }))

    return {
      graphs: graphsWithIndex,
      id,
      label,
      type: DisplayType[id]
    }
  })

  return topologyGraphs
}

/**
 * Order the graphs list by the display graph id
 * Note: this is probably no longer relevant since API response seems already being ordered
 * @param powergridGraphs 
 * @param graphId 
 * @returns ordered list
 */
export const formatPowergridGraph = (powergridGraphs: TopologyGraph[], graphId =''): object => {
  let orderedGraphs = [ ...powergridGraphs ]
  const orderedLayers = graphId?.split('.')

  for (const graph of orderedGraphs) {
    graph.index = orderedLayers.indexOf(graph.label.toLowerCase())
  }

  orderedGraphs = orderBy(orderedGraphs, 'index', 'asc')

  return {
    graphs: [...orderedGraphs]
  }
}
