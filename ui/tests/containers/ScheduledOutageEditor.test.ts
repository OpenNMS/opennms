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

import ScheduledOutageEditor from '@/containers/ScheduledOutageEditor.vue'
import { flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { getOutageApplicability, getScheduledOutage, saveScheduledOutage, setNotificationMembership, setPackageMembership } from '@/services/scheduledOutagesService'

const push = vi.fn()
const replace = vi.fn()
let query: Record<string, string> = {}
vi.mock('vue-router', () => ({
  useRouter: () => ({ push, replace }),
  useRoute: () => ({ query, fullPath: '/scheduled-outages/edit' })
}))

vi.mock('@/services/scheduledOutagesService', () => ({
  getScheduledOutage: vi.fn(),
  getOutageApplicability: vi.fn(),
  getNodeLabels: vi.fn().mockResolvedValue({}),
  saveScheduledOutage: vi.fn(),
  setPackageMembership: vi.fn(),
  setNotificationMembership: vi.fn(),
  scheduledOutageErrorMessage: (_err: any, fallback: string) => fallback
}))

const EMPTY_APPLIES = { notifications: false, notificationCalendars: [], pollers: [], thresholders: [], collectors: [] }

const mountPage = async () => {
  const wrapper = mount(ScheduledOutageEditor, {
    global: {
      plugins: [PrimeVue],
      stubs: {
        BreadCrumbs: true,
        NodeInterfacePicker: true,
        TimeSpanEditor: true,
        AppliesToMatrix: true
      }
    }
  })
  await flushPromises()
  return wrapper
}

describe('ScheduledOutageEditor.vue', () => {
  beforeEach(() => {
    vi.clearAllMocks()
    query = { name: 'nightly' }
    vi.mocked(getOutageApplicability).mockResolvedValue(EMPTY_APPLIES as any)
  })

  it('blocks editing when an existing outage fails to load', async () => {
    // a transient read failure must not present an empty form whose Save would
    // whole-object-replace (and so wipe) the real outage
    vi.mocked(getScheduledOutage).mockResolvedValue(null)
    const wrapper = await mountPage()

    expect(wrapper.find('[data-test="editor-load-failed"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('Editing is disabled')
    expect(wrapper.find('[data-test="save"]').exists()).toBe(false)
    expect(saveScheduledOutage).not.toHaveBeenCalled()
  })

  it('renders the form when the outage loads', async () => {
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [], interface: [{ address: 'match-any' }]
    } as any)
    const wrapper = await mountPage()

    expect(wrapper.find('[data-test="editor-load-failed"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="save"]').exists()).toBe(true)
  })

  it('adding a node ends "all nodes and interfaces"', async () => {
    // with match-any active the added node would be hidden by the pickers and
    // still be saved next to match-any; the legacy editor stripped it
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'daily', time: [], node: [], interface: [{ address: 'match-any' }]
    } as any)
    const wrapper = await mountPage()
    const [nodePicker, ifacePicker] = wrapper.findAllComponents({ name: 'NodeInterfacePicker' })
    expect(ifacePicker.props('matchAny')).toBe(true)

    nodePicker.vm.$emit('add', { id: 7 }, 'core-router')
    await flushPromises()

    expect(nodePicker.props('items')).toEqual([{ id: 7 }])
    expect(ifacePicker.props('items')).toEqual([])
    expect(ifacePicker.props('matchAny')).toBe(false)
  })

  it('reports an applies-to read failure instead of an empty matrix, and saves without touching memberships', async () => {
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [], interface: [{ address: 'match-any' }]
    } as any)
    vi.mocked(getOutageApplicability).mockResolvedValue(null)
    vi.mocked(saveScheduledOutage).mockResolvedValue(undefined as any)
    const wrapper = await mountPage()

    expect(wrapper.find('[data-test="applies-error"]').exists()).toBe(true)
    expect(wrapper.findComponent({ name: 'AppliesToMatrix' }).exists()).toBe(false)
    // the outage itself is still editable
    expect(wrapper.find('[data-test="save"]').exists()).toBe(true)

    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(saveScheduledOutage).toHaveBeenCalled()
    expect(setPackageMembership).not.toHaveBeenCalled()
    expect(setNotificationMembership).not.toHaveBeenCalled()
  })

  it('treats new=true as a blank form without a load guard', async () => {
    query = { name: 'brand-new', new: 'true' }
    const wrapper = await mountPage()

    expect(getScheduledOutage).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="save"]').exists()).toBe(true)
  })

  it('redirects to the list when no name is given', async () => {
    query = {}
    await mountPage()
    expect(replace).toHaveBeenCalledWith({ path: '/scheduled-outages' })
  })
})
