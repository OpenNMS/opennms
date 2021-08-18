import { SearchResultResponse } from '@/types'
import { State } from './state'

const SAVE_SEARCH_RESULTS = (state: State, results: SearchResultResponse[]) => {
  state.searchResults = results
}

export default {
  SAVE_SEARCH_RESULTS
}
