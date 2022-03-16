import { VuexContext, Notification, Toast } from '@/types'

const setNotification = (context: VuexContext, notification: Notification): void => {
  context.commit('SET_NOTIFICATION', notification)
}

const setToast = (context: VuexContext, toast: Toast): void => {
  context.commit('SET_TOAST_INFO', toast)
}

export default {
  setNotification,
  setToast
}
