import { rest } from './axiosInstances'
import { Summary } from '@/types'

const endpoint = '/notifications/summary'

const getSummary = async (): Promise<Summary> => {
  try {
    const resp = await rest.get(endpoint)
    return resp.data
  } catch (err) {
    return {} as Summary
  }
}

export { getSummary }
