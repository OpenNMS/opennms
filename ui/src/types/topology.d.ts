export interface VerticesAndEdges {
  vertices: ResponseVertex[]
  edges: ResponseEdge[]
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
export interface NetworkGraphEdge {
  source: string,
  target: string
}

export interface NetworkGraphVertex {
  name: string
}
