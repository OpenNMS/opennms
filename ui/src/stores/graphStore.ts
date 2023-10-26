import { defineStore } from 'pinia'
import API from '@/services'
import {
  GraphMetricsPayload,
  GraphMetricsResponse,
  PreFabGraph,
  Resource,
  ResourceDefinitionsApiResponse
} from '@/types'
import { uniq } from 'lodash'
import { sortBy } from 'lodash'

export interface GraphDefinition {
  id: string
  definitions: string[]
  label: string
}

export const useGraphStore = defineStore('graphStore', () => {
  const definitions = ref<GraphDefinition[]>([])
  const definitionDataObjects = ref<PreFabGraph[]>([])
  const graphMetrics = ref<GraphMetricsResponse[]>([])
  const definitionsList = ref<string[]>([])
  const nameOrderMap = ref(new Map<string,number>())

  const getGraphDefinitionsByResourceIds = async (ids: string[], nodeResources: Resource[]) => {
    let idsWithDefinitions: GraphDefinition[] = []
    const resourceAndPromises: { [x: string]: Promise<ResourceDefinitionsApiResponse>[] } = {}

    const resources: Resource[] = JSON.parse(
      JSON.stringify(nodeResources)
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
        defs.map((definition) => ({ name: definition, order: nameOrderMap.value.get(definition) })),
        ['order']
      ).map(definition => definition.name)

      idsWithDefinitions = [...idsWithDefinitions, { id, definitions: sortedDefinitions, label: getLabelFromId(id) }]
    }

    const totalDefinitionsList = uniq(idsWithDefinitions.map((item) => item.definitions).flat())

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

  const saveNameOrderMap = (graphs: PreFabGraph[]) => {
    const newMap = new Map<string,number>()

    for (const graph of graphs) {
      newMap.set(graph.name, graph.order)
    }

    nameOrderMap.value = newMap
  }

  const getPreFabGraphs = async (node: string) => {
    const preFabGraphs = await API.getPreFabGraphs(node)

    saveNameOrderMap(preFabGraphs)
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
