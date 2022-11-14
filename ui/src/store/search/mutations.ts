import { SearchResultResponse, SearchResultsByContext } from '@/types'
import { State } from './state'

const SAVE_SEARCH_RESULTS = (state: State, items:{responses: SearchResultResponse[], responsesByLabel: SearchResultsByContext}) => {
  state.searchResults = items.responses
  state.searchResultsByContext = [...items.responsesByLabel]
}

const SET_LOADING = (state: State, loading: boolean) => {
  state.loading = loading
}


export default {
  SAVE_SEARCH_RESULTS,
  SET_LOADING
}
