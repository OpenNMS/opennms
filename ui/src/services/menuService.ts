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
    console.log('DEBUG in getNotificationSummary, resp:')
    console.dir(resp)

    return resp.data
  } catch (err) {
    console.log('DEBUG in getNotificationSummary, err:')
    console.dir(err)
    return false
  }
}

export {
  getMainMenu,
  getNotificationSummary
}
