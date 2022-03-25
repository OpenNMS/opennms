import { NodePoint } from '@/types/topology'
import { Layouts } from 'v-network-graph'
import { State } from './state'

const getCircleLayout = (state: State): Record<string, NodePoint> => {
  const centerY = 350
  const centerX = 350
  const radius = 250

  const vertexNames = Object.keys(state.verticies)
  const layout = {} as Record<string, NodePoint>

  for (let i = 0; i < vertexNames.length; i++) {
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

export default {
  getLayout
}
