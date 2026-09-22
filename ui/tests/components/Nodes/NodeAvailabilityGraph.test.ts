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

import NodeAvailabilityGraph from '@/components/Nodes/NodeAvailabilityGraph.vue'
import { LOADING_DELAY_MS } from '@/components/Nodes/hooks/useDelayedLoading'
import { NodeAvailability } from '@/types'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('vue-router', () => ({
  useRoute: () => ({ params: { id: '101' }}),
  useRouter: () => ({ push: vi.fn() })
}))

vi.mock('@/services', () => ({
  default: {
    getNodeAvailabilityPercentage: vi.fn(),
    getNodeOutageTimeline: vi.fn()
  }
}))

import API from '@/services'

const getTimeline = vi.mocked(API.getNodeOutageTimeline)
const getAvailability = vi.mocked(API.getNodeAvailabilityPercentage)

const HOUR = 3_600_000
const MINUTE = 60_000

// The panel derives its window from the clock at setup, so the fixtures have to be anchored to the
// same clock: a fixed timestamp would fall outside the window and every bar would be dropped.
let NOW = Date.now()

const availabilityDoc = (overrides: Partial<NodeAvailability> = {}): NodeAvailability => ({
  availability: 98.76,
  id: 101,
  'service-count': 2,
  'service-down-count': 0,
  ipinterfaces: [
    {
      address: '192.168.1.1',
      availability: 99.5,
      id: 1,
      services: [
        { id: 10, name: 'ICMP', serviceId: 1, availability: 99.5 },
        { id: 11, name: 'SNMP', serviceId: 2, availability: 97.0 }
      ]
    }
  ],
  ...overrides
})

const timelineDoc = (overrides: Partial<NodeOutageTimeline> = {}): NodeOutageTimeline => ({
  nodeId: 101,
  start: NOW - 24 * HOUR,
  end: NOW,
  nodeCreateTime: NOW - 400 * HOUR,
  count: 1,
  outage: [
    {
      id: 3543,
      ifServiceId: 11,
      ipInterfaceId: 1,
      ipAddress: '192.168.1.1',
      serviceId: 2,
      serviceName: 'SNMP',
      ifLostService: NOW - 6 * HOUR,
      ifRegainedService: NOW - 5 * HOUR
    }
  ],
  ...overrides
})

const TimeControlsStub = {
  name: 'TimeControls',
  template: '<button @click="$emit(\'updateTime\', payload)">range</button>',
  props: ['label'],
  emits: ['updateTime'],
  computed: {
    payload() {
      return {
        startTime: Math.floor((NOW - HOUR) / 1000),
        endTime: Math.floor(NOW / 1000),
        format: 'hours',
        range: { unit: 'hours', amount: 1 }
      }
    }
  }
}

const NO_NODE = Symbol('no node')

const mountPanel = (
  nodeId: string | typeof NO_NODE = '101',
  stubs: Record<string, unknown> = {}
) =>
  mount(NodeAvailabilityGraph, {
    props: {
      baseHref: '/opennms/',
      node: (nodeId === NO_NODE ? {} : { id: nodeId }) as any
    },
    global: {
      plugins: [createTestingPinia({ createSpy: vi.fn, stubActions: false }), PrimeVue],
      stubs: { TimeControls: true, ...stubs },
      directives: { 'onms-tooltip': {}}
    }
  })

