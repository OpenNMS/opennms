import store from '@/store'

const enum Roles {
  ROLE_ADMIN = 'ROLE_ADMIN', 
  ROLE_USER = 'ROLE_USER', 
  ROLE_REST = 'ROLE_REST',
  ROLE_DEVICE_CONFIG_BACKUP = 'ROLE_DEVICE_CONFIG_BACKUP'
}

type Role = typeof Roles[keyof typeof Roles];

const roles = computed(() => store.state.authModule.whoAmi.roles)
const rolesAreLoaded = computed(() => store.state.authModule.loaded)

const hasOneOf = (...rolesToCheck: Role[]) =>{
  for (const role of rolesToCheck) {
    if (roles.value.includes(role)) {
      return true
    }
  }
  return false
}

const useRole = () => {
  const adminRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN))
  const dcbRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN, Roles.ROLE_REST, Roles.ROLE_DEVICE_CONFIG_BACKUP))

  return { adminRole, dcbRole, rolesAreLoaded }
}

export default useRole
