// types of right click menu
export enum ContextMenuType {
  node = 'node',
  background = 'background'
}

export enum ViewType {
  map = 'map',
  d3 = 'd3',
  circle = 'circle'
}

interface GraphType {
  [key: string]: string
}
export const DisplayType: GraphType = {
  application: 'application',
  bsm: 'bsm',
  'cities.transformers.substations.switches': 'powergrid',
  powergrid: 'powergrid',
  nodes: 'linkd', // nodes
  vmware: 'vmware'
}