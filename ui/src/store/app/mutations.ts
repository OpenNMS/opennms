import { State } from './state'

const SET_THEME = (state: State, theme: string) => {
  state.theme = theme
}

export default {
  SET_THEME
}
