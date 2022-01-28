import { Resource } from '@/types'

export interface State {
  resources: Resource[]
  nodeResource: Resource
  searchValue: string
}

const state: State = {
  resources: [],
  nodeResource: {} as Resource,
  searchValue: ''
}

export default state
