import { defineStore } from 'pinia'
import { sortBy, uniq } from 'lodash'
import API from '@/services'
import {
  GraphDefinition,
  GraphMetricsPayload,
  GraphMetricsResponse,
  PreFabGraph,
  Resource,
  ResourceDefinitionsApiResponse
} from '@/types'

export const useGraphStore = defineStore('graphStore', () => {
  const definitions = ref([] as GraphDefinition[])
  const definitionDataObjects = ref([] as PreFabGraph[])
  const graphMetrics = ref([] as GraphMetricsResponse[])
  const definitionsList = ref([] as string[])
  const nameOrderMap = ref({} as Record<string,number>)

  // nodeResource comes from resourceModule, either pass it in or else call useResourceStore() here
  const getGraphDefinitionsByResourceIds = async (nodeResource: Resource, ids: string[]) => {
    let idsWithDefinitions: { id: string; definitions: string[]; label: string }[] = []
    const resourceAndPromises: { [x: string]: Promise<ResourceDefinitionsApiResponse>[] } = {}

    // nodeResource is from resourceModule
    const resources: Resource[] = JSON.parse(
      JSON.stringify(nodeResource.children?.resource || [])
    )

    const getLabelFromId = (id: string) => {
      for (const resource of resources) {
        if (resource.id === id) {
          return resource.label
        }
      }
      return ''
    }

    for (const id of ids) {
      if (resourceAndPromises[id]) {
        resourceAndPromises[id] = [...resourceAndPromises[id], API.getGraphDefinitionsByResourceId(id)]
      } else {
        resourceAndPromises[id] = [API.getGraphDefinitionsByResourceId(id)]
      }
    }

    for (const id in resourceAndPromises) {
      let defs: string[] = []
      const promises = resourceAndPromises[id]
      const results = await Promise.all(promises)

      for (const result of results) {
        defs = [...defs, ...result.name]
      }

      // sorts by preFabGraph order value
      const sortedDefinitions = sortBy(
        defs.map(definition => ({ name: definition, order: nameOrderMap.value[definition] })),
        ['order']
      ).map(definition => definition.name)

      idsWithDefinitions = [...idsWithDefinitions, { id, definitions: sortedDefinitions, label: getLabelFromId(id) }]
    }

    const totalDefinitionsList = uniq(idsWithDefinitions.map(item => item.definitions).flat())

    definitionsList.value = totalDefinitionsList
    definitions.value = idsWithDefinitions
  }

  const getDefinitionData = async (definition: string): Promise<PreFabGraph | null> => {
    const definitionData = await API.getDefinitionData(definition)

    if (definitionData) {
      definitionDataObjects.value = [...definitionDataObjects.value, definitionData]
    }

    return definitionData
  }

  const getPreFabGraphs = async (node: string) => {
    const preFabGraphs = await API.getPreFabGraphs(node)

    const newMap: { [name: string]: number } = {}

    for (const graph of preFabGraphs) {
      newMap[graph.name] = graph.order
    }

    nameOrderMap.value = newMap
  }

  const getGraphMetrics = async (payload: GraphMetricsPayload): Promise<GraphMetricsResponse | null> => {
    const metrics = await API.getGraphMetrics(payload)

    if (metrics) {
      graphMetrics.value = [...graphMetrics.value, metrics]
    }

    return metrics
  }

  return {
    definitions,
    definitionDataObjects,
    graphMetrics,
    definitionsList,
    nameOrderMap,
    getGraphDefinitionsByResourceIds,
    getDefinitionData,
    getPreFabGraphs,
    getGraphMetrics
  }
})
