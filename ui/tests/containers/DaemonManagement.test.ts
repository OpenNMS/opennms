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

import DaemonManagement from '@/containers/DaemonManagement.vue'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { reloadDaemon } from '@/services/daemonService'

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

vi.mock('@/services/daemonService', async (importOriginal) => {
  const original = await importOriginal<typeof import('@/services/daemonService')>()
  return { ...original, reloadDaemon: vi.fn() }
})

const mountPage = async () => {
  const wrapper = mount(DaemonManagement, {
    global: {
      plugins: [PrimeVue],
      stubs: { BreadCrumbs: true, AboutDialogButton: true, DaemonManagementAbout: true }
    }
  })
  await flushPromises()
  return wrapper
}

describe('DaemonManagement.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists every reloadable daemon with a reload action', async () => {
    const wrapper = await mountPage()
    const table = wrapper.find('[data-test="daemons-table"]')
    for (const name of ['Alarmd', 'Collectd', 'Eventd', 'Notifd', 'Pollerd', 'Syslogd', 'Telemetryd', 'Trapd']) {
      expect(table.text()).toContain(name)
    }
    expect(wrapper.findAll('[data-test^="reload-"]')).toHaveLength(8)
  })

  it('requests the reload with the exact wire name and confirms via snackbar', async () => {
    vi.mocked(reloadDaemon).mockResolvedValue()
    const wrapper = await mountPage()

    await wrapper.find('[data-test="reload-Pollerd"]').trigger('click')
    await flushPromises()

    expect(reloadDaemon).toHaveBeenCalledWith('Pollerd')
    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringContaining('Reload requested for Pollerd') }))
  })

  it('surfaces a failed reload request as an error snackbar', async () => {
    vi.mocked(reloadDaemon).mockRejectedValue(new Error('boom'))
    const wrapper = await mountPage()

    await wrapper.find('[data-test="reload-trapd"]').trigger('click')
    await flushPromises()

    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
  })
})
