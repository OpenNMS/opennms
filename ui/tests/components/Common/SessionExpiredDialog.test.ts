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

import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import SessionExpiredDialog from '@/components/Common/SessionExpiredDialog.vue'
import { rest } from '@/services/axiosInstances'
import { sessionExpiredDialogVisible } from '@/services/sessionExpiry'

vi.mock('@/services/axiosInstances', () => ({
  rest: { get: vi.fn() },
  v2: { get: vi.fn() },
  restFile: { get: vi.fn() }
}))

// Shown when the session expired while the user may have unsaved changes. It must tell the user
// how to keep them (log in in another tab, come back, do the action again), and let them reload.

const mounted: { unmount: () => void }[] = []

const open = async () => {
  const wrapper = mount(SessionExpiredDialog, {
    global: { plugins: [PrimeVue] },
    attachTo: document.body
  })
  mounted.push(wrapper)
  sessionExpiredDialogVisible.value = true
  await nextTick()
  return wrapper
}

const dialog = () => document.querySelector('.p-dialog') as HTMLElement | null

const button = (label: string) => {
  const found = Array.from(document.querySelectorAll('.p-dialog button'))
    .find(b => b.textContent?.trim() === label) as HTMLButtonElement | undefined
  expect(found, `no "${label}" button rendered`).toBeTruthy()
  return found!
}

describe('SessionExpiredDialog', () => {
  let reload: ReturnType<typeof vi.fn>

  beforeEach(() => {
    reload = vi.fn()
    vi.stubGlobal('location', { reload, pathname: '/opennms/ui/' } as any)
    vi.mocked(rest.get).mockReset()
  })

  afterEach(() => {
    while (mounted.length) {
      mounted.pop()!.unmount()
    }
    sessionExpiredDialogVisible.value = false
    document.body.innerHTML = ''
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('is hidden until the session expires', () => {
    mounted.push(mount(SessionExpiredDialog, { global: { plugins: [PrimeVue] }, attachTo: document.body }))
    expect(dialog()).toBeNull()
  })

  it('tells the user how to keep the changes on the page', async () => {
    await open()
    const text = dialog()!.textContent ?? ''
    expect(text).toContain('Your session has expired')
    expect(text).toContain('Log in in a new tab')
    expect(text).toContain('come back to this tab')
    expect(text).toContain('Do your last action again')
    expect(text).toContain('Changes that you did not save are lost')
    expect(dialog()!.querySelectorAll('ol li')).toHaveLength(3)
  })

  it('opens the login page in a new tab and stays open', async () => {
    const openTab = vi.spyOn(window, 'open').mockImplementation(() => null)
    await open()

    button('Log in in a new tab').click()
    await nextTick()

    expect(openTab).toHaveBeenCalledWith('/opennms/login.jsp', '_blank', 'noopener')
    expect(sessionExpiredDialogVisible.value).toBe(true)
  })

  it('closes on Continue and checks the login with a request', async () => {
    vi.mocked(rest.get).mockResolvedValue({ status: 200, data: {}})
    await open()

    button('Continue').click()
    await nextTick()

    expect(sessionExpiredDialogVisible.value).toBe(false)
    expect(rest.get).toHaveBeenCalledWith('/info')
  })

  it('reloads the page on Reload and log in', async () => {
    await open()

    button('Reload and log in').click()

    expect(reload).toHaveBeenCalledTimes(1)
  })
})
