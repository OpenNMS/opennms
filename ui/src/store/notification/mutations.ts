import { State } from './state'
import { Notification, Toast } from '@/types'

const SET_NOTIFICATION = (state: State, notification: Notification): void => {
  state.notification = notification
}

const SET_TOAST_INFO = (state: State, toast: Toast): void => {
  state.toast = toast
}

export default {
  SET_NOTIFICATION,
  SET_TOAST_INFO
}
