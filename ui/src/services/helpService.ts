import { v2 } from './axiosInstances'

const endpoint = '/openapi.json'

const getOpenApi = async (): Promise<Record<string, unknown>> => {
  try {
    const resp = await v2.get(endpoint)
    return resp.data
  } catch (err) {
    return {}
  }
}

export { getOpenApi }
