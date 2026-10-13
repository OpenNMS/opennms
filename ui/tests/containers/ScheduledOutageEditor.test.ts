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
import { getNodeLabels, getOutageApplicability, getScheduledOutage, saveScheduledOutage, scheduledOutageExists, setNotificationMembership, setPackageMembership } from '@/services/scheduledOutagesService'

const push = vi.fn()
const replace = vi.fn()
let query: Record<string, string> = {}
vi.mock('vue-router', () => ({
  useRouter: () => ({ push, replace }),
  useRoute: () => ({ query, fullPath: '/scheduled-outages/edit' })
}))

vi.mock('@/services/scheduledOutagesService', async importOriginal => ({
  outageNameProblem: (await importOriginal<typeof import('@/services/scheduledOutagesService')>()).outageNameProblem,
  scheduledOutageExists: vi.fn(),
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
    vi.resetAllMocks()
    query = { name: 'nightly' }
    vi.mocked(getOutageApplicability).mockResolvedValue(EMPTY_APPLIES as any)
    vi.mocked(getNodeLabels).mockResolvedValue({})
    vi.mocked(scheduledOutageExists).mockResolvedValue(false)
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

  it('passes the loaded nodes, interfaces and spans to the pickers', async () => {
    // the reported "fields not populated" symptom: an opened outage must hand
    // its saved selection and spans to the components that list them
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'weekly', time: [{ day: 'monday', begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }], interface: [{ address: '10.0.0.1' }]
    } as any)
    vi.mocked(getNodeLabels).mockResolvedValue({ 5: 'core-router' })
    const wrapper = await mountPage()

    const [nodePicker, ifacePicker] = wrapper.findAllComponents({ name: 'NodeInterfacePicker' })
    expect(nodePicker.props('items')).toEqual([{ id: 5 }])
    expect(nodePicker.props('nodeLabels')).toEqual({ 5: 'core-router' })
    expect(ifacePicker.props('items')).toEqual([{ address: '10.0.0.1' }])
    const spans = wrapper.findComponent({ name: 'TimeSpanEditor' })
    expect(spans.props('type')).toBe('weekly')
    expect(spans.props('times')).toEqual([{ day: 'monday', begins: '01:00:00', ends: '02:00:00' }])
    expect(getNodeLabels).toHaveBeenCalledWith([5])
  })

  it('offers the form help in an About dialog next to the title instead of an inline panel', async () => {
    vi.mocked(getScheduledOutage).mockResolvedValue({ name: 'nightly', type: 'daily', time: [], node: [], interface: [] } as any)
    const wrapper = await mountPage()

    expect(wrapper.find('.title-row [data-test="about-button"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="editor-help"]').exists()).toBe(false)
  })

  it('saves the edited outage and returns to the list', async () => {
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }], interface: []
    } as any)
    vi.mocked(saveScheduledOutage).mockResolvedValue(undefined as any)
    const wrapper = await mountPage()

    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(saveScheduledOutage).toHaveBeenCalledWith({
      name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }], interface: []
    })
    expect(push).toHaveBeenCalledWith({ path: '/scheduled-outages' })
  })

  it('refuses to save without a selection or a time span', async () => {
    query = { name: 'brand-new', new: 'true' }
    const wrapper = await mountPage()

    await wrapper.find('[data-test="save"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="selection-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="time-error"]').exists()).toBe(true)
    expect(saveScheduledOutage).not.toHaveBeenCalled()
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
    expect(scheduledOutageExists).toHaveBeenCalledWith('brand-new')
    expect(wrapper.find('[data-test="save"]').exists()).toBe(true)
  })

  it('opens an existing outage for editing instead of creating over it', async () => {
    // browser Back after Create lands on new=true for the outage just saved
    query = { name: 'nightly', new: 'true' }
    vi.mocked(scheduledOutageExists).mockResolvedValue(true)
    vi.mocked(getScheduledOutage).mockResolvedValue({ name: 'nightly', type: 'daily', time: [], node: [{ id: 5 }], interface: [] } as any)
    const wrapper = await mountPage()

    expect(getScheduledOutage).toHaveBeenCalledWith('nightly')
    expect(replace).toHaveBeenCalledWith({ path: '/scheduled-outages/edit', query: { name: 'nightly' }})
    expect(wrapper.find('[data-test="editor-existing-note"]').text()).toContain('already exists')
    expect(wrapper.find('[data-test="editor-title"]').text()).toContain('Edit Scheduled Outage')
    expect(wrapper.find('[data-test="save"]').text()).toBe('Save')
    expect(getOutageApplicability).toHaveBeenCalledWith('nightly')
  })

  it('blocks creating when it cannot tell whether the name exists', async () => {
    query = { name: 'nightly', new: 'true' }
    vi.mocked(scheduledOutageExists).mockResolvedValue(null)
    const wrapper = await mountPage()

    expect(wrapper.find('[data-test="editor-load-failed"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('Creating is disabled')
    expect(wrapper.find('[data-test="save"]').exists()).toBe(false)
  })

  it('blocks a reserved name from a hand-typed URL', async () => {
    query = { name: 'applies-to' }
    const wrapper = await mountPage()

    expect(getScheduledOutage).not.toHaveBeenCalled()
    expect(wrapper.find('[data-test="editor-error"]').text()).toContain('is reserved')
    expect(wrapper.find('[data-test="save"]').exists()).toBe(false)
  })

  it('redirects to the list when no name is given', async () => {
    query = {}
    await mountPage()
    expect(replace).toHaveBeenCalledWith({ path: '/scheduled-outages' })
  })

  describe('editing actions', () => {
    const LOADED = {
      name: 'nightly', type: 'daily', time: [{ begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }, { id: 6 }], interface: [{ address: '10.0.0.1' }]
    }
    const APPLIES = {
      notifications: false,
      notificationCalendars: [],
      pollers: [{ name: 'example1', applied: true, calendars: ['nightly'] }, { name: 'strafer', applied: false, calendars: [] }],
      thresholders: [{ name: 'mib2', applied: false, calendars: [] }],
      collectors: [{ name: 'vmware6', applied: false, calendars: [] }]
    }

    beforeEach(() => {
      vi.mocked(getScheduledOutage).mockResolvedValue(structuredClone(LOADED) as any)
      vi.mocked(getOutageApplicability).mockResolvedValue(structuredClone(APPLIES) as any)
    })

    it('removes nodes and interfaces by index', async () => {
      const wrapper = await mountPage()
      const [nodePicker, ifacePicker] = wrapper.findAllComponents({ name: 'NodeInterfacePicker' })
      nodePicker.vm.$emit('remove', 0)
      ifacePicker.vm.$emit('remove', 0)
      await flushPromises()
      expect(nodePicker.props('items')).toEqual([{ id: 6 }])
      expect(ifacePicker.props('items')).toEqual([])
    })

    it('adds an interface once, clearing match-any, and ignores a duplicate node', async () => {
      const wrapper = await mountPage()
      const [nodePicker, ifacePicker] = wrapper.findAllComponents({ name: 'NodeInterfacePicker' })
      ifacePicker.vm.$emit('add', { address: '10.0.0.2' })
      ifacePicker.vm.$emit('add', { address: '10.0.0.2' })
      nodePicker.vm.$emit('add', { id: 5 }, 'core-router')
      await flushPromises()
      expect(ifacePicker.props('items')).toEqual([{ address: '10.0.0.1' }, { address: '10.0.0.2' }])
      expect(nodePicker.props('items')).toEqual([{ id: 5 }, { id: 6 }])
    })

    it('asks before replacing a curated selection with all nodes and interfaces', async () => {
      const wrapper = await mountPage()
      const vm = wrapper.vm as any
      await wrapper.find('[data-test="match-any"]').trigger('click')
      expect(vm.showSelectAllConfirm).toBe(true)
      wrapper.findAllComponents({ name: 'OnmsConfirmationDialog' })[0].vm.$emit('ok')
      await flushPromises()
      const [nodePicker, ifacePicker] = wrapper.findAllComponents({ name: 'NodeInterfacePicker' })
      expect(vm.showSelectAllConfirm).toBe(false)
      expect(nodePicker.props('items')).toEqual([])
      expect(ifacePicker.props('items')).toEqual([{ address: 'match-any' }])
      expect(ifacePicker.props('matchAny')).toBe(true)
    })

    it('applies all nodes and interfaces at once when nothing specific is selected', async () => {
      query = { name: 'brand-new', new: 'true' }
      const wrapper = await mountPage()
      await wrapper.find('[data-test="match-any"]').trigger('click')
      await flushPromises()
      expect((wrapper.vm as any).showSelectAllConfirm).toBe(false)
      expect(wrapper.findAllComponents({ name: 'NodeInterfacePicker' })[1].props('items')).toEqual([{ address: 'match-any' }])
    })

    it('confirms a type change before discarding spans, and restores the type on cancel', async () => {
      const wrapper = await mountPage()
      const vm = wrapper.vm as any
      vm.outage.type = 'weekly'
      vm.onTypeChange()
      expect(vm.showTypeChangeConfirm).toBe(true)
      vm.cancelTypeChange()
      expect(vm.outage.type).toBe('daily')
      expect(vm.outage.time).toHaveLength(1)

      vm.outage.type = 'weekly'
      vm.onTypeChange()
      vm.confirmTypeChange()
      await flushPromises()
      expect(vm.showTypeChangeConfirm).toBe(false)
      expect(vm.outage.time).toEqual([])
      // with no spans left a further change needs no confirmation
      vm.outage.type = 'monthly'
      vm.onTypeChange()
      expect(vm.showTypeChangeConfirm).toBe(false)
    })

    it('adds a time span once and notes a duplicate', async () => {
      const wrapper = await mountPage()
      const spans = wrapper.findComponent({ name: 'TimeSpanEditor' })
      spans.vm.$emit('add', { begins: '01:00:00', ends: '02:00:00' })
      await flushPromises()
      expect(wrapper.find('[data-test="time-note"]').text()).toContain('already in the list')
      spans.vm.$emit('add', { begins: '03:00:00', ends: '04:00:00' })
      await flushPromises()
      expect(wrapper.find('[data-test="time-note"]').exists()).toBe(false)
      expect(spans.props('times')).toHaveLength(2)
      spans.vm.$emit('remove', 0)
      await flushPromises()
      expect(spans.props('times')).toEqual([{ begins: '03:00:00', ends: '04:00:00' }])
    })

    it('only sends the applies-to memberships that changed', async () => {
      vi.mocked(saveScheduledOutage).mockResolvedValue(undefined as any)
      const wrapper = await mountPage()
      const matrix = wrapper.findComponent({ name: 'AppliesToMatrix' })
      matrix.vm.$emit('togglePackage', 'pollerd', 'example1', false)
      matrix.vm.$emit('setAll', 'threshd', true)
      matrix.vm.$emit('togglePackage', 'collectd', 'missing', true)
      matrix.vm.$emit('update:notifications', true)
      await flushPromises()

      await wrapper.find('[data-test="save"]').trigger('click')
      await flushPromises()
      expect(vi.mocked(setPackageMembership).mock.calls).toEqual([
        ['pollerd', 'nightly', 'example1', false],
        ['threshd', 'nightly', 'mib2', true]
      ])
      expect(setNotificationMembership).toHaveBeenCalledWith('nightly', true)
      expect(push).toHaveBeenCalledWith({ path: '/scheduled-outages' })
    })

    it('keeps the form open with an error when the save fails', async () => {
      vi.mocked(saveScheduledOutage).mockRejectedValueOnce(new Error('500'))
      const wrapper = await mountPage()
      await wrapper.find('[data-test="save"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="editor-error"]').text()).toContain('Failed to save the scheduled outage.')
      expect(push).not.toHaveBeenCalled()
      expect((wrapper.vm as any).saving).toBe(false)
    })

    it('re-reads applies-to when a membership call fails after the outage saved', async () => {
      vi.mocked(saveScheduledOutage).mockResolvedValue(undefined as any)
      vi.mocked(setPackageMembership).mockRejectedValueOnce(new Error('500'))
      const wrapper = await mountPage()
      wrapper.findComponent({ name: 'AppliesToMatrix' }).vm.$emit('togglePackage', 'pollerd', 'strafer', true)
      await flushPromises()

      await wrapper.find('[data-test="save"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="editor-error"]').text()).toContain('The outage was saved')
      expect(getOutageApplicability).toHaveBeenLastCalledWith('nightly')
      expect(push).not.toHaveBeenCalled()
    })

    it('asks for an outage type before saving', async () => {
      const wrapper = await mountPage()
      ;(wrapper.vm as any).outage.type = undefined
      await wrapper.find('[data-test="save"]').trigger('click')
      await flushPromises()
      expect(wrapper.find('[data-test="editor-error"]').text()).toContain('choose an outage type')
      expect(saveScheduledOutage).not.toHaveBeenCalled()
    })

    it('goes back to the list from the back button and Cancel', async () => {
      const wrapper = await mountPage()
      await wrapper.find('[data-test="back-button"]').trigger('click')
      await wrapper.find('[data-test="cancel-bottom"]').trigger('click')
      expect(push).toHaveBeenCalledTimes(2)
      expect(push).toHaveBeenCalledWith({ path: '/scheduled-outages' })
    })
  })

  it('renders the saved nodes, interfaces and spans on the page itself', async () => {
    // unstubbed: the reported symptom was an opened outage that looked empty
    vi.mocked(getScheduledOutage).mockResolvedValue({
      name: 'nightly', type: 'weekly', time: [{ day: 'monday', begins: '01:00:00', ends: '02:00:00' }], node: [{ id: 5 }], interface: [{ address: '10.0.0.1' }]
    } as any)
    vi.mocked(getNodeLabels).mockResolvedValue({ 5: 'core-router' })
    const wrapper = mount(ScheduledOutageEditor, { global: { plugins: [PrimeVue], stubs: { BreadCrumbs: true, AppliesToMatrix: true }}})
    await flushPromises()

    expect(wrapper.find('[data-test="picker-node-item"]').text()).toContain('core-router (id 5)')
    expect(wrapper.find('[data-test="picker-interface-item"]').text()).toContain('10.0.0.1')
    expect(wrapper.find('[data-test="time-row"]').text()).toContain('Monday 01:00:00')
    expect(wrapper.find('[data-test="time-empty"]').exists()).toBe(false)
  })
})
