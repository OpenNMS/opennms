import API from '@/services'
import { GraphMetricsPayload, GraphMetricsResponse, PreFabGraph, ResourceDefinitionsApiResponse, VuexContext } from '@/types'
import { uniq } from 'lodash'

const getGraphDefinitionsByResourceIds = async (context: VuexContext, ids: string[]) => {
  let idsWithDefinitions: { id: string, definitions: string[] }[] = []
  const resourceAndPromises: { [x: string]: Promise<ResourceDefinitionsApiResponse>[] } = {}

  for (const id of ids) {
    if (resourceAndPromises[id]) {
      resourceAndPromises[id] = [...resourceAndPromises[id], API.getGraphDefinitionsByResourceId(id)]
    } else {
      resourceAndPromises[id] = [API.getGraphDefinitionsByResourceId(id)]
    }
  }

  for (const id in resourceAndPromises) {
    let definitions: string[] = []
    const promises = resourceAndPromises[id]
    const results = await Promise.all(promises)
    for (const result of results) {
      definitions = [...definitions, ...result.name.sort((a, b) => a.localeCompare(b))]
    }
    const uniqueSortedDefinitions = uniq(definitions)
    idsWithDefinitions = [...idsWithDefinitions, { id, definitions: uniqueSortedDefinitions }]
  }

  const definitionsList = idsWithDefinitions.map((item) => item.definitions).flat()

  console.log(definitionsList)

  context.commit('SAVE_DEFINITIONS_LIST', definitionsList)
  context.commit('SAVE_DEFINITIONS', idsWithDefinitions)
}

const getDefinitionData = async (context: VuexContext, definition: string): Promise<PreFabGraph | null> => {
  const definitionData = await API.getDefinitionData(definition)

  if (definitionData) {
    context.commit('SAVE_DEFINITION_DATA', definitionData)
  }

  return definitionData
}

const getGraphMetrics = async (context: VuexContext, payload: GraphMetricsPayload): Promise<GraphMetricsResponse | null> => {
  const metrics = await API.getGraphMetrics(payload)

  if (metrics) {
    context.commit('SAVE_GRAPH_METRICS', metrics)
  }

  return metrics
}

export default {
  getGraphDefinitionsByResourceIds,
  getDefinitionData,
  getGraphMetrics
}
