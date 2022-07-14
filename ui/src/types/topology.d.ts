export interface VerticesAndEdges {
  ['focus-strategy']?: string
  defaultFocus?: DefaultFocus
  focus?: Focus
  vertices: ResponseVertex[]
  edges: ResponseEdge[]
}

interface DefaultFocus {
  type: string
  vertexIds: { namespace: string; id: string }[]
}

interface Focus {
  semanticZoomLevel: number
  vertices: string[]
}

// From response
export interface ResponseEdge {
  id: string
  label: string
  namespace: string
  source: {
    namespace: string
    id: string
  }
  target: {
    namespace: string
    id: string
  }
}

export interface ResponseVertex {
  iconKey: string
  id: string
  label: string
  namespace: string
  nodeID: string
  tooltipText: string
  x: string
  y: string
}

// For graph component
export interface NodePoint {
  x: number
  y: number
}

export interface SZLRequest {
  semanticZoomLevel: number
  verticesInFocus: string[]
}

export interface TopologyGraph {
  namespace: string
  description: string
  label: string
  index?: number
}

export interface TopologyGraphList {
  graphs: TopologyGraph[]
  id: string
  label: string
  description?: string
  type?: string
}
