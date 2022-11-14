import { State } from './state'

const SAVE_PROVISION_SERVICE = (state: State, provisionDService: any) => {
  state.provisionDService = provisionDService
}

export default { SAVE_PROVISION_SERVICE }
