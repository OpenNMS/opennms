import { VuexContext, Notification } from '@/types'

const setNotification = (context: VuexContext, notification: Notification): void => {
  context.commit('SET_NOTIFICATION', notification)
}

export default {
  setNotification
}