describe('NodeAvailabilityGraph.vue', () => {
  let wrapper: VueWrapper<any>

  beforeEach(() => {
    vi.clearAllMocks()
    NOW = Date.now()
    getAvailability.mockResolvedValue(availabilityDoc())
    getTimeline.mockResolvedValue(timelineDoc())
  })

  afterEach(() => {
    wrapper?.unmount()
    vi.useRealTimers()
  })

  // The details page keeps one component instance across node ids and nothing resets it, so a
  // panel that only fetches when props.node.id CHANGES never fetches at all on a return visit.
  it('fetches both documents once on mount, for the same window', async () => {
    wrapper = mountPanel()
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(1)
    expect(getTimeline).toHaveBeenCalledTimes(1)

    const [, availStart, availEnd] = getAvailability.mock.calls[0]
    const [, tlStart, tlEnd] = getTimeline.mock.calls[0]
    expect(availStart).toBe(tlStart)
    expect(availEnd).toBe(tlEnd)
  })

  it('does not fetch before the node is known', async () => {
    wrapper = mountPanel(NO_NODE)
    await flushPromises()

    expect(getAvailability).not.toHaveBeenCalled()
    expect(getTimeline).not.toHaveBeenCalled()
  })

  it('defaults to a 24 hour window, matching the range picker default', async () => {
    wrapper = mountPanel()
    await flushPromises()

    const [, start, end] = getTimeline.mock.calls[0]
    expect(end - start).toBe(24 * HOUR)
    // The label the picker shows before anything is picked.
    expect(wrapper.text()).toContain('last day')
  })

  it('renders the card element', async () => {
    wrapper = mountPanel()
    await flushPromises()
    expect(wrapper.find('.card').exists()).toBe(true)
  })

  it('renders the node availability figure', async () => {
    wrapper = mountPanel()
    await flushPromises()
    expect(wrapper.get('[data-test="node-availability"]').text()).toBe('98.76%')
  })

  // The previous version computed 100 * undefined on first paint.
  it('never renders NaN, before or after the fetch resolves', async () => {
    getAvailability.mockReturnValue(new Promise(() => {}) as any)
    getTimeline.mockReturnValue(new Promise(() => {}) as any)

    wrapper = mountPanel()
    expect(wrapper.text()).not.toContain('NaN')

    await flushPromises()
    expect(wrapper.text()).not.toContain('NaN')
  })

  // Two placeholder cells shipped to develop in the previous version.
  it('renders no placeholder text', async () => {
    wrapper = mountPanel()
    await flushPromises()
    expect(wrapper.text()).not.toContain('XXX')
  })

  // The previous version rendered the axis image once per interface.
  it('renders exactly one time axis regardless of interface count', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({
      ipinterfaces: [
        {
          address: '192.168.1.1', availability: 99.5, id: 1,
          services: [{ id: 10, name: 'ICMP', serviceId: 1, availability: 99.5 }]
        },
        {
          address: '192.168.1.2', availability: 100, id: 2,
          services: [{ id: 12, name: 'ICMP', serviceId: 1, availability: 100 }]
        }
      ]
    }))

    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.findAll('[data-test="timeline-axis"]')).toHaveLength(1)
    expect(wrapper.findAll('[data-test="timeline-service-row"]')).toHaveLength(2)
  })

  it('shows an error state when either document fails', async () => {
    getTimeline.mockResolvedValue(null)

    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.find('[data-test="availability-error"]').exists()).toBe(true)
    expect(wrapper.find('[data-test="availability-timeline"]').exists()).toBe(false)
  })

  it('shows an empty state when the node has no monitored services', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({ ipinterfaces: [] }))

    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('does not show a spinner for a fast fetch', async () => {
    wrapper = mountPanel()
    await flushPromises()
    expect(wrapper.findComponent({ name: 'OnmsSpinner' }).exists()).toBe(false)
  })

  it('shows a spinner once a fetch outlasts the delay', async () => {
    vi.useFakeTimers()
    getAvailability.mockReturnValue(new Promise(() => {}) as any)
    getTimeline.mockReturnValue(new Promise(() => {}) as any)

    wrapper = mountPanel()
    expect(wrapper.findComponent({ name: 'OnmsSpinner' }).exists()).toBe(false)

    vi.advanceTimersByTime(LOADING_DELAY_MS + 10)
    await wrapper.vm.$nextTick()

    expect(wrapper.findComponent({ name: 'OnmsSpinner' }).exists()).toBe(true)
  })

  it('links each outage segment to its detail page and describes it', async () => {
    wrapper = mountPanel()
    await flushPromises()

    const segment = wrapper.get('[data-test="outage-segment"]')
    expect(segment.attributes('href')).toBe('/opennms/outage/detail.htm?id=3543')

    const label = segment.attributes('aria-label') ?? ''
    expect(label).toContain('Outage 3543')
    expect(label).toContain('SNMP')
    expect(label).toContain('192.168.1.1')
  })

  // The service type id now comes from the availability payload, so the panel no longer depends
  // on nodeListStore.getServiceTypeByName -- which is populated at app start and is therefore a
  // race on a cold deep link, producing '#' links.
  it('builds service links without any nodeListStore seeding', async () => {
    wrapper = mountPanel()
    await flushPromises()

    const hrefs = wrapper.findAll('.service-link').map(a => a.attributes('href'))
    expect(hrefs).toHaveLength(2)
    hrefs.forEach(href => expect(href).not.toBe('#'))
    expect(hrefs[0]).toContain('service=1')
    expect(hrefs[1]).toContain('service=2')
  })

  // Green on an unmonitored strip would claim a full window of uptime for a service nothing was
  // watching. The legacy availability box leaves the same rows empty.
  it('does not paint an unmonitored service as available', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({
      ipinterfaces: [{
        address: '192.168.1.1', availability: 100, id: 1,
        services: [
          { id: 10, name: 'ICMP', serviceId: 1, availability: 100 },
          { id: 11, name: 'OpenNMS-DB', serviceId: 2, availability: -1 }
        ]
      }]
    }))

    wrapper = mountPanel()
    await flushPromises()

    const strips = wrapper.findAll('[data-test="timeline-strip"]')
    expect(strips[0].classes()).not.toContain('strip--unmonitored')
    expect(strips[1].classes()).toContain('strip--unmonitored')
    expect(wrapper.text()).toContain('Not Monitored')
  })

  it('encodes a scope-qualified IPv6 address in the links it builds', async () => {
    getAvailability.mockResolvedValue(availabilityDoc({
      ipinterfaces: [{
        address: 'fe80::1%eth0', availability: 100, id: 1,
        services: [{ id: 10, name: 'ICMP', serviceId: 1, availability: 100 }]
      }]
    }))

    wrapper = mountPanel()
    await flushPromises()

    expect(wrapper.get('.service-link').attributes('href')).toContain('intf=fe80%3A%3A1%25eth0')
  })

  it('refetches both documents when the range changes', async () => {
    wrapper = mountPanel('101', { TimeControls: TimeControlsStub })
    await flushPromises()
    expect(getTimeline).toHaveBeenCalledTimes(1)

    await wrapper.get('[data-test="availability-range"]').trigger('click')
    await flushPromises()

    expect(getTimeline).toHaveBeenCalledTimes(2)
    expect(getAvailability).toHaveBeenCalledTimes(2)

    const [, start, end] = getTimeline.mock.calls[1]
    expect(end - start).toBe(HOUR)
  })

  // The heading should name the range the way the picker's own button does, rather than
  // reconstructing it from unit and amount, which pluralised 'Last hour' as 'last 1 hours'.
  it('names the selected range the way the picker does', async () => {
    wrapper = mountPanel('101', { TimeControls: TimeControlsStub })
    await flushPromises()

    await wrapper.get('[data-test="availability-range"]').trigger('click')
    await flushPromises()

    expect(wrapper.text()).toContain('last hour')
    expect(wrapper.text()).not.toContain('last 1 hours')
  })

  // The panel outlives a node change, so a window resolved once at setup goes stale: navigating to
  // another node an hour later would ask for the hour-old window.
  it('re-resolves a relative range against the clock when the node changes', async () => {
    vi.useFakeTimers({ shouldAdvanceTime: true })
    vi.setSystemTime(NOW)

    wrapper = mountPanel()
    await flushPromises()
    const firstEnd = getTimeline.mock.calls[0][2] as number

    vi.setSystemTime(NOW + 2 * HOUR)
    await wrapper.setProps({ node: { id: '202' } as any })
    await flushPromises()

    const secondEnd = getTimeline.mock.calls[1][2] as number
    expect(secondEnd - firstEnd).toBeGreaterThanOrEqual(2 * HOUR - 1000)
  })

  it('keeps an absolute custom range fixed across a node change', async () => {
    const AbsoluteStub = {
      name: 'TimeControls',
      template: '<button @click="$emit(\'updateTime\', payload)">range</button>',
      emits: ['updateTime'],
      computed: {
        payload() {
          // No `range`, which is what TimeControls emits for a custom absolute window.
          return {
            startTime: Math.floor((NOW - 5 * HOUR) / 1000),
            endTime: Math.floor((NOW - 4 * HOUR) / 1000),
            format: 'hours'
          }
        }
      }
    }

    wrapper = mountPanel('101', { TimeControls: AbsoluteStub })
    await flushPromises()
    await wrapper.get('[data-test="availability-range"]').trigger('click')
    await flushPromises()

    const [, pickedStart, pickedEnd] = getTimeline.mock.calls[1]

    await wrapper.setProps({ node: { id: '202' } as any })
    await flushPromises()

    expect(getTimeline.mock.calls[2].slice(1)).toEqual([pickedStart, pickedEnd])
    expect(wrapper.text()).toContain('custom range')
  })

  // A slow first request must not overwrite the result of a later one.
  it('ignores a superseded response', async () => {
    let resolveFirst: (v: NodeOutageTimeline) => void = () => {}
    const first = new Promise<NodeOutageTimeline>((r) => {
      resolveFirst = r
    })

    getTimeline.mockReturnValueOnce(first as any)
    getTimeline.mockResolvedValueOnce(timelineDoc({
      outage: [{
        id: 9999, ifServiceId: 11, ipInterfaceId: 1, ipAddress: '192.168.1.1',
        serviceId: 2, serviceName: 'SNMP',
        ifLostService: NOW - 30 * MINUTE, ifRegainedService: NOW - 20 * MINUTE
      }]
    }))

    wrapper = mountPanel('101', { TimeControls: TimeControlsStub })
    await wrapper.get('[data-test="availability-range"]').trigger('click')
    await flushPromises()

    // The first request now lands, out of order, carrying outage 3543.
    resolveFirst(timelineDoc())
    await flushPromises()

    const hrefs = wrapper.findAll('[data-test="outage-segment"]')
      .map(a => a.attributes('href'))
    expect(hrefs).toContain('/opennms/outage/detail.htm?id=9999')
    expect(hrefs).not.toContain('/opennms/outage/detail.htm?id=3543')
  })
})
