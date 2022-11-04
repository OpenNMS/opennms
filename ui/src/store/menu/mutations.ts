import { State } from './state'
import { MainMenu, NotificationSummary } from '@/types/mainMenu'

const SAVE_MAIN_MENU = (state: State, mainMenu: MainMenu) => {
  state.mainMenu = mainMenu
}

const SAVE_NOTIFICATION_SUMMARY = (state: State, notificationSummary: NotificationSummary) => {
  state.notificationSummary = notificationSummary
}

export default {
  SAVE_MAIN_MENU,
  SAVE_NOTIFICATION_SUMMARY
}