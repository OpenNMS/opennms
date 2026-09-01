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

import ScheduledOutages from '@/containers/ScheduledOutages.vue'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import {
  getNodeLabels,
  getOutageApplicability,
  getScheduledOutages
} from '@/services/scheduledOutagesService'

const push = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({ push })
}))

vi.mock('@/services/scheduledOutagesService', () => ({
  getScheduledOutages: vi.fn(),
  getOutageApplicability: vi.fn(),
  getNodeLabels: vi.fn(),
  deleteScheduledOutage: vi.fn(),
  scheduledOutageErrorMessage: (_err: any, fallback: string) => fallback
}))

const OUTAGES = [
  { name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }], interface: [{ address: '10.0.0.1' }] },
  { name: 'everything', type: 'weekly', time: [], node: [], interface: [{ address: 'match-any' }] }
]

const APPLIES = {
  notifications: false,
  notificationCalendars: ['nightly'],
  pollers: [{ name: 'example1', applied: false, calendars: ['nightly'] }],
  thresholders: [{ name: 'mib2', applied: false, calendars: [] }],
  collectors: [{ name: 'vmware6', applied: false, calendars: ['everything'] }]
}

const mountPage = async () => {
  const wrapper = mount(ScheduledOutages, {
    global: {
      plugins: [PrimeVue],
      stubs: {
        BreadCrumbs: true,
        AboutDialogButton: true,
        ScheduledOutagesAbout: true,
        // PrimeVue Dialog teleports to body, out of the wrapper's reach
        OnmsDialog: {
          props: ['visible', 'header'],
          template: '<div v-if="visible" class="dialog-stub"><slot /></div>'
        }
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('ScheduledOutages.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    vi.mocked(getScheduledOutages).mockResolvedValue(OUTAGES as any)
    vi.mocked(getOutageApplicability).mockResolvedValue(APPLIES as any)
    vi.mocked(getNodeLabels).mockResolvedValue({ 5: 'core-router' })
  })

  it('derives every membership from a single name-less applies-to call', async () => {
    const wrapper = await mountPage()

    expect(getOutageApplicability).toHaveBeenCalledTimes(1)
    expect(getOutageApplicability).toHaveBeenCalledWith()

    const marks = wrapper.findAll('[data-test="applied-mark"]').map(m => m.attributes('aria-label'))
    // nightly: notifications + polling applied; everything: collection applied
    expect(marks.filter(m => m === 'applied').length).toBe(3)
  })

  it('shows node labels and interface addresses in the selection column', async () => {
    const wrapper = await mountPage()
    const text = wrapper.find('[data-test="outages-table"]').text()
    expect(text).toContain('core-router')
    expect(text).toContain('10.0.0.1')
    expect(text).toContain('All nodes and interfaces')
  })

  it('reports a first-load failure without claiming a last known list', async () => {
    vi.mocked(getScheduledOutages).mockResolvedValue(null)
    const wrapper = await mountPage()
    expect(wrapper.find('[data-test="load-error"]').text()).toBe('Failed to load scheduled outages.')
    expect(wrapper.find('[data-test="outages-table"]').exists()).toBe(false)
  })

  it('creates through the dialog and routes to the editor', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="create-outage"]').trigger('click')
    await wrapper.find('[data-test="new-name"]').setValue('maintenance')
    await wrapper.find('[data-test="create-confirm"]').trigger('click')

    expect(push).toHaveBeenCalledWith({ path: '/scheduled-outages/edit', query: { name: 'maintenance', new: 'true' }})
  })

  it('rejects a duplicate name in the create dialog', async () => {
    const wrapper = await mountPage()
    await wrapper.find('[data-test="create-outage"]').trigger('click')
    await wrapper.find('[data-test="new-name"]').setValue('nightly')
    await wrapper.find('[data-test="create-confirm"]').trigger('click')

    expect(push).not.toHaveBeenCalled()
    expect(wrapper.text()).toContain('already exists')
  })

  it('routes edit to the editor with the outage name', async () => {
    const wrapper = await mountPage()
    await wrapper.findAll('[data-test="edit-outage"]')[0].trigger('click')
    expect(push).toHaveBeenCalledWith({ path: '/scheduled-outages/edit', query: { name: 'nightly' }})
  })
})
