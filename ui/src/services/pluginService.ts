import { Plugin } from '@/types'
import { rest } from './axiosInstances'

const endpoint = '/plugins'

const getPlugins = async (): Promise<Plugin[]> => {
  try {
    const resp = await rest.get(`${endpoint}/extensions`)
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
    const resp = await rest.put(`${endpoint}/extensions/${plugin.extensionID}`, updatedPlugin)
    return resp.data
  } catch (err) {
    return {} as Plugin
  }
}

export { getPlugins, updatePluginStatus }
