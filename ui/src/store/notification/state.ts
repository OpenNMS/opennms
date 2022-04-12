import { NotificationSeverity, Notification, Toast } from '@/types'

export interface State {
  notification: Notification
  toast: Toast
}

const state: State = {
  notification: { msg: '', severity: NotificationSeverity.ERROR },
  toast: {
    basic: '',
    detail: '',
    hasErrors: false
  }
}

export default state
