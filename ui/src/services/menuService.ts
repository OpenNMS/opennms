import { rest, v2 } from './axiosInstances'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'

const menuEndpoint = 'menu'
const notificationSummaryEndpoint = 'notifications/summary'

const getMainMenu = async (): Promise<MainMenu | false> => {
  try {
    const resp = await v2.get(menuEndpoint)
    return resp.data
  } catch (err) {
    return false
  }
}

const getNotificationSummary = async (): Promise<NotificationSummary | false> => {
  try {
    const resp = await rest.get(notificationSummaryEndpoint)
    return resp.data
  } catch (err) {
    return false
  }
}

export {
  getMainMenu,
  getNotificationSummary
}
