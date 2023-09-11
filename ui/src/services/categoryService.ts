import { rest } from './axiosInstances'
import {
  CategoryApiResponse
} from '@/types'

const endpoint = '/categories'

const getCategories = async (): Promise<CategoryApiResponse | false> => {
  try {
    const resp = await rest.get(endpoint)

    // no content from server
    if (resp.status === 204) {
      return { category: [], totalCount: 0, count: 0, offset: 0 }
    }

    return resp.data
  } catch (err) {
    return false
  }
}

export {
  getCategories
}
