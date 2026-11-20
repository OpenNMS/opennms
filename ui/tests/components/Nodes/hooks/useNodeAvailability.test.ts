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

import { useNodeAvailability } from '@/components/Nodes/hooks/useNodeAvailability'
import { useNodeStore } from '@/stores/nodeStore'
import { NodeAvailability } from '@/types/index'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import { createTestingPinia } from '@pinia/testing'
import { flushPromises, mount, VueWrapper } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, nextTick } from 'vue'

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
const MINUTE = 60_000

// The composable resolves its window from the clock, so the fixtures are anchored to the same
// clock: a fixed timestamp would fall outside the window and every bar would be dropped.
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

/** A promise the test resolves itself, to land responses out of order. */
const deferred = <T>() => {
  let resolve: (value: T) => void = () => {}
  const promise = new Promise<T>((r) => {
    resolve = r
  })
  return { promise, resolve }
}

const LAST_HOUR = { unit: 'hours', amount: 1 } as const

/** What TimeControls emits for the 'Last hour' preset. */
const lastHourTime = () => ({
  startTime: Math.floor((Date.now() - HOUR) / 1000),
  endTime: Math.floor(Date.now() / 1000),
  format: 'hours',
  range: { ...LAST_HOUR }
})

type Availability = ReturnType<typeof useNodeAvailability>

interface HostProps {
  nodeId?: string
  limit?: number
  offset?: number
}

const Host = defineComponent({
  props: {
    nodeId: { type: String, default: undefined },
    limit: { type: Number, default: 4 },
    offset: { type: Number, default: 0 }
  },
  setup(props) {
    result = useNodeAvailability(() => props.nodeId, () => ({ limit: props.limit, offset: props.offset }))
    return {}
  },
  render: () => h('div')
})

let result: Availability
let pinia: ReturnType<typeof createTestingPinia>

const mountHost = (props: HostProps = { nodeId: '101' }) =>
  mount(Host, {
    props,
    global: { plugins: [pinia] }
  })

const rosterCalls = () => getAvailability.mock.calls
const timelineCalls = () => getTimeline.mock.calls

