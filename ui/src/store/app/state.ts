export interface State {
  theme: string
  navRailOpen: boolean
}

const state: State = {
  theme: localStorage.getItem('theme') as string,
  navRailOpen: true
}

export default state
