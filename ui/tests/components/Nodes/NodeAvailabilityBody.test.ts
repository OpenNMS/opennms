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

import NodeAvailabilityBody from '@/components/Nodes/NodeAvailabilityBody.vue'
import { TimelineModel } from '@/components/Nodes/availabilityTimelineModel'
import { NodeOutageTimeline } from '@/types/nodeAvailabilityTimeline'
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'

const WINDOW = { start: 1_700_000_000_000, end: 1_700_086_400_000 }

const model = (over: Partial<TimelineModel> = {}): TimelineModel => ({
  window: WINDOW,
  availability: 98.5,
  monitored: true,
  thresholds: { normal: 99.99, warning: 97 },
  unmonitoredPct: 0,
  interfaces: [{
    ipInterfaceId: 1,
    ipAddress: '192.168.1.1',
    availability: 98.5,
    monitored: true,
    services: [{
      ifServiceId: 10,
      serviceId: 1,
      serviceName: 'ICMP',
      ipAddress: '192.168.1.1',
      availability: 98.5,
      segments: []
    }]
  }],
  ...over
})

const timeline = (over: Partial<NodeOutageTimeline> = {}): NodeOutageTimeline => ({
  nodeId: 101,
  start: WINDOW.start,
  end: WINDOW.end,
  nodeCreateTime: WINDOW.start - 1,
  count: 0,
  outage: [],
  ...over
})

const baseProps = {
  model: model(),
  ticks: [],
  timeline: timeline() as NodeOutageTimeline | null,
  rangeLabel: 'Last day',
  baseHref: '/opennms/',
  nodeId: 101,
  showSpinner: false,
  isFetching: false,
  loadFailed: false,
  hasLoaded: true,
  totalInterfaces: 1
}

const mountBody = (props: Partial<typeof baseProps> & { model?: TimelineModel } = {}) =>
  mount(NodeAvailabilityBody, {
    props: { ...baseProps, ...props },
    slots: {
      'after-timeline': '<p data-test="after-timeline-slot">after</p>',
      footer: '<nav data-test="footer-slot">footer</nav>'
    },
    global: {
      plugins: [PrimeVue],
      directives: { 'onms-tooltip': {}}
    }
  })

const exists = (wrapper: ReturnType<typeof mountBody>, dataTest: string) =>
  wrapper.find(`[data-test="${dataTest}"]`).exists()

describe('NodeAvailabilityBody.vue', () => {
  it('names the range and shows the node figure with its level', () => {
    const wrapper = mountBody()
    const badge = wrapper.get('[data-test="node-availability"]')

    expect(wrapper.text()).toContain('Availability (last day)')
    expect(badge.text()).toBe('98.5%')
    expect(badge.attributes('data-level')).toBe('warning')
  })

  it('shows a placeholder and no level without a model', () => {
    const badge = mountBody({ model: undefined, hasLoaded: false }).get('[data-test="node-availability"]')

    expect(badge.text()).toBe('—')
    expect(badge.attributes('data-level')).toBeUndefined()
  })

  it('shows the timeline, then the after-timeline slot, then the footer slot', () => {
    const wrapper = mountBody()

    expect(exists(wrapper, 'availability-timeline')).toBe(true)
    expect(exists(wrapper, 'after-timeline-slot')).toBe(true)
    expect(exists(wrapper, 'footer-slot')).toBe(true)
    expect(wrapper.html().indexOf('availability-timeline')).toBeLessThan(wrapper.html().indexOf('after-timeline-slot'))
    expect(wrapper.html().indexOf('after-timeline-slot')).toBeLessThan(wrapper.html().indexOf('footer-slot'))
  })

  it('says how many outages it drew when the server truncated them', () => {
    const wrapper = mountBody({ timeline: timeline({ truncated: true, count: 500 }) })

    expect(wrapper.get('[data-test="availability-truncated"]').text())
      .toBe('Showing the 500 most recent outages. Some are not drawn.')
  })

  it('shows the error state, without the timeline or the after-timeline slot, when the load failed', () => {
    const wrapper = mountBody({ model: undefined, timeline: null, loadFailed: true })

    expect(exists(wrapper, 'availability-error')).toBe(true)
    expect(wrapper.text()).toContain('Could not load availability for this node.')
    expect(exists(wrapper, 'availability-timeline')).toBe(false)
    expect(exists(wrapper, 'after-timeline-slot')).toBe(false)
    expect(exists(wrapper, 'footer-slot')).toBe(true)
  })

  it('shows the empty state for a node with no monitored interfaces', () => {
    const wrapper = mountBody({ model: model({ interfaces: [] }), totalInterfaces: 0 })

    expect(exists(wrapper, 'availability-empty')).toBe(true)
    expect(wrapper.text()).toContain('No monitored services on this node.')
    expect(exists(wrapper, 'availability-timeline')).toBe(false)
  })

  it('claims nothing before a fetch has completed', () => {
    const wrapper = mountBody({ model: undefined, timeline: null, hasLoaded: false, totalInterfaces: 0 })

    expect(exists(wrapper, 'availability-empty')).toBe(false)
    expect(exists(wrapper, 'availability-error')).toBe(false)
    expect(exists(wrapper, 'availability-timeline')).toBe(false)
  })

  // The footer stays put while a page is fetched; the states do not show stale content meanwhile.
  it('shows only the footer while fetching, and the spinner once it is due', () => {
    const fetching = mountBody({ isFetching: true })

    expect(exists(fetching, 'availability-timeline')).toBe(false)
    expect(fetching.findComponent({ name: 'OnmsSpinner' }).exists()).toBe(false)
    expect(exists(fetching, 'footer-slot')).toBe(true)

    const spinning = mountBody({ isFetching: true, showSpinner: true })

    expect(spinning.findComponent({ name: 'OnmsSpinner' }).exists()).toBe(true)
    expect(exists(spinning, 'availability-timeline')).toBe(false)
    expect(exists(spinning, 'footer-slot')).toBe(true)
  })
})