describe('useNodeAvailability', () => {
  let wrapper: VueWrapper<any> | undefined

  beforeEach(() => {
    vi.clearAllMocks()
    NOW = Date.now()
    pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    getAvailability.mockResolvedValue(availabilityDoc())
    getTimeline.mockResolvedValue(timelineDoc())
  })

  afterEach(() => {
    wrapper?.unmount()
    wrapper = undefined
    vi.useRealTimers()
  })

  it('fetches roster and outages on mount', async () => {
    wrapper = mountHost({ nodeId: '101', limit: 4, offset: 8 })
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(1)
    expect(getTimeline).toHaveBeenCalledTimes(1)

    const [id, availStart, availEnd, page] = rosterCalls()[0]
    const [tlId, tlStart, tlEnd] = timelineCalls()[0]
    expect(id).toBe('101')
    expect(tlId).toBe('101')
    expect(page).toEqual({ limit: 4, offset: 8, withServices: true })
    // One window for both documents, so the bars and the figures describe the same time.
    expect(availStart).toBe(tlStart)
    expect(availEnd).toBe(tlEnd)

    expect(result.model.value?.availability).toBe(98.76)
    expect(result.hasLoaded.value).toBe(true)
    expect(result.loadFailed.value).toBe(false)
    expect(result.isFetching.value).toBe(false)
  })

  it('does not fetch before the node is known', async () => {
    wrapper = mountHost({})
    await flushPromises()

    expect(getAvailability).not.toHaveBeenCalled()
    expect(getTimeline).not.toHaveBeenCalled()
    expect(result.hasLoaded.value).toBe(false)
    expect(result.model.value).toBeUndefined()
  })

  it('defaults to a 24 hour window, matching the range picker default', async () => {
    wrapper = mountHost()
    await flushPromises()

    const [, start, end] = timelineCalls()[0]
    expect(end - start).toBe(24 * HOUR)
    expect(result.initialRangeLabel).toBe('Last day')
    expect(result.rangeLabel.value).toBe('Last day')
  })

  it('paging change refetches only the roster', async () => {
    wrapper = mountHost()
    await flushPromises()

    getAvailability.mockResolvedValueOnce(availabilityDoc({ availability: 42 }))
    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(2)
    expect(getTimeline).toHaveBeenCalledTimes(1)
    expect(rosterCalls()[1][3]).toEqual({ limit: 4, offset: 4, withServices: true })

    // The new roster joined to the cached outages.
    expect(result.model.value?.availability).toBe(42)
    expect(result.timeline.value?.count).toBe(1)
    expect(result.model.value?.interfaces[0].services.find(s => s.serviceName === 'SNMP')?.segments).toHaveLength(1)
  })

  it('page change reuses activeWindow', async () => {
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(NOW)
    useNodeStore(pinia).setAvailabilityRange({ kind: 'relative', range: { ...LAST_HOUR }})

    wrapper = mountHost()
    await flushPromises()

    const [, firstStart, firstEnd] = rosterCalls()[0]
    expect(Number(firstEnd) - Number(firstStart)).toBe(HOUR)

    vi.setSystemTime(NOW + 2 * HOUR)
    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    // "Last hour" is not re-resolved: the figures must describe the window of the cached bars.
    const [, pageStart, pageEnd] = rosterCalls()[1]
    expect(pageStart).toBe(firstStart)
    expect(pageEnd).toBe(firstEnd)
    expect(result.model.value?.window).toEqual({ start: firstStart, end: firstEnd })
  })

  it('range change refetches both', async () => {
    wrapper = mountHost()
    await flushPromises()

    result.onUpdateTime(lastHourTime())
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(2)
    expect(getTimeline).toHaveBeenCalledTimes(2)

    const [, start, end] = timelineCalls()[1]
    expect(end - start).toBe(HOUR)
    expect(rosterCalls()[1].slice(1, 3)).toEqual([start, end])
    expect(result.rangeLabel.value).toBe('Last hour')
    expect(result.model.value?.window).toEqual({ start, end })
  })

  it('node change refetches both', async () => {
    wrapper = mountHost()
    await flushPromises()

    await wrapper.setProps({ nodeId: '202' })
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(2)
    expect(getTimeline).toHaveBeenCalledTimes(2)
    expect(rosterCalls()[1][0]).toBe('202')
    expect(timelineCalls()[1][0]).toBe('202')
  })

  it('coalesces node, range and paging changes in one tick into one full fetch', async () => {
    wrapper = mountHost()
    await flushPromises()

    useNodeStore(pinia).setAvailabilityRange({ kind: 'relative', range: { ...LAST_HOUR }})
    await wrapper.setProps({ nodeId: '202', offset: 4 })
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(2)
    expect(getTimeline).toHaveBeenCalledTimes(2)

    const [id, start, end, page] = rosterCalls()[1]
    expect(id).toBe('202')
    expect(Number(end) - Number(start)).toBe(HOUR)
    expect(page).toEqual({ limit: 4, offset: 4, withServices: true })
  })

  it('drops a superseded response', async () => {
    const first = deferred<NodeOutageTimeline>()
    getTimeline.mockReturnValueOnce(first.promise as any)
    getTimeline.mockResolvedValueOnce(timelineDoc({
      outage: [{
        id: 9999, ifServiceId: 11, ipInterfaceId: 1, ipAddress: '192.168.1.1',
        serviceId: 2, serviceName: 'SNMP',
        ifLostService: NOW - 30 * MINUTE, ifRegainedService: NOW - 20 * MINUTE
      }]
    }))
    getAvailability.mockResolvedValueOnce(availabilityDoc({ availability: 11 }))
    getAvailability.mockResolvedValueOnce(availabilityDoc({ availability: 22 }))

    wrapper = mountHost()
    result.onUpdateTime(lastHourTime())
    await flushPromises()

    // The first request now lands, out of order.
    first.resolve(timelineDoc())
    await flushPromises()

    expect(result.model.value?.availability).toBe(22)
    expect(result.timeline.value?.outage.map(o => o.id)).toEqual([9999])
    expect(result.rangeLabel.value).toBe('Last hour')
    // Every start() got its stop(), the superseded one included.
    expect(result.isFetching.value).toBe(false)
  })

  /*
   * A page change while a full fetch is in flight cannot be a roster-only fetch: the window it
   * would have to use is the one the full fetch has not yet set. It becomes a full fetch instead,
   * which supersedes the one in flight.
   */
  it('turns a page change during a full fetch into a full fetch', async () => {
    const firstRoster = deferred<NodeAvailability>()
    const firstTimeline = deferred<NodeOutageTimeline>()
    getAvailability.mockReturnValueOnce(firstRoster.promise as any)
    getTimeline.mockReturnValueOnce(firstTimeline.promise as any)
    getAvailability.mockResolvedValueOnce(availabilityDoc({ availability: 44 }))

    wrapper = mountHost()
    await nextTick()
    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    expect(getAvailability).toHaveBeenCalledTimes(2)
    expect(getTimeline).toHaveBeenCalledTimes(2)
    expect(rosterCalls()[1][3]).toEqual({ limit: 4, offset: 4, withServices: true })
    expect(rosterCalls()[1].slice(1, 3)).toEqual(timelineCalls()[1].slice(1, 3))
    expect(result.model.value?.availability).toBe(44)

    // The superseded full fetch lands late and is dropped.
    firstRoster.resolve(availabilityDoc({ availability: 1 }))
    firstTimeline.resolve(timelineDoc())
    await flushPromises()

    expect(result.model.value?.availability).toBe(44)
    expect(result.isFetching.value).toBe(false)
  })

  // A page response must not overwrite the result of a later node or range change.
  it('drops a page response superseded by a full fetch', async () => {
    wrapper = mountHost()
    await flushPromises()

    const page = deferred<NodeAvailability>()
    getAvailability.mockReturnValueOnce(page.promise as any)
    await wrapper.setProps({ offset: 4 })
    await nextTick()

    getAvailability.mockResolvedValueOnce(availabilityDoc({ availability: 55 }))
    result.onUpdateTime(lastHourTime())
    await flushPromises()

    expect(result.model.value?.availability).toBe(55)

    page.resolve(availabilityDoc({ availability: 1 }))
    await flushPromises()

    expect(result.model.value?.availability).toBe(55)
    expect(result.rangeLabel.value).toBe('Last hour')
    expect(result.isFetching.value).toBe(false)
  })

  it('either failure sets loadFailed', async () => {
    getTimeline.mockResolvedValueOnce(null)
    wrapper = mountHost()
    await flushPromises()

    expect(result.loadFailed.value).toBe(true)
    expect(result.model.value).toBeUndefined()
    wrapper.unmount()

    getAvailability.mockResolvedValueOnce(false)
    wrapper = mountHost()
    await flushPromises()

    expect(result.loadFailed.value).toBe(true)
    expect(result.model.value).toBeUndefined()
    wrapper.unmount()

    // A failed page fetch clears the model too.
    wrapper = mountHost()
    await flushPromises()
    expect(result.model.value).toBeDefined()

    getAvailability.mockResolvedValueOnce(false)
    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    expect(result.loadFailed.value).toBe(true)
    expect(result.model.value).toBeUndefined()
  })

  // With nothing cached for the node and range, a roster alone would be joined to no outages.
  it('refetches both on a page change after a failed load', async () => {
    getTimeline.mockResolvedValueOnce(null)
    wrapper = mountHost()
    await flushPromises()
    expect(result.loadFailed.value).toBe(true)

    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    expect(getTimeline).toHaveBeenCalledTimes(2)
    expect(result.loadFailed.value).toBe(false)
    expect(result.model.value).toBeDefined()
  })

  // fail() clears the cached outages, so the page after a failed roster-only fetch cannot be joined
  // to them: the outages are fetched again with it.
  it('a failed page fetch makes the next page change a full fetch', async () => {
    wrapper = mountHost()
    await flushPromises()
    expect(getTimeline).toHaveBeenCalledTimes(1)

    getAvailability.mockResolvedValueOnce(false)
    await wrapper.setProps({ offset: 4 })
    await flushPromises()

    expect(getTimeline).toHaveBeenCalledTimes(1)
    expect(result.loadFailed.value).toBe(true)

    await wrapper.setProps({ offset: 8 })
    await flushPromises()

    expect(getTimeline).toHaveBeenCalledTimes(2)
    expect(rosterCalls().at(-1)?.[3]).toEqual({ limit: 4, offset: 8, withServices: true })
    expect(result.loadFailed.value).toBe(false)
    expect(result.model.value).toBeDefined()
  })

  it('totals come from ipinterfaceCount and service-count', async () => {
    getAvailability.mockResolvedValueOnce(availabilityDoc({ ipinterfaceCount: 12, 'service-count': 30 }))
    wrapper = mountHost()
    await flushPromises()

    expect(result.totalInterfaces.value).toBe(12)
    expect(result.totalServices.value).toBe(30)
  })

  // An older server has no count and sends every interface: count the ones the timeline would show.
  it('without ipinterfaceCount, counts only interfaces with services', async () => {
    getAvailability.mockResolvedValueOnce(availabilityDoc({
      ipinterfaces: [
        { address: '10.0.0.1', availability: 100, id: 1, services: [] },
        { address: '10.0.0.2', availability: 100, id: 2, services: [{ id: 20, name: 'ICMP', serviceId: 1, availability: 100 }] }
      ]
    }))
    wrapper = mountHost()
    await flushPromises()

    expect(result.totalInterfaces.value).toBe(1)
  })

  // The label names the range of the figure on show, not the one just picked.
  it('keeps the range label until the new range has loaded', async () => {
    wrapper = mountHost()
    await flushPromises()

    const second = deferred<NodeAvailability>()
    getAvailability.mockReturnValueOnce(second.promise as any)
    result.onUpdateTime(lastHourTime())
    await flushPromises()

    expect(result.rangeLabel.value).toBe('Last day')

    second.resolve(availabilityDoc({ availability: 12.5 }))
    await flushPromises()

    expect(result.rangeLabel.value).toBe('Last hour')
  })

  it('builds the ticks for the window of the data', async () => {
    wrapper = mountHost()
    await flushPromises()

    expect(result.ticks.value.length).toBeGreaterThan(0)
  })

  /*
   * A relative range means "resolve it when you ask", so a node change asks for a window anchored
   * to the clock at that moment.
   */
  it('re-resolves a relative range against the clock when the node changes', async () => {
    vi.useFakeTimers({ toFake: ['Date'] })
    vi.setSystemTime(NOW)

    wrapper = mountHost()
    await flushPromises()
    const firstEnd = timelineCalls()[0][2] as number

    vi.setSystemTime(NOW + 2 * HOUR)
    await wrapper.setProps({ nodeId: '202' })
    await flushPromises()

    const secondEnd = timelineCalls()[1][2] as number
    expect(secondEnd - firstEnd).toBe(2 * HOUR)
  })

  it('keeps an absolute custom range fixed across a node change', async () => {
    wrapper = mountHost()
    await flushPromises()

    // No `range`, which is what TimeControls emits for a custom absolute window.
    result.onUpdateTime({
      startTime: Math.floor((NOW - 5 * HOUR) / 1000),
      endTime: Math.floor((NOW - 4 * HOUR) / 1000),
      format: 'hours'
    })
    await flushPromises()

    const [, pickedStart, pickedEnd] = timelineCalls()[1]

    await wrapper.setProps({ nodeId: '202' })
    await flushPromises()

    expect(timelineCalls()[2].slice(1)).toEqual([pickedStart, pickedEnd])
    expect(result.rangeLabel.value).toBe('custom range')
  })

  // The range lives in the node store, so a new instance -- the next node's panel -- starts from it.
  it('reuses the chosen range in a new instance', async () => {
    wrapper = mountHost()
    await flushPromises()
    result.onUpdateTime(lastHourTime())
    await flushPromises()
    wrapper.unmount()

    wrapper = mountHost({ nodeId: '202' })
    await flushPromises()

    const [nodeId, start, end] = timelineCalls()[2]
    expect(nodeId).toBe('202')
    expect(end - start).toBe(HOUR)
    expect(result.initialRangeLabel).toBe('Last hour')
    expect(result.rangeLabel.value).toBe('Last hour')
  })
})
