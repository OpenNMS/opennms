import { Event } from '@/types'

export interface State {
  events: Event[]
  totalCount: number
}

const state: State = {
  events: [],
  totalCount: 0
}

export default state
