import { v2 } from './axiosInstances'
import { SearchResultResponse } from '@/types'

const endpoint = '/search'

const search = async (searchStr: string): Promise<SearchResultResponse[] | false> => {
  try {
    const resp = await v2.get(`${endpoint}?_s=${searchStr}`)

    // no content from server
    if (resp.status === 204) {
      return []
    }

    return resp.data
  } catch (err) {
    return false
  }
}

export { search }
