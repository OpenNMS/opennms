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

import { describe, it, expect, beforeEach, vi } from 'vitest'
import { createSnmpCollectionSource } from '@/services/snmpDataCollectionService'
import { v2 } from '@/services/axiosInstances'

vi.mock('@/services/axiosInstances', () => ({
  v2: {
    get: vi.fn(),
    post: vi.fn(),
    put: vi.fn(),
    delete: vi.fn(),
    patch: vi.fn()
  }
}))

describe('snmpDataCollectionService', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  describe('createSnmpCollectionSource', () => {
    it('should POST to /datacollectionconf/collectsources with name and profiles', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 201, data: 42 })

      await createSnmpCollectionSource('my-source', ['default'])

      expect(v2.post).toHaveBeenCalledWith('/datacollectionconf/collectsources', {
        name: 'my-source',
        profiles: ['default']
      })
    })

    it('should return the new source ID on 201', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 201, data: 7 })

      const result = await createSnmpCollectionSource('my-source', ['default'])

      expect(result).toBe(7)
    })

    it('should return null on non-201 response', async () => {
      vi.mocked(v2.post).mockResolvedValue({ status: 400, data: null })

      const result = await createSnmpCollectionSource('duplicate-source', ['default'])

      expect(result).toBeNull()
    })

    it('should return null and log an error on network failure', async () => {
      const consoleSpy = vi.spyOn(console, 'error').mockImplementation(() => {})
      vi.mocked(v2.post).mockRejectedValue(new Error('Network error'))

      const result = await createSnmpCollectionSource('my-source', ['default'])

      expect(result).toBeNull()
      expect(consoleSpy).toHaveBeenCalledWith(
        'Error creating SNMP data collection source:',
        expect.any(Error)
      )
      consoleSpy.mockRestore()
    })
  })
})
