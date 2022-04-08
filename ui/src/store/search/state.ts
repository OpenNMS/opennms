import { SearchResultResponse } from '@/types'

export interface State {
  searchResults: SearchResultResponse[]
}

const state: State = {
  searchResults: []
}

export default state
