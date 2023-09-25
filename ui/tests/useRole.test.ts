import { assert, beforeAll, describe, test } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import useRole from '@/composables/useRole'
import { useAuthStore } from '@/stores/authStore'
import { WhoAmIResponse } from '@/types'

const mockWhoAmI = {
  roles: ['ROLE_DEVICE_CONFIG_BACKUP']
} as WhoAmIResponse

describe('useRole test', () => {
  beforeAll(() => {
    createTestingPinia()
  })

  test('returns role access correctly', () => {
    const authStore = useAuthStore()
    authStore.whoAmI = mockWhoAmI

    const { adminRole, dcbRole } = useRole()
    assert.equal(adminRole.value, false)
    assert.equal(dcbRole.value, true)
  })
})
