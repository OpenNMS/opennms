import { MainMenu, NotificationSummary } from '@/types/mainMenu'

export interface State {
  mainMenu: MainMenu,
  notificationSummary: NotificationSummary
}

const state: State = {
  mainMenu: {} as MainMenu,
  notificationSummary: {} as NotificationSummary
}

export default state
