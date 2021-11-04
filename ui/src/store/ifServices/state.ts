import { IfService } from '@/types'

export interface State {
  ifServices: IfService[]
  totalCount: number
}

const state: State = {
  ifServices: [],
  totalCount: 0
}

export default state
