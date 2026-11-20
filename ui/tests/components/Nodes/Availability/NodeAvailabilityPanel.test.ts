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

import NodeAvailabilityPanel from '@/components/Nodes/Availability/NodeAvailabilityPanel.vue'
import { NodeAvailability, NodeAvailabilityInterface } from '@/types/index'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '101' }}),
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('@/services/index', () => ({
  default: {
    getNodeAvailabilityPercentage: vi.fn(),
    getNodeOutageTimeline: vi.fn()
  }
}))

import API from '@/services/index'

const getTimeline = vi.mocked(API.getNodeOutageTimeline)
const getAvailability = vi.mocked(API.getNodeAvailabilityPercentage)

const HOUR = 3_600_000

// The composable resolves its window from the clock, so the fixtures are anchored to the same clock.
let NOW = Date.now()

const iface = (n: number): NodeAvailabilityInterface => ({
  address: `192.168.1.${n}`,
  availability: 100,
  id: n,
  services: [
    { id: n * 10, name: 'ICMP', serviceId: 1, availability: 100 },
    { id: n * 10 + 1, name: 'SNMP', serviceId: 2, availability: 100 }
  ]
})

const availabilityDoc = (overrides: Partial<NodeAvailability> = {}): NodeAvailability => ({
  availability: 98.76,
  id: 101,
  'service-count': 2,
  'service-down-count': 0,
  ipinterfaceCount: 1,
  ipinterfaces: [iface(1)],
  ...overrides
})

const timelineDoc = (overrides: Partial<NodeOutageTimeline> = {}): NodeOutageTimeline => ({
  nodeId: 101,
  start: NOW - 24 * HOUR,
  end: NOW,
  nodeCreateTime: NOW - 400 * HOUR,
  count: 0,
  outage: [],
  ...overrides
})

const mountPanel = (nodeId = '101', stubs: Record<string, unknown> = {}) =>
  mount(NodeAvailabilityPanel, {
    props: {
      baseHref: '/opennms/',
      node: { id: nodeId } as any
    },
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { TimeControls: true, ...stubs },
      directives: { 'onms-tooltip': {}}
    }
  })

const TimeControlsStub = {
  name: 'TimeControls',
  template: '<button></button>',
  props: ['label', 'initialLabel'],
  emits: ['updateTime']
}

/** What TimeControls emits for the 'Last hour' preset. */
const lastHourTime = () => ({
  startTime: Math.floor((Date.now() - HOUR) / 1000),
  endTime: Math.floor(Date.now() / 1000),
  format: 'hours',
  range: { unit: 'hours', amount: 1 }
})

const PaginatorStub = {
  name: 'OnmsPaginator',
  template: '<nav></nav>',
  props: ['first', 'rows', 'totalRecords', 'rowsPerPageOptions'],
  emits: ['page']
}

/** The paging the composable asked the server for, call by call. */
const pagingCalls = () => getAvailability.mock.calls.map(([id, , , paging]) => ({ id, ...paging }))

