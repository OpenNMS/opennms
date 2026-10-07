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

import OutagesTable from '@/components/Nodes/OutagesTable.vue'
import NodeDownloadDropdown from '@/components/Nodes/NodeDownloadDropdown.vue'
import { OnmsTag } from '@opennms/onms-ui'
import { useOutageStore } from '@/stores/outageStore'
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

const mockOutage = {
  id: 2435,
  ipAddress: '10.0.0.44',
  serviceId: 3,
  monitoredService: { id: 99, serviceType: { id: 3, name: 'ICMP' }},
  ifLostService: 1700000000000,
  ifRegainedService: 1700009999000,
  hostname: 'host1.example.com',
  nodeId: 42
}

describe('OutagesTable.vue', () => {
  let wrapper: VueWrapper<any>
  let outageStore: ReturnType<typeof useOutageStore>

  // Seed the store's node slice as a successful fetch for `nodeId` would leave it.
  const setNodeOutages = (outages: unknown[], totalCount = outages.length, nodeId = mockNodeId) => {
    outageStore.nodeOutages = outages as any
    outageStore.nodeOutagesTotalCount = totalCount
    outageStore.nodeOutagesNodeId = nodeId
  }

  // The store action must be mocked BEFORE mounting — the component fetches in
  // onMounted, and an unmocked action would fire a real network request.
  const mountTable = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    outageStore = useOutageStore(pinia)
    outageStore.getNodeOutages = vi.fn().mockResolvedValue({ success: true, message: '' })
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as any

    return mount(OutagesTable, {
      global: {
        plugins: [pinia, PrimeVue],
        directives: {
          // Marks the value instead of formatting it, so a test can tell the directive was
          // applied without depending on the app's date format or time zone.
          date: {
            mounted(el: Element) {
              el.innerHTML = `formatted:${el.innerHTML}`
            }
          }
        }
      }
    })
  }

  beforeEach(async () => {
    vi.clearAllMocks()
    ;(useRoute() as any).params.id = mockNodeId
    wrapper = mountTable()
    setNodeOutages([], 0)
    await flushPromises()
    await nextTick()
  })

  afterEach(() => {
    vi.restoreAllMocks()
  })

  describe('Rendering', () => {
    it('renders the DataTable with data-test attribute', () => {
      expect(wrapper.find('[data-test="outages-table"]').exists()).toBe(true)
    })

    it('renders the column headers, without Host Name', () => {
      const headers = wrapper.findAll('th')
      const headerTexts = headers.map(h => h.text())
      expect(headerTexts).toContain('ID')
      expect(headerTexts).toContain('IP Address')
      expect(headerTexts).toContain('Service Name')
      expect(headerTexts).toContain('Lost')
      expect(headerTexts).toContain('Regained')
      expect(headerTexts).not.toContain('Host Name')
    })

    it('renders rows for each outage, and no longer the host name', async () => {
      setNodeOutages([mockOutage], 1)
      await nextTick()

      const rows = wrapper.findAll('tbody tr')
      expect(rows.length).toBe(1)
      expect(rows[0].text()).toContain('10.0.0.44')
      expect(rows[0].text()).toContain('ICMP')
      expect(rows[0].text()).not.toContain('host1.example.com')
    })
  })

  describe('Card and title row', () => {
    it('wraps the panel content in a card with the title and both action buttons on one row', () => {
      expect(wrapper.find('.card').exists()).toBe(true)

      const titleRow = wrapper.find('.title-row')
      expect(titleRow.text()).toContain('Recent Outages')
      expect(titleRow.find('[data-test="view-outages-button"]').exists()).toBe(true)
      expect(titleRow.find('[data-test="download-button"]').exists()).toBe(true)
    })

    it('navigates to the legacy outage list filtered to this node', async () => {
      const assign = vi.fn()
      vi.stubGlobal('location', { assign } as any)

      await wrapper.find('[data-test="view-outages-button"]').trigger('click')

      expect(assign).toHaveBeenCalledWith(`/opennms/outage/list.htm?filter=node%3D${mockNodeId}&outtype=both`)
      vi.unstubAllGlobals()
    })
  })

  describe('Cell links', () => {
    const cellLinks = async () => {
      setNodeOutages([mockOutage], 1)
      await nextTick()

      return wrapper.findAll('tbody tr td a')
    }

    it('links the ID to the outage detail page', async () => {
      const links = await cellLinks()

      expect(links[0].text()).toBe('2435')
      expect(links[0].attributes('href')).toBe('/opennms/outage/detail.htm?id=2435')
    })

    it('links the IP address to the interface page for this node', async () => {
      const links = await cellLinks()

      expect(links[1].text()).toBe('10.0.0.44')
      expect(links[1].attributes('href')).toBe(`/opennms/element/interface.jsp?node=${mockNodeId}&intf=10.0.0.44`)
    })

    // The row carries monitoredService.serviceType.name, so the name needs no lookup against
    // the service types the app loads at startup -- and cannot read N/A while that is in flight.
    it('takes the service name from the row and links it to the service page', async () => {
      const links = await cellLinks()

      expect(links[2].text()).toBe('ICMP')
      expect(links[2].attributes('href'))
        .toBe(`/opennms/element/service.jsp?node=${mockNodeId}&intf=10.0.0.44&service=3`)
    })

    it('shows N/A without a link when the outage has no IP address or known service', async () => {
      setNodeOutages([{ id: 1, serviceId: 99 }], 1)
      await nextTick()

      const cells = wrapper.findAll('tbody tr td')
      expect(cells[1].text()).toBe('N/A')
      expect(cells[1].find('a').exists()).toBe(false)
      expect(cells[2].text()).toBe('N/A')
      expect(cells[2].find('a').exists()).toBe(false)
    })

    it('shows a known service name without a link when the outage has no IP address', async () => {
      setNodeOutages([{ id: 1, serviceId: 3, monitoredService: { serviceType: { name: 'ICMP' }}}], 1)
      await nextTick()

      const cells = wrapper.findAll('tbody tr td')
      expect(cells[2].text()).toBe('ICMP')
      expect(cells[2].find('a').exists()).toBe(false)
    })
  })

  describe('Date columns', () => {
    // An ongoing outage has no regained time; it kept showing N/A before the dates were
    // formatted and should keep doing so.
    it('shows N/A for a missing lost or regained time', async () => {
      setNodeOutages([{ id: 1 }], 1)
      await nextTick()

      const cells = wrapper.findAll('tbody tr td')
      expect(cells[3].text()).toBe('N/A')
      expect(cells[4].text()).toBe('N/A')
    })

    // An outage with no regained time is still down, so its lost time is called out.
    it('tags the lost time as danger while the outage is unresolved', async () => {
      setNodeOutages([{ id: 1, ifLostService: 1700000000000 }], 1)
      await nextTick()

      const tag = wrapper.findComponent(OnmsTag)
      expect(tag.exists()).toBe(true)
      expect(tag.props('severity')).toBe('danger')
      expect(tag.text()).toBe('formatted:1700000000000')
    })

    it('leaves the lost time untagged once the outage has been regained', async () => {
      setNodeOutages([mockOutage], 1)
      await nextTick()

      expect(wrapper.findComponent(OnmsTag).exists()).toBe(false)
    })

    it('renders the lost and regained times through the date directive', async () => {
      setNodeOutages([mockOutage], 1)
      await nextTick()

      const cells = wrapper.findAll('tbody tr td')
      expect(cells[3].text()).toBe('formatted:1700000000000')
      expect(cells[4].text()).toBe('formatted:1700009999000')
    })
  })

  describe('Download', () => {
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

    // The download is the page the paginator is showing, which the store already holds: no
    // second request, and no way for the file to disagree with the table.
    it('exports the rows the table is showing without another request', async () => {
      setNodeOutages([mockOutage], 1)
      vi.clearAllMocks()

      await runDownload('Download CSV...')

      expect(outageStore.getNodeOutages).not.toHaveBeenCalled()
      expect(downloadNames).toEqual(['Outages.csv'])
      expect(await blobs[0].text()).toContain(String(mockOutage.id))
    })

    it('downloads a CSV of every outage field', async () => {
      setNodeOutages([{ id: 2435, ipAddress: '10.0.0.44' }])

      await runDownload('Download CSV...')

      expect(downloadNames).toEqual(['Outages.csv'])
      expect(blobs[0].type).toBe('text/csv')
      expect(await blobs[0].text()).toBe('id,ipAddress\n2435,10.0.0.44')
    })

    // Log messages and service names come from traps and syslog, so a leading = + - @ or tab
    // would execute as a formula when the file is opened in a spreadsheet.
    it('neutralises spreadsheet formulas in CSV text cells', async () => {
      setNodeOutages([{ id: 1, ipAddress: '=cmd|calc!A1' }])

      await runDownload('Download CSV...')

      expect(await blobs[0].text()).toBe('id,ipAddress\n1,\'=cmd|calc!A1')
    })

    // A number cannot be a formula, and guarding one would corrupt a legitimate negative.
    it('leaves negative numbers alone', async () => {
      setNodeOutages([{ id: -5, ipAddress: '10.0.0.44' }])

      await runDownload('Download CSV...')

      expect(await blobs[0].text()).toBe('id,ipAddress\n-5,10.0.0.44')
    })

    // Only serviceType used to be special-cased, so an outage's monitoredService landed in one
    // cell as a JSON blob.
    it('expands nested objects into their own CSV columns', async () => {
      setNodeOutages([{
        id: 1,
        monitoredService: { id: 99, serviceType: { id: 3, name: 'ICMP' }}
      }])

      await runDownload('Download CSV...')

      expect(await blobs[0].text()).toBe(
        'id,monitoredService.id,monitoredService.serviceType.id,monitoredService.serviceType.name\n' +
        '1,99,3,ICMP'
      )
    })

    it('downloads JSON of the full outage records', async () => {
      setNodeOutages([mockOutage])

      await runDownload('Download JSON...')

      expect(downloadNames).toEqual(['Outages.json'])
      expect(blobs[0].type).toBe('application/json')
      expect(JSON.parse(await blobs[0].text())).toEqual([mockOutage])
    })

    it('shows an error snackbar and downloads nothing when the node has no outages', async () => {
      setNodeOutages([])

      await runDownload('Download CSV...')

      expect(downloadNames).toEqual([])
      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
    })
  })

  describe('Node changes under the same component instance', () => {
    it('refetches for the new node', async () => {
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(outageStore.getNodeOutages).toHaveBeenLastCalledWith('99', { limit: 5, offset: 0 })
    })

    // The store's slice is replaced only on success, so until node 99's outages arrive -- or
    // when they fail to -- it still holds node 42's. Those must not be shown under node 99.
    it('shows nothing until the store holds the new node\'s outages', async () => {
      setNodeOutages([mockOutage], 1, mockNodeId)
      ;(useRoute() as any).params.id = '99'
      await flushPromises()

      expect(wrapper.findAll('tbody tr td a')).toHaveLength(0)
      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
    })
  })

  describe('Loading and failure', () => {
    // Another node's slice is not this node's: nothing to show yet, and not "no results".
    it('says it is loading while this node\'s outages are in flight', async () => {
      setNodeOutages([], 0, 'some-other-node')
      await nextTick()

      expect(wrapper.text()).toContain('Loading outages…')
      expect(wrapper.text()).not.toContain('No results found.')
    })

    it('says the outages could not be loaded when this node\'s fetch failed', async () => {
      setNodeOutages([], 0, 'some-other-node')
      outageStore.nodeOutagesFailedNodeId = mockNodeId
      await nextTick()

      expect(wrapper.text()).toContain('Unable to load outages for this node.')
      expect(wrapper.text()).not.toContain('No results found.')
    })

    // The slice still holds page one, so the paginator must not say page two.
    it('goes back to the page on screen when a page change fails', async () => {
      setNodeOutages(Array.from({ length: 5 }, (_, i) => ({ id: i + 1 })), 12)
      outageStore.nodeOutagesPage = { offset: 0, limit: 5 }
      outageStore.getNodeOutages = vi.fn().mockResolvedValue({ success: false, message: 'nope' })
      await nextTick()

      await wrapper.findAll('.p-paginator-page')[1].trigger('click')
      await flushPromises()

      expect(outageStore.getNodeOutages).toHaveBeenCalledWith(mockNodeId, { offset: 5, limit: 5 })
      expect(wrapper.find('.p-paginator-page-selected').text()).toBe('1')
    })
  })

  describe('Empty state', () => {
    it('shows EmptyList when there are no rows', async () => {
      setNodeOutages([], 0)
      await nextTick()

      expect(wrapper.findComponent({ name: 'EmptyList' }).exists()).toBe(true)
      expect(wrapper.text()).toContain('No results found.')
    })
  })

  describe('onMounted fetch', () => {
    it('calls getNodeOutages on mount with node id, offset 0 and limit 5', async () => {
      vi.clearAllMocks()
      const localWrapper = mountTable()
      await flushPromises()
      await nextTick()

      expect(outageStore.getNodeOutages).toHaveBeenCalledWith(
        mockNodeId,
        expect.objectContaining({ offset: 0, limit: 5 })
      )

      localWrapper.unmount()
    })
  })

  describe('Lazy pagination — onPage', () => {
    it('calls getNodeOutages with updated offset and limit, preserving node id', async () => {
      await wrapper.vm.onPage({ first: 10, rows: 10, page: 1, pageCount: 2 } as any)
      await flushPromises()

      expect(outageStore.getNodeOutages).toHaveBeenLastCalledWith(
        mockNodeId,
        expect.objectContaining({ offset: 10, limit: 10 })
      )
    })

    it('calls getNodeOutages with the node route id', async () => {
      await wrapper.vm.onPage({ first: 0, rows: 10, page: 0, pageCount: 1 } as any)
      await flushPromises()

      expect(outageStore.getNodeOutages).toHaveBeenLastCalledWith(mockNodeId, expect.anything())
    })
  })
})
