export interface State {
  logs: string[]
  log: string
  searchValue: string
  selectedLog: string
  reverseLog: boolean
}

const state: State = {
  logs: [],
  log: '',
  searchValue: '',
  selectedLog: '',
  reverseLog: false
}

export default state
