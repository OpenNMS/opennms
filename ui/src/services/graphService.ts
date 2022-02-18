import { rest } from './axiosInstances'
import {
  ResourceDefinitionsApiResponse,
  PreFabGraph,
  GraphMetricsPayload,
  GraphMetricsResponse
} from '@/types'

const endpoint = '/graphs'

const getGraphDefinitionsByResourceId = async (id: string): Promise<ResourceDefinitionsApiResponse> => {
  try {
    const resp = await rest.get(`${endpoint}/for/${id}`)
    return resp.data
  } catch (err) {
    return (<unknown>{ name: [] }) as ResourceDefinitionsApiResponse
  }
}

const getPreFabGraphs = async (node: string): Promise<PreFabGraph[]> => {
  try {
    const resp = await rest.get(`${endpoint}/fornode/${node}`)
    return resp.data['prefab-graphs']['prefab-graph']
  } catch (err) {
    return []
  }
}

const getDefinitionData = async (definition: string): Promise<PreFabGraph | null> => {
  try {
    const resp = await rest.get(`${endpoint}/${definition}`)
    return resp.data
  } catch (err) {
    return null
  }
}

const getGraphMetrics = async (payload: GraphMetricsPayload): Promise<GraphMetricsResponse | null> => {
  try {
    const resp = await rest.post('/measurements', payload)
    return resp.data
  } catch (err) {
    return null
  }
}

export { getGraphDefinitionsByResourceId, getDefinitionData, getGraphMetrics, getPreFabGraphs }
