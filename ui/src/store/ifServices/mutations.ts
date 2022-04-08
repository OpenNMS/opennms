import { IfService } from '@/types'
import { State } from './state'

const SAVE_IF_SERVICES_TO_STATE = (state: State, ifServices: IfService[]) => {
  state.ifServices = ifServices
}

const SAVE_IF_SERVICES_TOTAL_COUNT = (state: State, totalCount: number) => {
  state.totalCount = totalCount
}

export default {
  SAVE_IF_SERVICES_TO_STATE,
  SAVE_IF_SERVICES_TOTAL_COUNT
}