describe('NodeAvailabilityPanel.vue', () => {
  let wrapper: VueWrapper<any>

  const paginator = () => wrapper.findComponent({ name: 'OnmsPaginator' })
  const turnPage = async (first: number, rows: number) => {
    paginator().vm.$emit('page', { page: Math.floor(first / rows), first, rows })
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    NOW = Date.now()
    getAvailability.mockResolvedValue(availabilityDoc())
    getTimeline.mockResolvedValue(timelineDoc())
  })

  afterEach(() => {
    wrapper?.unmount()
  })

  it('asks for the first 10 interfaces', async () => {
    wrapper = mountPanel()
    await flushPromises()

    expect(pagingCalls()).toEqual([{ id: '101', limit: 10, offset: 0, withServices: true }])
  })

  it('is titled Availability and has the range picker but no View Availability button', async () => {
    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.text()).toContain('Availability')
    expect(wrapper.find('[data-test="availability-range"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="availability-details-button"]').exists()).toBe(false)
  })

  it('shows the node figure in the summary badge', async () => {
    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.get('[data-test="node-availability"]').text()).toBe('98.76%')
  })

  // The Node Details panel cuts at 8 rows; this page shows every service of the interfaces fetched.
  it('shows every interface fetched, without cutting rows or pointing elsewhere', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({
      ipinterfaceCount: 6,
      'service-count': 12,
      ipinterfaces: [1, 2, 3, 4, 5, 6].map(iface)
    }))

    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.findAll('[data-test="timeline-service-row"]')).toHaveLength(12)
    expect(wrapper.find('[data-test="availability-partial"]').exists()).toBe(false)
  })

  describe('paginator', () => {
    it('pages interfaces 10 at a time, out of the node\'s total, with 5, 10, 20 or 50 per page', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()

      const p = paginator()
      expect(p.attributes('data-test')).toBe('availability-paginator')
      expect(p.props('first')).toBe(0)
      expect(p.props('rows')).toBe(10)
      expect(p.props('totalRecords')).toBe(60)
      expect(p.props('rowsPerPageOptions')).toEqual([5, 10, 20, 50])
    })

    // A page is a number of interfaces, each with every service, not a number of rows: say so.
    it('names interfaces as the unit it pages by', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()

      expect(paginator().props('pageReport')).toBe('Interfaces {first}–{last} of {totalRecords}')
      expect(paginator().find('.p-paginator-current').text()).toBe('Interfaces 1–10 of 60')
    })

    it('fetches the page chosen, the roster only', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()

      await turnPage(25, 25)

      expect(pagingCalls().at(-1)).toEqual({ id: '101', limit: 25, offset: 25, withServices: true })
      expect(getTimeline).toHaveBeenCalledTimes(1)
      expect(paginator().props('first')).toBe(25)
    })

    it('fetches the page size chosen', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()

      await turnPage(0, 50)

      expect(pagingCalls().at(-1)).toEqual({ id: '101', limit: 50, offset: 0, withServices: true })
      expect(paginator().props('rows')).toBe(50)
    })

    // One fetch for the new node, at its first page: never one at the old node's page.
    it('goes back to the first page for another node', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()
      await turnPage(25, 25)

      await wrapper.setProps({ node: { id: '102' } as any })
      await flushPromises()

      expect(pagingCalls().filter(call => call.id === '102')).toEqual([{ id: '102', limit: 25, offset: 0, withServices: true }])
      expect(paginator().props('first')).toBe(0)
    })

    // The roster shrank under the page shown: go to the last page there is, not an empty one.
    // PrimeVue's paginator does the same on its own, so it is stubbed: the panel must not rely on
    // that, nor on the paginator being shown at all.
    it('moves to the last page when the page shown is past the end', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel('101', { OnmsPaginator: PaginatorStub })
      await flushPromises()

      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 35, ipinterfaces: [] }))
      await turnPage(50, 10)

      expect(paginator().props('first')).toBe(30)
      expect(pagingCalls().at(-1)).toEqual({ id: '101', limit: 10, offset: 30, withServices: true })
    })

    it('keeps the page shown when it is within the total', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel()
      await flushPromises()
      await turnPage(50, 10)

      expect(paginator().props('first')).toBe(50)
      expect(pagingCalls().at(-1)).toEqual({ id: '101', limit: 10, offset: 50, withServices: true })
    })

    // Only another node goes back to the first page: a new range keeps the page shown.
    it('keeps the page when the range changes', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 60 }))
      wrapper = mountPanel('101', { TimeControls: TimeControlsStub })
      await flushPromises()
      await turnPage(25, 25)

      wrapper.findComponent({ name: 'TimeControls' }).vm.$emit('updateTime', lastHourTime())
      await flushPromises()

      expect(getTimeline).toHaveBeenCalledTimes(2)
      expect(paginator().props('first')).toBe(25)
      expect(pagingCalls().at(-1)).toEqual({ id: '101', limit: 25, offset: 25, withServices: true })
    })

    it('is not shown for a node with no monitored interfaces', async () => {
      getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 0, ipinterfaces: [] }))
      wrapper = mountPanel()
      await flushPromises()

      expect(paginator().exists()).toBe(false)
    })
  })

  it('shows the empty state for a node with no monitored services', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaceCount: 0, ipinterfaces: [] }))
    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.find('[data-test="availability-empty"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="availability-timeline"]').exists()).toBe(false)
  })

  it('shows the error state when a document fails', async () => {
    getTimeline.mockResolvedValue(null)
    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.find('[data-test="availability-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="availability-timeline"]').exists()).toBe(false)
  })

  it('says so when the server truncated the outages', async () => {
    getTimeline.mockResolvedValue(timelineDoc({ truncated: true, count: 500 }))
    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.get('[data-test="availability-truncated"]').text()).toContain('500')
    expect(wrapper.find('[data-test="availability-timeline"]').exists()).toBe(true)
  })
})
