import { v2, rest } from './axiosInstances'

const endpoint = '/openapi.json'

const getOpenApi = async (): Promise<Record<string, unknown>> => {
  try {
    const resp = await v2.get(endpoint)
    return resp.data
  } catch (err) {
    return {}
  }
}

const getOpenApiV1 = async (): Promise<Record<string, unknown>> => {
  try {
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    return {}
  }
}

export { getOpenApiV1, getOpenApi }
