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

import ApplicationEditorDialog from '@/components/ManageApplications/ApplicationEditorDialog.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { useMonitoringLocationAdminStore } from '@/stores/monitoringLocationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const DialogStub = {
  name: 'Dialog',
  props: ['visible', 'header', 'modal'],
  template: '<div v-if="visible"><slot /><slot name="footer" /></div>'
}

const svc = (id: number, nodeLabel: string, serviceName = 'HTTP') =>
  ({ id, nodeId: id, nodeLabel, ipInterfaceId: id, ipAddress: `10.0.0.${id}`, serviceName })
const loc = (name: string) => ({ 'location-name': name, 'monitoring-area': name } as any)
const web = { id: 4, name: 'Web Store', serviceCount: 1, perspectiveLocations: ['RDU'] }

const mountDialog = async (opts: { members?: any; locations?: any[]; locationsOk?: boolean; candidates?: any } = {}) => {
  const wrapper = mount(ApplicationEditorDialog, {
    props: { visible: false, application: web },
    global: {
      plugins: [PrimeVue, createTestingPinia({ createSpy: vi.fn, stubActions: true })],
      stubs: { Dialog: DialogStub }
    }
  })
  const store = useApplicationAdminStore()
  const locationStore = useMonitoringLocationAdminStore()
  locationStore.locations = opts.locations ?? [loc('RDU'), loc('Fulda'), loc('Default')]
  vi.mocked(locationStore.getLocations).mockResolvedValue(opts.locationsOk ?? true)
  vi.mocked(store.getMembers).mockResolvedValue(opts.members === undefined
    ? { id: 4, name: 'Web Store', services: [svc(1, 'web-01')], perspectiveLocations: ['RDU'] }
    : opts.members)
  vi.mocked(store.searchServices).mockResolvedValue(opts.candidates ?? { totalCount: 2, services: [svc(1, 'web-01'), svc(2, 'db-01', 'PostgreSQL')] })
  vi.mocked(store.updateMembers).mockResolvedValue({ success: true, message: '' })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  return { wrapper, store, locationStore }
}

const saveDisabled = (wrapper: VueWrapper<any>) => wrapper.find('[data-test="save-button"]').attributes('disabled') !== undefined
const memberRows = (wrapper: VueWrapper<any>) => wrapper.findAll('[data-test="members-table"] tbody tr').filter(row => !row.find('[data-test="no-members"]').exists())
const candidateRows = (wrapper: VueWrapper<any>) => wrapper.findAll('[data-test="candidates-table"] tbody tr')
const multiSelect = (wrapper: VueWrapper<any>) => wrapper.findComponent({ name: 'MultiSelect' })

