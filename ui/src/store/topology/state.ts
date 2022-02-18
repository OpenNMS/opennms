import { NetworkGraphEdge, NetworkGraphVertex } from '@/types/topology'

export interface State {
  isTopologyView: boolean
  selectedView: string
  edges: Record<string, NetworkGraphEdge>
  verticies: Record<string, NetworkGraphVertex>
  semanticZoomLevel: number
}

const state: State = {
  isTopologyView: false,
  selectedView: 'map',
  edges: {},
  verticies: {},
  semanticZoomLevel: 1
}

export default state
