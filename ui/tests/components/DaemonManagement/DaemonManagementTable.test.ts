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

import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import DaemonManagementTable from '@/components/DaemonManagement/DaemonManagementTable.vue'
import { RELOADABLE_DAEMONS, reloadDaemon } from '@/services/daemonService'

const showToast = vi.fn()
vi.mock('@opennms/onms-ui', async (importOriginal) => {
  const original = await importOriginal<typeof import('@opennms/onms-ui')>()
  return { ...original, useOnmsToast: () => ({ showToast }) }
})

vi.mock('@/services/daemonService', async (importOriginal) => {
  const original = await importOriginal<typeof import('@/services/daemonService')>()
  return { ...original, reloadDaemon: vi.fn() }
})

// expose the content slot and ok/cancel controls without the modal machinery
const ConfirmationDialogStub = {
  props: ['visible', 'title', 'actionButtonText'],
  template: `<div v-if="visible" class="confirm-stub">
    <slot name="content"></slot>
    <button class="ok-btn" @click="$emit('ok')">{{ actionButtonText }}</button>
    <button class="cancel-btn" @click="$emit('cancel')">Cancel</button>
  </div>`
}

const mountTable = async () => {
  const wrapper = mount(DaemonManagementTable, {
    global: {
      plugins: [PrimeVue],
      stubs: {
        AboutDialogButton: true,
        DaemonManagementAbout: true,
        OnmsConfirmationDialog: ConfirmationDialogStub
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('DaemonManagementTable.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('lists every reloadable daemon with a reload action', async () => {
    const wrapper = await mountTable()
    const table = wrapper.find('[data-test="daemons-table"]')
    for (const daemon of RELOADABLE_DAEMONS) {
      expect(table.text()).toContain(daemon.label)
    }
    expect(wrapper.findAll('[data-test^="reload-"]')).toHaveLength(RELOADABLE_DAEMONS.length)
  })

  it('asks for confirmation and only reloads after OK', async () => {
    vi.mocked(reloadDaemon).mockResolvedValue()
    const wrapper = await mountTable()

    await wrapper.find('[data-test="reload-Pollerd"]').trigger('click')
    expect(reloadDaemon).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="reload-confirm-text"]').text()).toContain('Pollerd')

    await wrapper.find('.ok-btn').trigger('click')
    await flushPromises()

    expect(reloadDaemon).toHaveBeenCalledWith('Pollerd')
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({
      severity: 'success',
      message: expect.stringContaining('Reload requested for Pollerd')
    }))
  })

  it('does not reload when the confirmation is cancelled', async () => {
    const wrapper = await mountTable()

    await wrapper.find('[data-test="reload-Vacuumd"]').trigger('click')
    await wrapper.find('.cancel-btn').trigger('click')
    await flushPromises()

    expect(reloadDaemon).not.toHaveBeenCalled()
    expect(wrapper.find('.confirm-stub').exists()).toBe(false)
  })

  it('surfaces the server validation message on a failed request', async () => {
    // POST /rest/events answers 400 with the reason as a plain-text body
    vi.mocked(reloadDaemon).mockRejectedValue({
      response: { status: 400, data: 'Failed to marshal/unmarshal XML file' }
    })
    const wrapper = await mountTable()

    await wrapper.find('[data-test="reload-Trapd"]').trigger('click')
    await wrapper.find('.ok-btn').trigger('click')
    await flushPromises()

    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({
      severity: 'error',
      message: expect.stringContaining('Failed to marshal/unmarshal XML file')
    }))
  })

  it('still reports a failure without a server message', async () => {
    vi.mocked(reloadDaemon).mockRejectedValue(new Error('network'))
    const wrapper = await mountTable()

    await wrapper.find('[data-test="reload-Eventd"]').trigger('click')
    await wrapper.find('.ok-btn').trigger('click')
    await flushPromises()

    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({
      severity: 'error',
      message: expect.stringContaining('Failed to request a reload for Eventd.')
    }))
  })
})
