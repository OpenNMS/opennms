import { VuexContext } from '@/types'

const setTheme = async (context: VuexContext, theme: string) => context.commit('SET_THEME', theme)

export default {
  setTheme
}
