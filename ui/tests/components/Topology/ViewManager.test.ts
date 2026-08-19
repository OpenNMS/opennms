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

import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import ViewManager from '@/components/Topology/ViewManager.vue'
import { useTopologyStore } from '@/stores/topologyStore'

// The seam's toast takes one message string where PrimeVue took a summary plus
// a detail. These assertions pin the merged wording, so a later edit cannot
// quietly drop the view name out of the message the operator reads.
const showToast = vi.fn()
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast, hideAllToasts: vi.fn() })
}))

const catalog = [{ id: 'v1', name: 'Core switches' }]

const mountManager = () => {
  const wrapper = mount(ViewManager, {
    props: { visible: true },
    global: { plugins: [PrimeVue, createTestingPinia({ stubActions: false })] },
    attachTo: document.body
  })
  const store = useTopologyStore()
  store.catalog = catalog as never
  vi.spyOn(store, 'refreshCatalog').mockResolvedValue(true)
  return { wrapper, store }
}

// happy-dom implements neither window.prompt nor window.confirm, so they are
// stubbed in rather than spied on.
const stubPrompt = (value: string | null) => vi.stubGlobal('prompt', vi.fn().mockReturnValue(value))
const stubConfirm = (value: boolean) => vi.stubGlobal('confirm', vi.fn().mockReturnValue(value))

// The dialog teleports to <body>; buttons are found by their rendered label.
const clickButton = async (label: string) => {
  const button = Array.from(document.querySelectorAll('button'))
    .find(b => b.textContent?.trim() === label)
  expect(button, `no "${label}" button rendered`).toBeTruthy()
  button!.click()
  await flushPromises()
}

describe('ViewManager', () => {
  beforeEach(() => {
    showToast.mockClear()
  })

  afterEach(() => {
    document.body.innerHTML = ''
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('puts the new name in the rename success toast', async () => {
    const { store } = mountManager()
    vi.spyOn(store, 'renameView').mockResolvedValue(true)
    stubPrompt('Edge switches')
    await flushPromises()

    await clickButton('Rename')

    expect(store.renameView).toHaveBeenCalledWith('v1', 'Edge switches')
    expect(showToast).toHaveBeenCalledWith({
      message: 'View renamed to "Edge switches"',
      severity: 'success',
      timeout: 3000
    })
  })

  it('names the view that failed to rename, not the name that was attempted', async () => {
    const { store } = mountManager()
    vi.spyOn(store, 'renameView').mockResolvedValue(false)
    stubPrompt('Edge switches')
    await flushPromises()

    await clickButton('Rename')

    expect(showToast).toHaveBeenCalledWith({
      message: 'Could not rename view "Core switches"',
      severity: 'error',
      timeout: 5000
    })
  })

  it('does not toast when the rename prompt is cancelled', async () => {
    const { store } = mountManager()
    vi.spyOn(store, 'renameView').mockResolvedValue(true)
    stubPrompt(null)
    await flushPromises()

    await clickButton('Rename')

    expect(store.renameView).not.toHaveBeenCalled()
    expect(showToast).not.toHaveBeenCalled()
  })

  it('names the view in the delete toast', async () => {
    const { store } = mountManager()
    vi.spyOn(store, 'removeView').mockResolvedValue(true)
    stubConfirm(true)
    await flushPromises()

    await clickButton('Delete')

    expect(store.removeView).toHaveBeenCalledWith('v1')
    expect(showToast).toHaveBeenCalledWith({
      message: 'View "Core switches" deleted',
      severity: 'success',
      timeout: 3000
    })
  })
})
