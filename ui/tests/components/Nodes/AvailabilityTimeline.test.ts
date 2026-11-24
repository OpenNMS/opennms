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

import AvailabilityTimeline from '@/components/Nodes/AvailabilityTimeline.vue'
import { buildTimelineModel } from '@/components/Nodes/availabilityTimelineModel'
import { NodeAvailability } from '@/types'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const WINDOW = { start: 1_700_000_000_000, end: 1_700_086_400_000 }

const availability: NodeAvailability = {
  availability: 98.76,
  id: 101,
  'service-count': 2,
  'service-down-count': 0,
  'managed-service-count': 2,
  'normal-threshold': 99.99,
  'warning-threshold': 97,
  ipinterfaces: [{
    address: '192.168.1.1',
    availability: 99.5,
    id: 1,
    services: [
      { id: 10, name: 'ICMP', serviceId: 1, availability: 99.995 },
      { id: 11, name: 'SNMP', serviceId: 2, availability: 96 }
    ]
  }]
}

const mountTimeline = () =>
  mount(AvailabilityTimeline, {
    props: {
      model: buildTimelineModel(availability, null, WINDOW),
      ticks: [],
      baseHref: '/opennms/',
      nodeId: 101,
      rangeLabel: 'Last day'
    }
  })

describe('AvailabilityTimeline.vue', () => {
  // 99.5 is below the normal threshold (99.99) and at or above the warning one (97).
  it('classes the interface figure by the node\'s thresholds', () => {
    const badge = mountTimeline().get('.interface-row [data-test="availability-badge"]')

    expect(badge.text()).toBe('99.5%')
    expect(badge.attributes('data-level')).toBe('warning')
  })

  it('classes each service figure by the same thresholds', () => {
    const levels = mountTimeline().findAll('[data-test="timeline-service-row"] [data-test="availability-badge"]')
      .map(badge => badge.attributes('data-level'))

    expect(levels).toEqual(['normal', 'critical'])
  })
})
