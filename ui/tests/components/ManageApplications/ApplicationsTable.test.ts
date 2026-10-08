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

import ApplicationsTable from '@/components/ManageApplications/ApplicationsTable.vue'
import { useApplicationAdminStore } from '@/stores/applicationAdminStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const { showToast } = vi.hoisted(() => ({ showToast: vi.fn() }))
vi.mock('@opennms/onms-ui', async importOriginal => ({
  ...(await importOriginal<typeof import('@opennms/onms-ui')>()),
  useOnmsToast: () => ({ showToast })
}))

const app = (id: number, name: string, serviceCount = 0, perspectiveLocations: string[] = []) =>
  ({ id, name, serviceCount, perspectiveLocations })

const EditorStub = { name: 'ApplicationEditorDialog', props: ['visible', 'application'], template: '<div data-test="editor-stub" />' }
const DeleteStub = { name: 'ApplicationDeleteDialog', props: ['visible', 'application'], template: '<div data-test="delete-stub" />' }
const CreateStub = { name: 'ApplicationCreateDialog', props: ['visible'], emits: ['update:visible', 'created'], template: '<div data-test="create-stub" />' }

const mountTable = async (applications: any[] = [], state: { loading?: boolean; loadError?: boolean } = {}) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: true })
  const wrapper = mount(ApplicationsTable, {
    global: {
      plugins: [PrimeVue, pinia],
      stubs: {
        ApplicationEditorDialog: EditorStub,
        ApplicationDeleteDialog: DeleteStub,
        ApplicationCreateDialog: CreateStub,
        TableCard: { template: '<div><slot /></div>' }
      }
    }
  })
  const store = useApplicationAdminStore()
  store.applications = applications
  store.loading = state.loading ?? false
  store.loadError = state.loadError ?? false
  await flushPromises()
  return { wrapper, store }
}

const headers = (wrapper: VueWrapper<any>) => wrapper.findAll('th').map(th => th.text())
const rows = (wrapper: VueWrapper<any>) => wrapper.findAll('[data-test="applications-table"] tbody tr')

describe('ApplicationsTable.vue', () => {
  beforeEach(() => showToast.mockClear())

  it('renders the column headers with no applications, and says how to start', async () => {
    const { wrapper } = await mountTable()
    expect(headers(wrapper)).toEqual(['Application', 'Services', 'Perspective locations', 'Actions'])
    expect(wrapper.find('[data-test="empty-list"]').text()).toContain('No applications yet')
    // nothing to search yet
    expect(wrapper.find('[data-test="application-search"]').exists()).toBe(false)
  })

  it('does not flash the empty message while the first load runs', async () => {
    const { wrapper } = await mountTable([], { loading: true })
    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(false)
  })

  it('tells a load failure apart from an empty list', async () => {
    const { wrapper } = await mountTable([], { loadError: true })
    expect(wrapper.find('[data-test="empty-list"]').text()).toContain('Could not load applications')
  })

  it('warns that a failed reload left the rows out of date', async () => {
    const { wrapper } = await mountTable([app(1, 'Web')], { loadError: true })
    expect(wrapper.find('[data-test="reload-error"]').exists()).toBe(true)
  })

  it('lists name, service count and perspective locations, sorted by name', async () => {
    const { wrapper } = await mountTable([app(2, 'Web Store', 3, ['Fulda', 'RDU']), app(1, 'Billing')])
    const [first, second] = rows(wrapper)
    expect(first.find('[data-test="application-name"]').text()).toBe('Billing')
    expect(first.find('[data-test="service-count"]').text()).toBe('0')
    expect(first.find('[data-test="no-perspectives"]').text()).toBe('None')
    expect(second.find('[data-test="application-name"]').text()).toBe('Web Store')
    expect(second.find('[data-test="service-count"]').text()).toBe('3')
    expect(second.findAll('[data-test="perspective-location"]').map(tag => tag.text())).toEqual(['Fulda', 'RDU'])
  })

  it('shows names with markup as text', async () => {
    const { wrapper } = await mountTable([app(1, '<b>x</b>')])
    expect(wrapper.find('[data-test="application-name"]').text()).toBe('<b>x</b>')
    expect(wrapper.find('[data-test="application-name"] b').exists()).toBe(false)
  })

  it('filters on the name and the perspective locations', async () => {
    const { wrapper } = await mountTable([app(1, 'Billing', 0, ['Fulda']), app(2, 'Web Store')])
    await wrapper.find('[data-test="application-search"]').setValue('fulda')
    await flushPromises()
    expect(rows(wrapper).map(row => row.find('[data-test="application-name"]').text())).toEqual(['Billing'])
    await wrapper.find('[data-test="application-search"]').setValue('zzz')
    await flushPromises()
    expect(wrapper.find('[data-test="empty-list"]').text()).toContain('No applications match')
  })

  it('opens the editor from the name or the edit button, and the delete dialog from delete', async () => {
    const { wrapper } = await mountTable([app(1, 'Billing')])
    const editor = wrapper.findComponent(EditorStub)
    await wrapper.find('[data-test="application-name"]').trigger('click')
    expect(editor.props('visible')).toBe(true)
    expect(editor.props('application')).toMatchObject({ id: 1 })

    editor.vm.$emit('update:visible', false)
    await flushPromises()
    expect(editor.props('visible')).toBe(false)
    await wrapper.find('[data-test="edit-application-button"]').trigger('click')
    expect(editor.props('visible')).toBe(true)

    await wrapper.find('[data-test="delete-application-button"]').trigger('click')
    const remove = wrapper.findComponent(DeleteStub)
    expect(remove.props('visible')).toBe(true)
    expect(remove.props('application')).toMatchObject({ id: 1 })
  })

  it('opens the create dialog, then the editor for the new application', async () => {
    const { wrapper, store } = await mountTable([app(1, 'Billing')])
    await wrapper.find('[data-test="add-application-button"]').trigger('click')
    const create = wrapper.findComponent(CreateStub)
    expect(create.props('visible')).toBe(true)

    store.applications = [app(1, 'Billing'), app(2, 'Web Store')]
    create.vm.$emit('created', 'Web Store', 2)
    await flushPromises()
    const editor = wrapper.findComponent(EditorStub)
    expect(editor.props('visible')).toBe(true)
    expect(editor.props('application')).toMatchObject({ id: 2 })
  })

  it('says so when the new application is not in the reloaded list', async () => {
    const { wrapper } = await mountTable([app(1, 'Billing')])
    wrapper.findComponent(CreateStub).vm.$emit('created', 'Web Store', 2)
    await flushPromises()
    expect(wrapper.findComponent(EditorStub).props('visible')).toBe(false)
    expect(showToast).toHaveBeenCalledWith(expect.objectContaining({ severity: 'warn' }))
  })
})
