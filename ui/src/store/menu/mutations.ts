import { State } from './state'
import { MainMenu } from '@/types/mainMenu'

const SAVE_MAIN_MENU = (state: State, mainMenu: MainMenu) => {
  state.mainMenu = mainMenu
}

export default {
  SAVE_MAIN_MENU
}