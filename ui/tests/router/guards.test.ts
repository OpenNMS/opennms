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

import { requireRole } from '@/main/router/guards'
import useRole from '@/composables/useRole'
import { useAuthStore } from '@/stores/authStore'
import { WhoAmIResponse } from '@/types'
import { createTestingPinia } from '@pinia/testing'
import { beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { RouteLocationNormalized, START_LOCATION } from 'vue-router'

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

const to = { path: '/notifications-config', matched: [{}] } as unknown as RouteLocationNormalized
// an in-app navigation comes from a matched route; a fresh load from START_LOCATION
const fromInApp = { path: '/notifications', matched: [{}] } as unknown as RouteLocationNormalized
const fromFreshLoad = START_LOCATION

const run = (from: RouteLocationNormalized) => {
  const { adminRole } = useRole()
  const guard = requireRole(adminRole, 'Must be admin.') as (...args: unknown[]) => Promise<unknown>
  return guard(to, from, () => undefined)
}

describe('requireRole', () => {
  // useRole caches the auth store at module level, so keep one pinia for the file
  beforeAll(() => {
    createTestingPinia()
  })

  beforeEach(() => {
    const authStore = useAuthStore()
    authStore.loaded = true
    authStore.whoAmI = { roles: ['ROLE_USER'] } as WhoAmIResponse
    showSnackBar.mockClear()
  })

  it('lets a user with the role through', async () => {
    useAuthStore().whoAmI = { roles: ['ROLE_ADMIN'] } as WhoAmIResponse

    expect(await run(fromInApp)).toBe(true)
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('cancels an in-app navigation for a user without the role, with an error snackbar', async () => {
    expect(await run(fromInApp)).toBe(false)
    expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Must be admin.', error: true })
  })

  it('sends a fresh load (nowhere to stay) home', async () => {
    expect(await run(fromFreshLoad)).toBe('/')
  })

  it('waits for the roles before deciding, so the page never mounts first', async () => {
    const authStore = useAuthStore()
    authStore.loaded = false

    let decided: unknown = 'pending'
    const pending = run(fromFreshLoad).then((result) => {
      decided = result
    })
    await nextTick()
    expect(decided).toBe('pending')

    authStore.whoAmI = { roles: ['ROLE_ADMIN'] } as WhoAmIResponse
    authStore.loaded = true
    await pending

    expect(decided).toBe(true)
  })
})
