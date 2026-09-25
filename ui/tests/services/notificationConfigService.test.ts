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

import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getNotificationGroups, getNotificationUsers, testDestinationPath } from '@/services/notificationConfigService'
import { rest, v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() },
  v2: { get: vi.fn(), post: vi.fn(), put: vi.fn(), delete: vi.fn() }
}))

describe('notificationConfigService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('testDestinationPath', () => {
    // The trigger endpoint only exists on the v1 resource; posting to v2 404s,
    // which made the Test button permanently fail with the generic snackbar.
    it('triggers via the v1 rest instance, not v2', async () => {
      vi.mocked(rest.post).mockResolvedValue({ status: 204 })

      const ok = await testDestinationPath('Email Admins')

      expect(ok).toBe(true)
      expect(rest.post).toHaveBeenCalledWith('/notifications/destination-paths/Email%20Admins/trigger')
      expect(v2.post).not.toHaveBeenCalled()
    })

    it('returns false when the trigger fails', async () => {
      vi.mocked(rest.post).mockRejectedValue(new Error('boom'))

      expect(await testDestinationPath('Email-Admin')).toBe(false)
    })
  })

  // v2 /users and /groups return plain arrays of DTOs; the pickers read the
  // v1 shape ({ user: [{ 'user-id' }] }) by mistake and always came up empty.
  describe('users and groups for target pickers', () => {
    it('reads user ids from the v2 users array', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: [{ userId: 'admin', fullName: 'Administrator' }, { userId: 'rtc' }] })

      expect(await getNotificationUsers()).toEqual(['admin', 'rtc'])
      expect(v2.get).toHaveBeenCalledWith('/users?limit=0')
    })

    it('reads group names from the v2 groups array', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: [{ name: 'Admin', users: ['admin'] }] })

      expect(await getNotificationGroups()).toEqual(['Admin'])
      expect(v2.get).toHaveBeenCalledWith('/groups?limit=0')
    })

    it('returns an empty list when the body is not an array (e.g. 204)', async () => {
      vi.mocked(v2.get).mockResolvedValue({ data: '' })

      expect(await getNotificationUsers()).toEqual([])
      expect(await getNotificationGroups()).toEqual([])
    })

    it('returns null on failure so the tab loader retries', async () => {
      vi.mocked(v2.get).mockRejectedValue(new Error('boom'))

      expect(await getNotificationUsers()).toBeNull()
      expect(await getNotificationGroups()).toBeNull()
    })
  })
})
