import { useAuthStore } from '@/stores/authStore'

const enum Roles {
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_USER = 'ROLE_USER',
  ROLE_REST = 'ROLE_REST',
  ROLE_FILESYSTEM_EDITOR = 'ROLE_FILESYSTEM_EDITOR',
  ROLE_DEVICE_CONFIG_BACKUP = 'ROLE_DEVICE_CONFIG_BACKUP'
}

type Role = typeof Roles[keyof typeof Roles]

const authStore = computed(() => useAuthStore())

const roles = computed(() => authStore.value.whoAmI.roles)
const rolesAreLoaded = computed(() => authStore.value.loaded)

const hasOneOf = (...rolesToCheck: Role[]) => {
  for (const role of rolesToCheck) {
    if (roles.value.includes(role)) {
      return true
    }
  }
  return false
}

const useRole = () => {
  const adminRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN))
  const filesystemEditorRole = computed<boolean>(() => hasOneOf(Roles.ROLE_FILESYSTEM_EDITOR))
  const dcbRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN, Roles.ROLE_REST, Roles.ROLE_DEVICE_CONFIG_BACKUP))

  return { adminRole, filesystemEditorRole, dcbRole, rolesAreLoaded }
}

export default useRole
