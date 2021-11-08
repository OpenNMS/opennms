export interface State {
  logs: string[]
  log: string
  searchValue: string
  selectedLog: string
}

const state: State = {
  logs: [],
  log: '',
  searchValue: '',
  selectedLog: ''
}

export default state
