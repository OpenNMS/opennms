import { SearchResultResponse, SearchResultsByContext } from '@/types'

export interface State {
  searchResults: SearchResultResponse[],
  searchResultsByContext: SearchResultsByContext,
  loading: boolean
}

const state: State = {
  searchResults: [],
  searchResultsByContext: [],
  loading: false
}

export default state
