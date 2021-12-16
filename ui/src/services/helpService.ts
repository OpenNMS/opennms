import { v2 } from './axiosInstances'

const endpoint = '/openapi.json'

const getOpenApi = async (): Promise<any> => {
  try {
    const resp = await v2.get(endpoint)
    return resp.data
  } catch (err) {
    return {} as any
  }
}

export { getOpenApi }
