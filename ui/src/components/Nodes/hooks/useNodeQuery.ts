import {
  Category,
  MonitoringLocation,
  NodeQueryFilter,
  QueryParameters,
  SetOperator
} from '@/types'

export const useNodeQuery = () => {
  const getDefaultNodeQueryFilter = () => {
    return {
      searchTerm: '',
      categoryMode: SetOperator.Union,
      selectedCategories: [] as Category[],
      selectedFlows: [] as string[],
      selectedMonitoringLocations: [] as MonitoringLocation[]
    } as NodeQueryFilter
  }

  /**
   * Build new QueryParameters based on existing QueryParameters (which contain e.g. limit, offset and similar), 
   * combined with the given NodeQueryFilter.
   */
  const buildUpdatedNodeStructureQueryParameters = (queryParameters: QueryParameters, filter: NodeQueryFilter) => {
    const searchQuery = buildNodeStructureQuery(filter)
    const searchQueryParam: QueryParameters = { _s: searchQuery }
    const updatedParams = { ...queryParameters, ...searchQueryParam }

    // if there is no search query, remove the '_s' property entirely so it doesn't
    // get put into the API request query string
    if (!searchQuery) {
      delete updatedParams._s
    }

    return updatedParams as QueryParameters
  }

  const trackedNodeQueryStringProperties = new Set([
    'nodename', 'nodeLabel', 'categories'
  ])

  /**
   * Check if vue-router route.query object has any query string values we are tracking.
   */
  const queryStringHasTrackedValues = (queryObject: any) => {
    return Object.getOwnPropertyNames(queryObject).some(x => trackedNodeQueryStringProperties.has(x))
  }

  /**
   * Build a node query from a query string coming from another page. queryObject is from vue-router route.query.
   * Used to set the new search query as well as NodeFilterPreferences, which will
   * replace the ones user had previously set.
   * Note, this creates a new filter, should only be called when queryObject has one or more values we are tracking.
   *
   * @param query query object from vue-router route.query
   */
  const buildNodeQueryFilterFromQueryString = (queryObject: any, categories: Category[]) => {
    const filter: NodeQueryFilter = getDefaultNodeQueryFilter()

    const nodeLabel = queryObject.nodename as string || queryObject.nodeLabel as string

    if (nodeLabel) {
      filter.searchTerm = nodeLabel
    }

    // 'categories' can be a comma or semicolon separated list of either numeric Category ids or names
    // comma: Union; semicolon: Intersection
    const selectedCategories = queryObject.categories as string ?? ''

    if (selectedCategories.length > 0) {
      filter.categoryMode = selectedCategories.includes(';') ? SetOperator.Intersection : SetOperator.Union

      const cats: string[] = selectedCategories.replace(';', ',').split(',')

      // add any valid categories
      cats.forEach(c => {
        if (/\d+/.test(c)) {
          // category id number
          const id = parseInt(c)

          const item = categories.find(x => x.id === id)

          if (item) {
            filter.selectedCategories.push(item)
          }
        } else {
          // category name, case insensitive
          const item = categories.find(x => x.name.toLowerCase() === c.toLowerCase())

          if (item) {
            filter.selectedCategories.push(item)
          }
        }
      })
    }

    return filter
  }

  return {
    buildNodeQueryFilterFromQueryString,
    buildUpdatedNodeStructureQueryParameters,
    getDefaultNodeQueryFilter,
    queryStringHasTrackedValues
  }
}

const buildSearchQuery = (searchTerm: string) => {
  if (searchTerm?.length > 0) {
    const startStar = searchTerm.startsWith('*') ? '' : '*'
    const endStar = searchTerm.endsWith('*') ? '' : '*'
    return `node.label==${startStar}${searchTerm}${endStar}`
  }

  return ''
}

/**
 * Build a FIQL query for the Node Rest service from a NodeQueryFilter.
 */
const buildNodeStructureQuery = (filter: NodeQueryFilter) => {
  const searchQuery = buildSearchQuery(filter.searchTerm)
  const categoryQuery = buildCategoryQuery(filter.selectedCategories, filter.categoryMode)
  const flowsQuery = buildFlowsQuery(filter.selectedFlows)
  const locationQuery = buildLocationsQuery(filter.selectedMonitoringLocations)

  // TODO: filter on regex to screen out bad FIQL characters like ',', ';', etc.
  // and/or restrict characters in the FeatherInput above
  const query = [searchQuery, categoryQuery, flowsQuery, locationQuery].filter(s => s.length > 0).join(';')

  return query
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
