import { MainMenuDefinition } from '@/types/mainMenu'

export interface State {
  mainMenu: MainMenuDefinition
}

const state: State = {
  mainMenu: {} as MainMenuDefinition
}

export default state
