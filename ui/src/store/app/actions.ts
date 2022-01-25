import { VuexContext } from '@/types'

const setTheme = async (context: VuexContext, theme: string) => context.commit('SET_THEME', theme)

const setNavRailOpen = async (context: VuexContext, navRailOpen: boolean) =>
  context.commit('SET_NAV_RAIL_OPEN', navRailOpen)

export default {
  setTheme,
  setNavRailOpen
}
