import { Plugin } from '@/types'
import { rest } from './axiosInstances'

const endpoint = '/plugins'

const getPlugins = async (): Promise<Plugin[]> => {
  try {
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    return []
  }
}

export { getPlugins }
