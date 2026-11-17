///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

import { describe, expect, test } from 'vitest'
import { createTestingPinia } from '@pinia/testing'
import useRole from '@/composables/useRole'
import { useAuthStore } from '@/stores/authStore'
import { WhoAmIResponse } from '@/types'

const setRoles = (roles: string[], loaded = true) => {
  const authStore = useAuthStore()
  authStore.whoAmI = { roles } as WhoAmIResponse
  authStore.loaded = loaded
}

describe('useRole test', () => {
  test('returns role access correctly', () => {
    createTestingPinia()
    setRoles(['ROLE_DEVICE_CONFIG_BACKUP'])

    const { adminRole, dcbRole, rolesAreLoaded } = useRole()
    expect(adminRole.value).toBe(false)
    expect(dcbRole.value).toBe(true)
    expect(rolesAreLoaded.value).toBe(true)
  })

  // Each test gets its own pinia; useRole must read the active one, not the first one it ever saw.
  test('reads the active pinia, not one from an earlier test', () => {
    createTestingPinia()
    setRoles(['ROLE_ADMIN'], false)

    const { adminRole, dcbRole, rolesAreLoaded } = useRole()
    expect(adminRole.value).toBe(true)
    expect(dcbRole.value).toBe(true)
    expect(rolesAreLoaded.value).toBe(false)
  })

  test('reacts when the roles load', () => {
    createTestingPinia()

    const { adminRole, rolesAreLoaded } = useRole()
    expect(adminRole.value).toBe(false)
    expect(rolesAreLoaded.value).toBe(false)

    setRoles(['ROLE_ADMIN'])
    expect(adminRole.value).toBe(true)
    expect(rolesAreLoaded.value).toBe(true)
  })
})
