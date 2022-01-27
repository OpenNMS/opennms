import { State } from './state'

const SET_THEME = (state: State, theme: string) => {
  state.theme = theme
}

const SET_NAV_RAIL_OPEN = (state: State, navRailOpen: boolean) => {
  state.navRailOpen = navRailOpen
}

export default {
  SET_THEME,
  SET_NAV_RAIL_OPEN
}