describe('ApplicationEditorDialog.vue', () => {
  beforeEach(() => showToast.mockClear())
  afterEach(() => vi.useRealTimers())

  it('loads the members, the locations and a first page of candidates', async () => {
    const { wrapper, store } = await mountDialog()
    expect(store.getMembers).toHaveBeenCalledWith(4)
    expect(store.searchServices).toHaveBeenCalledWith('', 50)
    expect(wrapper.find('[data-test="members-title"]').text()).toBe('Services (1)')
    expect(memberRows(wrapper)[0].text()).toContain('web-01')
    expect(multiSelect(wrapper).props('modelValue')).toEqual(['RDU'])
    expect(multiSelect(wrapper).props('options')).toEqual(['Default', 'Fulda', 'RDU'])
    expect(wrapper.findAll('[data-test="help-badge"]').length).toBeGreaterThan(0)
    // nothing changed yet
    expect(saveDisabled(wrapper)).toBe(true)
  })

  it('marks a candidate that is already a member, and adds the others', async () => {
    const { wrapper } = await mountDialog()
    const [member, other] = candidateRows(wrapper)
    expect(member.find('[data-test="already-added"]').exists()).toBe(true)
    await other.find('[data-test="add-service-button"]').trigger('click')
    expect(wrapper.find('[data-test="members-title"]').text()).toBe('Services (2)')
    expect(candidateRows(wrapper)[1].find('[data-test="already-added"]').exists()).toBe(true)
    expect(saveDisabled(wrapper)).toBe(false)
    expect(wrapper.find('[data-test="unsaved-note"]').exists()).toBe(true)
  })

  it('saves the full member and location lists', async () => {
    const { wrapper, store } = await mountDialog()
    await candidateRows(wrapper)[1].find('[data-test="add-service-button"]').trigger('click')
    await memberRows(wrapper).find(row => row.text().includes('web-01'))!.find('[data-test="remove-service-button"]').trigger('click')
    multiSelect(wrapper).vm.$emit('update:modelValue', ['RDU', 'Fulda'])
    await flushPromises()
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(store.updateMembers).toHaveBeenCalledWith(web, { serviceIds: [2], perspectiveLocations: ['RDU', 'Fulda'] })
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ severity: 'success' }))
  })

  it('is not dirty after undoing a change', async () => {
    const { wrapper } = await mountDialog()
    multiSelect(wrapper).vm.$emit('update:modelValue', [])
    await flushPromises()
    expect(saveDisabled(wrapper)).toBe(false)
    multiSelect(wrapper).vm.$emit('update:modelValue', ['RDU'])
    await flushPromises()
    expect(saveDisabled(wrapper)).toBe(true)
  })

  it('keeps the dialog open with the server message when the save is rejected', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.updateMembers).mockResolvedValue({ success: false, message: 'Monitoring location Nowhere was not found.' })
    await candidateRows(wrapper)[1].find('[data-test="add-service-button"]').trigger('click')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toBe('Monitoring location Nowhere was not found.')
    expect(wrapper.emitted('update:visible')).toBeUndefined()
  })

  it('names the member service that vanished instead of its id', async () => {
    const { wrapper, store } = await mountDialog()
    vi.mocked(store.updateMembers).mockResolvedValue({ success: false, message: 'Monitored service 2 was not found.' })
    await candidateRows(wrapper)[1].find('[data-test="add-service-button"]').trigger('click')
    await wrapper.find('[data-test="save-button"]').trigger('click')
    await flushPromises()
    expect(wrapper.find('[data-test="dialog-error"]').text()).toBe('db-01 / 10.0.0.2 / PostgreSQL no longer exists. Remove it from the services and save again.')
  })

  it('is not dirty after adding and then removing a service', async () => {
    const { wrapper } = await mountDialog()
    await candidateRows(wrapper)[1].find('[data-test="add-service-button"]').trigger('click')
    expect(saveDisabled(wrapper)).toBe(false)
    await memberRows(wrapper).find(row => row.text().includes('db-01'))!.find('[data-test="remove-service-button"]').trigger('click')
    expect(saveDisabled(wrapper)).toBe(true)
  })

  it('adds every shown candidate that is not a member yet', async () => {
    const { wrapper } = await mountDialog({ candidates: { totalCount: 3, services: [svc(1, 'web-01'), svc(2, 'db-01'), svc(3, 'dns-01')] }})
    const addAll = wrapper.find('[data-test="add-all-button"]')
    expect(addAll.text()).toBe('Add all 2 shown')
    await addAll.trigger('click')
    expect(wrapper.find('[data-test="members-title"]').text()).toBe('Services (3)')
    expect(wrapper.find('[data-test="add-all-button"]').exists()).toBe(false)
  })

  it('links a member node to its page in a new tab', async () => {
    const { wrapper } = await mountDialog()
    const link = wrapper.find('[data-test="member-node-link"]')
    expect(link.attributes('href')).toBe('#/node/1')
    expect(link.attributes('target')).toBe('_blank')
  })

  it('asks before discarding unsaved changes, and closes at once when there are none', async () => {
    const { wrapper } = await mountDialog()
    await wrapper.find('[data-test="cancel-button"]').trigger('click')
    expect(wrapper.emitted('update:visible')?.at(-1)).toEqual([false])

    const dirty = (await mountDialog()).wrapper
    await candidateRows(dirty)[1].find('[data-test="add-service-button"]').trigger('click')
    await dirty.find('[data-test="cancel-button"]').trigger('click')
    expect(dirty.emitted('update:visible')).toBeUndefined()
    expect(dirty.find('[data-test="discard-prompt"]').exists()).toBe(true)
    await dirty.find('[data-test="keep-editing-button"]').trigger('click')
    expect(dirty.find('[data-test="discard-prompt"]').exists()).toBe(false)
    expect(dirty.find('[data-test="members-title"]').text()).toBe('Services (2)')

    // the close icon and Escape arrive as update:visible false
    dirty.findComponent({ name: 'Dialog' }).vm.$emit('update:visible', false)
    await flushPromises()
    expect(dirty.emitted('update:visible')).toBeUndefined()
    await dirty.find('[data-test="discard-button"]').trigger('click')
    expect(dirty.emitted('update:visible')?.at(-1)).toEqual([false])
  })

  it('keeps the newest load when the dialog is closed and reopened before the first one answers', async () => {
    const { wrapper, store } = await mountDialog()
    let answerFirst: (value: any) => void = () => {}
    vi.mocked(store.getMembers)
      .mockImplementationOnce(() => new Promise((resolve) => {
        answerFirst = resolve
      }))
      .mockResolvedValueOnce({ id: 4, name: 'Web Store', services: [svc(1, 'web-01'), svc(2, 'db-01')], perspectiveLocations: [] })
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    await wrapper.setProps({ visible: false })
    await wrapper.setProps({ visible: true })
    await flushPromises()
    answerFirst(null)
    await flushPromises()
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(false)
    expect(wrapper.find('[data-test="members-title"]').text()).toBe('Services (2)')
  })

  it('searches once when it is reopened after a search', async () => {
    const { wrapper, store } = await mountDialog()
    vi.useFakeTimers()
    await wrapper.find('[data-test="candidate-search"]').setValue('web')
    await vi.advanceTimersByTimeAsync(300)
    await wrapper.setProps({ visible: false })
    vi.mocked(store.searchServices).mockClear()
    await wrapper.setProps({ visible: true })
    await vi.advanceTimersByTimeAsync(1000)
    await flushPromises()
    expect(store.searchServices).toHaveBeenCalledTimes(1)
    expect(store.searchServices).toHaveBeenCalledWith('', 50)
  })

  it('says when the application could not be loaded and offers no save', async () => {
    const { wrapper } = await mountDialog({ members: null })
    expect(wrapper.find('[data-test="load-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="members-table"]').exists()).toBe(false)
    expect(saveDisabled(wrapper)).toBe(true)
  })

  it('keeps the current perspective locations selectable when the location list is unavailable', async () => {
    const { wrapper } = await mountDialog({ locations: [], locationsOk: false })
    expect(wrapper.find('[data-test="locations-unavailable"]').exists()).toBe(true)
    expect(multiSelect(wrapper).props('options')).toEqual(['RDU'])
  })

  it('debounces the search and says when the matches were cut off', async () => {
    const { wrapper, store } = await mountDialog()
    vi.useFakeTimers()
    vi.mocked(store.searchServices).mockResolvedValue({ totalCount: 500, services: [svc(9, 'web-09')] })
    await wrapper.find('[data-test="candidate-search"]').setValue('web')
    expect(store.searchServices).toHaveBeenCalledTimes(1)
    await vi.advanceTimersByTimeAsync(300)
    await flushPromises()
    expect(store.searchServices).toHaveBeenLastCalledWith('web', 50)
    expect(wrapper.find('[data-test="candidate-truncated"]').text()).toContain('Showing the first 1 of 500')
  })

  it('ignores a slower, older search that answers last', async () => {
    const { wrapper, store } = await mountDialog()
    vi.useFakeTimers()
    let answerOld: (value: any) => void = () => {}
    vi.mocked(store.searchServices)
      .mockImplementationOnce(() => new Promise((resolve) => {
        answerOld = resolve
      }))
      .mockResolvedValueOnce({ totalCount: 1, services: [svc(5, 'new-one')] })
    await wrapper.find('[data-test="candidate-search"]').setValue('old')
    await vi.advanceTimersByTimeAsync(300)
    await wrapper.find('[data-test="candidate-search"]').setValue('new')
    await vi.advanceTimersByTimeAsync(300)
    await flushPromises()
    answerOld({ totalCount: 1, services: [svc(6, 'old-one')] })
    await flushPromises()
    expect(candidateRows(wrapper).map(row => row.text()).join()).toContain('new-one')
    expect(candidateRows(wrapper).map(row => row.text()).join()).not.toContain('old-one')
  })

  it('shows a search failure', async () => {
    const { wrapper } = await mountDialog({ candidates: null })
    // the default mock resolves a page; replace the first result with a failure
    const store = useApplicationAdminStore()
    vi.useFakeTimers()
    vi.mocked(store.searchServices).mockResolvedValue(null)
    await wrapper.find('[data-test="candidate-search"]').setValue('x')
    await vi.advanceTimersByTimeAsync(300)
    await flushPromises()
    expect(wrapper.find('[data-test="candidate-error"]').exists()).toBe(true)
  })
})
