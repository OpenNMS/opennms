import { rest } from './axiosInstances'
import { Resource, ResourcesApiResponse } from '@/types'

const endpoint = '/resources'

const getResources = async (): Promise<ResourcesApiResponse | null> => {
  try {
    const resp = await rest.get(`${endpoint}?depth=0`)

    if (resp.status === 204) {
      return { resource: [], count: 0, offset: 0, totalCount: 0 }
    }

    return resp.data
  } catch (err) {
    return null
  }
}

const getResourceForNode = async (name: string): Promise<Resource | null> => {
  try {
    const resp = await rest.get(`${endpoint}/fornode/${name}`)

    if (resp.status === 204) {
      return null
    }

    return resp.data
  } catch (err) {
    return null
  }
}

export { getResources, getResourceForNode }
