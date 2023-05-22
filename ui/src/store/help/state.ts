export interface State {
  openApi: Record<string, unknown>
  openApiV1: Record<string, unknown>
}

const state: State = {
  openApi: {},
  openApiV1: {}
}

export default state
