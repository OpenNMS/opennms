import { Category, MonitoringLocation, Node, SetOperator } from '@/types'
import { isNumber } from '@/lib/utils'

const buildSearchQuery = (searchVal: string) => {
  if (searchVal.length > 0) {
    const star = searchVal.endsWith('*') ? '' : '*'
    return `node.label==${searchVal}${star}`
  }

  return ''
}

const buildCategoryQuery = (selectedCategories: Category[], categoryMode: SetOperator) => {
  const categoryItems = selectedCategories.map(cat => `category.id==${cat.id}`)

  if (categoryItems.length === 1) {
    return `${categoryItems[0]}`
  } else if (categoryItems.length > 1) {
    const separator = categoryMode === SetOperator.Intersection ? ';' : ','
    return `(${categoryItems.join(separator)})`
  }

  return ''
}

const buildFlowsQuery = (selectedFlows: string[]) => {
  const hasIngress = selectedFlows.some(f => f === 'Ingress')
  const hasEgress = selectedFlows.some(f => f === 'Egress')

  const flowItems = [
    hasIngress ? 'lastIngressFlow=gt=0' : '',
    hasEgress ? 'lastEgressFlow=gt=0' : ''
  ].filter(x => x)

  if (flowItems.length === 1) {
    return `${flowItems[0]}`
  } else if (flowItems.length > 1) {
    return `(${flowItems.join(',')})`
  }

  return ''
}

const buildLocationsQuery = (selectedLocations: MonitoringLocation[]) => {
  const locationItems = selectedLocations.map(loc => `node.location.locationName==${loc.name}`)

  if (locationItems.length === 1) {
    return `${locationItems[0]}`
  } else if (locationItems.length > 1) {
    return `(${locationItems.join(',')})`
  }

  return ''
}

export interface NodeStructureQueryParams {
  searchVal: string,
  selectedCategories: Category[],
  categoryMode: SetOperator,
  selectedFlows: string[],
  selectedLocations: MonitoringLocation[]
}

export const buildNodeStructureQuery = (
  { searchVal, selectedCategories, categoryMode, selectedFlows, selectedLocations }: NodeStructureQueryParams) => {
  const searchQuery = buildSearchQuery(searchVal)
  const categoryQuery = buildCategoryQuery(selectedCategories, categoryMode)
  const flowsQuery = buildFlowsQuery(selectedFlows)
  const locationQuery = buildLocationsQuery(selectedLocations)

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  const query = [searchQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
}

export const hasIngressFlow = (node: Node) => {
  return node.lastIngressFlow && isNumber(node.lastIngressFlow)
}

export const hasEgressFlow = (node: Node) => {
  return node.lastEgressFlow && isNumber(node.lastEgressFlow)
}
