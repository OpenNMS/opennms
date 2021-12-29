import { State } from './state'
import { Notification } from '@/types'

const SET_NOTIFICATION = (state: State, notification: Notification): void => {
  state.notification = notification
}

export default {
  SET_NOTIFICATION
}
