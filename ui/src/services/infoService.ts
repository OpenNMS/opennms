import { AppInfo } from '@/types'
import { rest } from './axiosInstances'

const endpoint = '/info'

const getInfo = async (): Promise<AppInfo> => {
  try {
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    return {} as AppInfo
  }
}

export { getInfo }
