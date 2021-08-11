import { Commit, Dispatch } from 'vuex'

export interface VuexContext {
  commit: Commit,
  dispatch: Dispatch
}

export interface SearchResultResponse {
  label?: string
  context: {
    name: string, 
    weight: number
  }
  empty: boolean
  more: boolean
  results: {
    identifier: string
    label: string
    matches: any
    properties: any
    url: string
    weight: number
  }[]
}
