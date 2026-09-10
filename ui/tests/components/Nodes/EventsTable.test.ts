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
import NodeDownloadDropdown from '@/components/Nodes/NodeDownloadDropdown.vue'
import { useEventStore } from '@/stores/eventStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { useRoute } from 'vue-router'

const mockNodeId = '42'

// A reactive route, so a test can change the node id the way navigating to another node does.
// The details page keeps one component instance across those changes.
vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '42' }})

  return { useRoute: () => route }
})

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

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

  // The store action must be mocked BEFORE mounting — the component fetches in
  // onMounted, and an unmocked action would fire a real network request.
  const mountTable = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    eventStore = useEventStore(pinia)
    eventStore.getEvents = vi.fn().mockResolvedValue(undefined)

    return mount(EventsTable, {
      global: {
        plugins: [pinia, PrimeVue],
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
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    ;(useRoute() as any).params.id = mockNodeId
    wrapper = mountTable()
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

  describe('Card and title row', () => {
    it('wraps the panel content in a card with the title and both action buttons on one row', () => {
      expect(wrapper.find('.card').exists()).toBe(true)

      const titleRow = wrapper.find('.title-row')
      expect(titleRow.text()).toContain('Recent Events')
      expect(titleRow.find('[data-test="events-for-node-button"]').exists()).toBe(true)
      expect(titleRow.find('[data-test="download-button"]').exists()).toBe(true)
    })
  })

  describe('Events for this Node link', () => {
    it('navigates to the legacy event list filtered to this node', async () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)
      useMenuStore().mainMenu = { baseHref: '/opennms/' } as any
      await nextTick()

      await wrapper.find('[data-test="events-for-node-button"]').trigger('click')

      expect(assign).toHaveBeenCalledWith(`/opennms/event/list?filter=node%3D${mockNodeId}`)
      vi.unstubAllGlobals()
    })
  })

  describe('Download', () => {
    // Drive the real menu wiring rather than the component internals: the dropdown owns the
    // CSV/JSON menu items and calls back into the table.
    const runDownload = async (label: string) => {
      const items = wrapper.findComponent(NodeDownloadDropdown).vm.items as Array<{ label: string, command: () => void }>
      await items.find(i => i.label === label)!.command()
      await flushPromises()
    }

    let blobs: Blob[]
    let downloadNames: string[]

    beforeEach(() => {
      blobs = []
      downloadNames = []

      vi.stubGlobal('URL', {
        createObjectURL: (blob: Blob) => {
          blobs.push(blob)
          return 'blob:fake'
        }
      } as any)

      const realCreateElement = document.createElement.bind(document)
      vi.spyOn(document, 'createElement').mockImplementation(((tag: string, options?: any) => {
        const el = realCreateElement(tag, options)
        if (tag === 'a') {
          Object.defineProperty(el, 'click', { value: () => downloadNames.push((el as HTMLAnchorElement).download) })
        }
        return el
      }) as any)
    })

    it('requests the default page size for the node, leaving the visible page untouched', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([mockEvent])
      eventStore.events = [mockEvent] as any
      eventStore.totalCount = 1

      await runDownload('Download CSV...')

      expect(eventStore.getEventsForExport).toHaveBeenCalledWith({
        limit: 5,
        offset: 0,
        _s: `node.id==${mockNodeId}`
      })
      expect(eventStore.events).toEqual([mockEvent])
      expect(eventStore.totalCount).toBe(1)
    })

    // The download is the page the paginator is showing, so raising rows-per-page is how a user
    // downloads more; the whole set comes from the Events page instead.
    it('follows the paginator when the user changes page or rows-per-page', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([mockEvent])

      await wrapper.vm.onPage({ first: 20, rows: 20, page: 1, pageCount: 2 })
      await flushPromises()
      await runDownload('Download CSV...')

      expect(eventStore.getEventsForExport).toHaveBeenCalledWith({
        limit: 20,
        offset: 20,
        _s: `node.id==${mockNodeId}`
      })
    })

    it('downloads a CSV of every event field, quoting values that contain commas or quotes', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([
        {
          id: 101,
          severity: 'Major',
          logMessage: '<p>Interface "eth0" down, node 42</p>',
          serviceType: { id: 3, name: 'ICMP' }
        }
      ])

      await runDownload('Download CSV...')

      expect(downloadNames).toEqual(['Events.csv'])
      expect(blobs[0].type).toBe('text/csv')
      expect(await blobs[0].text()).toBe(
        'id,severity,logMessage,serviceType\n' +
        '101,Major,"<p>Interface ""eth0"" down, node 42</p>",ICMP'
      )
    })

    // The table only shows 4 columns, but the export is the whole record.
    it('includes CSV columns for fields the table does not display', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([mockEvent])

      await runDownload('Download CSV...')

      const [header] = (await blobs[0].text()).split('\n')
      expect(header.split(',')).toEqual(Object.keys(mockEvent))
    })

    // parameters is the other non-primitive field on an event; String() on it would put
    // '[object Object]' in the cell.
    it('keeps other non-primitive CSV fields readable as JSON', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([
        { id: 101, parameters: [{ name: 'p1', value: 'v1' }] }
      ])

      await runDownload('Download CSV...')

      expect(await blobs[0].text()).toBe(
        'id,parameters\n' +
        '101,"[{""name"":""p1"",""value"":""v1""}]"'
      )
    })

    it('downloads JSON of the full event records', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([mockEvent])

      await runDownload('Download JSON...')

      expect(downloadNames).toEqual(['Events.json'])
      expect(blobs[0].type).toBe('application/json')
      expect(JSON.parse(await blobs[0].text())).toEqual([mockEvent])
    })

    it('shows an error snackbar and downloads nothing when the node has no events', async () => {
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([])

      await runDownload('Download CSV...')

      expect(downloadNames).toEqual([])
      expect(showSnackBar).toHaveBeenCalledWith(
        expect.objectContaining({ error: true })
      )
    })
  })

  describe('Node changes under the same component instance', () => {
    // /node/42 -> /node/99 reuses this instance, so a table that only reads the route id at
    // setup would keep showing node 42 while the panel's other links point at 99.
    it('refetches for the new node and exports that node', async () => {
      // Empty, so the download stops at the snackbar: what matters here is the node it asked
      // for, not the file it would have produced.
      eventStore.getEventsForExport = vi.fn().mockResolvedValue([])
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(eventStore.getEvents).toHaveBeenCalledWith(
        expect.objectContaining({ _s: 'node.id==99', offset: 0, limit: 5 })
      )

      const items = wrapper.findComponent(NodeDownloadDropdown).vm.items as Array<{ label: string, command: () => void }>
      await items[0].command()
      await flushPromises()

      expect(eventStore.getEventsForExport).toHaveBeenCalledWith(
        expect.objectContaining({ _s: 'node.id==99' })
      )
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
