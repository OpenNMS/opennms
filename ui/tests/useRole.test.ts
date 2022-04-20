import useRole from '@/composables/useRole'
import store from '@/store'
import { test, assert } from 'vitest'
import { WhoAmIResponse } from '@/types'

const mockWhoAmI = {
  roles: ['ROLE_DEVICE_CONFIG_BACKUP']
} as WhoAmIResponse

test('returns role access correctly', () => {
  store.commit('authModule/SAVE_WHO_AM_I_TO_STATE', mockWhoAmI)
  const { adminRole, dcbRole } = useRole()
  assert.equal(adminRole.value, false)
  assert.equal(dcbRole.value, true)
})
