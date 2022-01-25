import { NotificationSeverity, Notification } from '@/types'

export interface State {
  notification: Notification
}

const state: State = {
  notification: { msg: '', severity: NotificationSeverity.ERROR }
}

export default state
