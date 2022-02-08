import { Plugin } from '@/types'
import { v2 } from './axiosInstances'

const endpoint = '/plugins'

const getPlugins = async (): Promise<Plugin[]> => {
  try {
    const resp = await v2.get(`${endpoint}/extensions/enabled`)
    return resp.data
  } catch (err) {
    return []
  }
}

const updatePluginStatus = async (plugin: Plugin): Promise<Plugin> => {
  const updatedPlugin = {
    ...plugin,
    enabled: !plugin.enabled
  }

  try {
    const resp = await v2.put(`${endpoint}/extensions/${plugin.id}`, updatedPlugin)
    return resp.data
  } catch (err) {
    return {} as Plugin
  }
}

export { getPlugins, updatePluginStatus }
