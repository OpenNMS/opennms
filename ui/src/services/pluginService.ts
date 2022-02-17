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

const getEnabledPlugins = async (): Promise<Plugin[]> => {
  try {
    const resp = await rest.get(`${endpoint}/enabled`)
    return resp.data
  } catch (err) {
    return []
  }
}

const togglePlugin = async (id: string): Promise<Plugin> => {
  try {
    const resp = await rest.put(`${endpoint}/toggle/${id}`)
    return resp.data
  } catch (err) {
    return {} as Plugin
  }
}

export { getPlugins, getEnabledPlugins, togglePlugin }
