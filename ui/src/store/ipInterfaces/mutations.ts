import { IpInterface } from '@/types'
import { State } from './state'

const SAVE_ALL_IP_INTERFACES_TO_STATE = (state: State, ipInterfaces: IpInterface[]) => {
  state.ipInterfaces = [...ipInterfaces]
}

export default {
  SAVE_ALL_IP_INTERFACES_TO_STATE
}
