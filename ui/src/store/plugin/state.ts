import { Plugin } from '@/types'

export interface State {
  plugins: Plugin[]
  enabledPlugins: Plugin[]
}

const state: State = {
  plugins: [],
  enabledPlugins: []
}

export default state
