import { Plugin } from '@/types'

export interface State {
  plugins: Plugin[]
}

const state: State = {
  plugins: []
}

export default state
