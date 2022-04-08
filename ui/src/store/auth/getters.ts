import { State } from './state'

const ROLE_ADMIN = 'ROLE_ADMIN'

const isAdmin = (state: State) => state.whoAmi.roles.includes(ROLE_ADMIN)

export default {
  isAdmin
}
