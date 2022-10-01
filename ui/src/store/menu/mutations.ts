import { State } from './state'
import { MainMenuDefinition } from '@/types/mainMenu'

const SAVE_MAIN_MENU_DEFINITION = (state: State, mainMenu: MainMenuDefinition) => {
  state.mainMenu = mainMenu
}

export default {
  SAVE_MAIN_MENU_DEFINITION
}