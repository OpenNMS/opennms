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
import useAdminWriteFeedback from '@/composables/useAdminWriteFeedback'

const showSnackBar = vi.hoisted(() => vi.fn())
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

const STALE = 'This user was changed by someone else. The list has been reloaded; reapply your change.'

describe('useAdminWriteFeedback', () => {
  const { reportWrite } = useAdminWriteFeedback()
  const close = vi.fn()
  const banner = vi.fn()

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('on success shows the success message, closes and returns true', () => {
    expect(reportWrite(null, 'User \'jdoe\' updated.', '', { close, banner })).toBe(true)

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'User \'jdoe\' updated.' }]])
    expect(close).toHaveBeenCalledTimes(1)
    expect(banner).not.toHaveBeenCalled()
  })

  it('on success whose reload failed shows the success message, then the reload error', () => {
    reportWrite(null, 'User \'jdoe\' updated.', 'Failed to load users.', { close, banner })

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'User \'jdoe\' updated.' }], [{ msg: 'Failed to load users.', error: true }]])
    expect(close).toHaveBeenCalledTimes(1)
  })

  it('on a stale write shows the stale text once, closes and returns false', () => {
    // a reload error belongs to a successful write only
    expect(reportWrite({ message: STALE, stale: true }, 'User \'jdoe\' updated.', 'Failed to load users.', { close, banner })).toBe(false)

    expect(showSnackBar.mock.calls).toEqual([[{ msg: STALE, error: true }]])
    expect(close).toHaveBeenCalledTimes(1)
    expect(banner).not.toHaveBeenCalled()
  })

  it('on another error shows it in the banner only, stays open and returns false', () => {
    expect(reportWrite({ message: 'User \'x\' already exists.', stale: false }, 'User \'jdoe\' renamed to \'x\'.', '', { close, banner })).toBe(false)

    expect(banner).toHaveBeenCalledWith('User \'x\' already exists.')
    expect(showSnackBar).not.toHaveBeenCalled()
    expect(close).not.toHaveBeenCalled()
  })

  it('without a banner shows another error as a snackbar', () => {
    reportWrite({ message: 'User \'jdoe\' cannot be deleted.', stale: false }, 'User \'jdoe\' deleted.', '')

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'User \'jdoe\' cannot be deleted.', error: true }]])
  })

  it('without a close still reports success and a stale write', () => {
    reportWrite(null, 'User \'jdoe\' deleted.', '')
    reportWrite({ message: STALE, stale: true }, 'User \'jdoe\' deleted.', '')

    expect(showSnackBar.mock.calls).toEqual([[{ msg: 'User \'jdoe\' deleted.' }], [{ msg: STALE, error: true }]])
  })
})
