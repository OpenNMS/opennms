export interface State {
  theme: string
}

const state: State = {
  theme: localStorage.getItem('theme') as string
}

export default state
