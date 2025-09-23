import { rest } from './axiosInstances'
import { WhoAmIResponse } from '@/types'

const endpoint = '/whoami'

const getWhoAmI = async (): Promise<WhoAmIResponse | false> => {
  try {
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    return false
  }
}

export { getWhoAmI }
