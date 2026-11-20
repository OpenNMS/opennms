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

import AvailabilityTimelineRow from '@/components/Nodes/AvailabilityTimelineRow.vue'
import { TimelineServiceRow } from '@/components/Nodes/availabilityTimelineModel'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const row = (over: Partial<TimelineServiceRow> = {}): TimelineServiceRow => ({
  ifServiceId: 10,
  serviceId: 1,
  serviceName: 'ICMP',
  ipAddress: '192.168.1.1',
  availability: 99.5,
  segments: [],
  ...over
})

const mountRow = (over: Partial<TimelineServiceRow> = {}) =>
  mount(AvailabilityTimelineRow, {
    props: {
      row: row(over),
      ticks: [],
      baseHref: '/opennms/',
      nodeId: 101,
      unmonitoredPct: 0,
      thresholds: { normal: 99.99, warning: 97 }
    },
    global: {
      directives: { 'onms-tooltip': {}}
    }
  })

const strip = (wrapper: ReturnType<typeof mountRow>) => wrapper.get('[data-test="timeline-strip"]')

describe('AvailabilityTimelineRow.vue', () => {
  it('paints a managed service with a figure as available', () => {
    const wrapper = mountRow({ status: 'A' })

    expect(strip(wrapper).classes()).not.toContain('strip--unmonitored')
  })

  it('does not paint a service with no figure as available', () => {
    const wrapper = mountRow({ status: 'A', availability: -1 })

    expect(strip(wrapper).classes()).toContain('strip--unmonitored')
  })

  // The strip, the text and the badge agree: a stale figure on a service that is not managed must
  // not draw a green strip beside its 'Forced Unmanaged' label.
  it('does not paint a service that is not managed as available, whatever its figure', () => {
    const wrapper = mountRow({ status: 'F', availability: 99.5 })

    expect(strip(wrapper).classes()).toContain('strip--unmonitored')
    expect(wrapper.text()).toContain('Forced Unmanaged')
  })
})
