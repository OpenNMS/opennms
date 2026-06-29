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

import EventsTable from '@/components/Nodes/EventsTable.vue'
import { useEventStore } from '@/stores/eventStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'

const mockNodeId = '42'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: mockNodeId }})
}))

const mockEvent = {
  id: 101,
  createTime: 1700000000000,
  severity: 'Major',
  logMessage: '<p>A test log message</p>',
  nodeId: 42,
  label: 'Test Event',
  location: 'Default',
  log: 'Y',
  description: '',
  display: 'Y',
  nodeLabel: 'node42',
  parameters: [],
  source: 'TestSource',
  time: 1700000000000,
  uei: 'uei.opennms.org/test'
}

describe('EventsTable.vue', () => {
  let wrapper: VueWrapper<any>
  let eventStore: ReturnType<typeof useEventStore>

  const mountTable = () =>
    mount(EventsTable, {
      global: {
        plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
        directives: {
          date: {
            mounted(_el: Element) {
              // no-op stub for v-date in tests
            }
          }
        },
        stubs: {
          RouterLink: {
            name: 'RouterLink',
            props: ['to'],
            template: '<a :href="to"><slot /></a>'
          }
        }
      }
    })

  beforeEach(async () => {
    vi.clearAllMocks()
    wrapper = mountTable()
    eventStore = useEventStore()
    eventStore.getEvents = vi.fn().mockResolvedValue(undefined)
    eventStore.events = []
    eventStore.totalCount = 0
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the DataTable with data-test attribute', () => {
      expect(wrapper.find('[data-test="events-table"]').exists()).toBe(true)
    })

    it('renders all 4 column headers', () => {
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('Id')
      expect(headerTexts).toContain('Created')
      expect(headerTexts).toContain('Severity')
      expect(headerTexts).toContain('Message')
    })

    it('renders a row for each event', async () => {
      eventStore.events = [mockEvent] as any
      eventStore.totalCount = 1
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
    })

    it('Id cell renders a router-link to /event/{id}', async () => {
      eventStore.events = [mockEvent] as any
      eventStore.totalCount = 1
      await nextTick()

      const link = wrapper.find('tbody tr td a')
      expect(link.exists()).toBe(true)
      expect(link.attributes('href')).toContain(`/event/${mockEvent.id}`)
      expect(link.text()).toBe(String(mockEvent.id))
    })

    it('Severity cell renders a PrimeVue Tag', async () => {
      eventStore.events = [mockEvent] as any
      eventStore.totalCount = 1
      await nextTick()

      // PrimeVue Tag renders with class p-tag
      expect(wrapper.find('.p-tag').exists()).toBe(true)
      expect(wrapper.find('.p-tag').text()).toContain('Major')
    })

    it('Message cell renders html via v-html', async () => {
      eventStore.events = [mockEvent] as any
      eventStore.totalCount = 1
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows[0].html()).toContain('<p>A test log message</p>')
    })
  })

  describe('Empty state', () => {
    it('shows EmptyList when there are no rows', async () => {
      eventStore.events = []
      eventStore.totalCount = 0
      await nextTick()

      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('Lazy pagination — onPage', () => {
    it('calls getEvents with offset and _s node filter when page changes', async () => {
      await wrapper.vm.onPage({ first: 5, rows: 5, page: 1, pageCount: 2 })
      await flushPromises()

      expect(eventStore.getEvents).toHaveBeenCalledWith(
        expect.objectContaining({
          offset: 5,
          limit: 5,
          _s: `node.id==${mockNodeId}`
        })
      )
    })

    it('preserves the node.id _s filter after page change', async () => {
      await wrapper.vm.onPage({ first: 10, rows: 10, page: 2, pageCount: 3 })
      await flushPromises()

      expect(eventStore.getEvents).toHaveBeenCalledWith(
        expect.objectContaining({
          _s: `node.id==${mockNodeId}`
        })
      )
    })
  })
})
