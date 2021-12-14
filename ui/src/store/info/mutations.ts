import { AppInfo } from '@/types'
import { State } from './state'

const SET_INFO = (state: State, info: AppInfo) => {
  state.info = info
}

export default {
  SET_INFO
}
