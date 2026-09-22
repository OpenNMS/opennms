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

import { describe, expect, it, vi } from 'vitest'
import {
  buildTimelineModel,
  formatAvailability,
  formatCompactDuration,
  isMonitored,
  segmentDescription,
  segmentFor,
  unmonitoredPercent,
  TimelineServiceRow,
  TimelineWindow
} from '@/components/Nodes/availabilityTimelineModel'
import { NodeAvailability } from '@/types'
import { NodeOutageTimeline, NodeOutageTimelineEntry } from '@/types/nodeAvailabilityTimeline'

vi.mock('@/lib/displayTimeZone', () => ({
  formatInDisplayZone: (v: number) => `@${v}`,
  displayTimeZone: () => 'UTC',
  displayDateFormat: () => 'yyyy-MM-dd'
}))

const HOUR = 3_600_000
const START = 1_700_000_000_000
const WINDOW: TimelineWindow = { start: START, end: START + 24 * HOUR }

const outage = (over: Partial<NodeOutageTimelineEntry> = {}): NodeOutageTimelineEntry => ({
  id: 1,
  ifServiceId: 11,
  ipInterfaceId: 1,
  ipAddress: '192.168.1.1',
  serviceId: 2,
  serviceName: 'SNMP',
  ifLostService: START + 6 * HOUR,
  ifRegainedService: START + 12 * HOUR,
  ...over
})

describe('segmentFor', () => {
  it('places an outage wholly inside the window', () => {
    const seg = segmentFor(outage(), WINDOW)!
    expect(seg.startPct).toBe(25)
    expect(seg.widthPct).toBe(25)
    expect(seg.startsBeforeWindow).toBe(false)
    expect(seg.openAtWindowEnd).toBe(false)
  })

  it('clamps an outage that began before the window and flags the cut edge', () => {
    const seg = segmentFor(outage({
      ifLostService: START - 12 * HOUR,
      ifRegainedService: START + 6 * HOUR
    }), WINDOW)!

    expect(seg.startPct).toBe(0)
    expect(seg.widthPct).toBe(25)
    expect(seg.startsBeforeWindow).toBe(true)
    // The true start is preserved for the tooltip even though the bar is clamped.
    expect(seg.lost).toBe(START - 12 * HOUR)
  })

  it('runs an unresolved outage to the right edge', () => {
    const seg = segmentFor(outage({
      ifLostService: START + 18 * HOUR,
      ifRegainedService: null
    }), WINDOW)!

    expect(seg.startPct).toBe(75)
    expect(seg.startPct + seg.widthPct).toBe(100)
    expect(seg.openAtWindowEnd).toBe(true)
    expect(seg.regained).toBeNull()
  })

  it('drops an outage entirely before the window', () => {
    expect(segmentFor(outage({
      ifLostService: START - 10 * HOUR,
      ifRegainedService: START - 5 * HOUR
    }), WINDOW)).toBeNull()
  })

  it('drops an outage entirely after the window', () => {
    expect(segmentFor(outage({
      ifLostService: START + 30 * HOUR,
      ifRegainedService: START + 31 * HOUR
    }), WINDOW)).toBeNull()
  })

  // Half-open interval: an outage that ends exactly as the window opens contributes nothing, and
  // should not be drawn as a sliver pinned to the left edge.
  it('drops an outage that ends exactly at the window start', () => {
    expect(segmentFor(outage({
      ifLostService: START - HOUR,
      ifRegainedService: START
    }), WINDOW)).toBeNull()
  })

  it('drops an outage that begins exactly at the window end', () => {
    expect(segmentFor(outage({
      ifLostService: WINDOW.end,
      ifRegainedService: null
    }), WINDOW)).toBeNull()
  })

  it('drops a row whose regained time precedes its lost time rather than emitting a negative width', () => {
    expect(segmentFor(outage({
      ifLostService: START + 12 * HOUR,
      ifRegainedService: START + 6 * HOUR
    }), WINDOW)).toBeNull()
  })

  // The min-width in CSS is what makes this visible, mirroring the one-pixel floor the server
  // used. If a `widthPct > 0` filter is ever added here, a brief outage disappears entirely.
  it('keeps a one-second outage in a one-year window, at effectively zero width', () => {
    const yearWindow = { start: START, end: START + 365 * 24 * HOUR }
    const seg = segmentFor(outage({
      ifLostService: START + 100 * HOUR,
      ifRegainedService: START + 100 * HOUR + 1000
    }), yearWindow)

    expect(seg).not.toBeNull()
    expect(seg!.widthPct).toBeGreaterThan(0)
    expect(seg!.widthPct).toBeLessThan(0.001)
  })

  it('returns null for an empty window rather than dividing by zero', () => {
    expect(segmentFor(outage(), { start: START, end: START })).toBeNull()
  })
})

describe('buildTimelineModel', () => {
  const availability: NodeAvailability = {
    availability: 98.76,
    id: 101,
    'service-count': 2,
    'service-down-count': 0,
    ipinterfaces: [{
      address: '192.168.1.1',
      availability: 99.5,
      id: 1,
      services: [
        { id: 10, name: 'ICMP', serviceId: 1, availability: 100 },
        { id: 11, name: 'SNMP', serviceId: 2, availability: 97 }
      ]
    }]
  }

  const timeline: NodeOutageTimeline = {
    nodeId: 101,
    start: WINDOW.start,
    end: WINDOW.end,
    nodeCreateTime: START - 1000 * HOUR,
    count: 2,
    outage: [
      outage({ id: 2, ifLostService: START + 12 * HOUR, ifRegainedService: START + 13 * HOUR }),
      outage({ id: 1, ifLostService: START + 6 * HOUR, ifRegainedService: START + 7 * HOUR })
    ]
  }

  // The roster drives the rows, not the outages: a healthy service has no outage row and would
  // otherwise vanish from the panel entirely.
  it('renders a row for every service in the roster, including ones with no outages', () => {
    const model = buildTimelineModel(availability, timeline, WINDOW)
    const services = model.interfaces[0].services

    expect(services.map(s => s.serviceName)).toEqual(['ICMP', 'SNMP'])
    expect(services[0].segments).toHaveLength(0)
    expect(services[1].segments).toHaveLength(2)
  })

  // The availability resource returns services in database order, so without sorting the rows
  // shuffle between refreshes of the same node.
  it('orders services by name regardless of the order they arrive in', () => {
    const shuffled: NodeAvailability = {
      ...availability,
      ipinterfaces: [{
        ...availability.ipinterfaces[0],
        services: [
          { id: 11, name: 'SNMP', serviceId: 2, availability: 97 },
          { id: 13, name: 'OpenNMS-DB', serviceId: 3, availability: -1 },
          { id: 10, name: 'ICMP', serviceId: 1, availability: 100 }
        ]
      }]
    }

    expect(buildTimelineModel(shuffled, timeline, WINDOW).interfaces[0].services
      .map(s => s.serviceName)).toEqual(['ICMP', 'OpenNMS-DB', 'SNMP'])
  })

  it('takes percentages from the availability document rather than deriving them', () => {
    const model = buildTimelineModel(availability, timeline, WINDOW)
    expect(model.availability).toBe(98.76)
    expect(model.interfaces[0].availability).toBe(99.5)
    expect(model.interfaces[0].services[1].availability).toBe(97)
  })

  // The endpoint orders newest first; reading order and tab order should run left to right.
  it('sorts segments by lost-service time ascending', () => {
    const model = buildTimelineModel(availability, timeline, WINDOW)
    const segments = model.interfaces[0].services[1].segments
    expect(segments.map(s => s.outageId)).toEqual([1, 2])
  })

  it('renders rows with no bars when the timeline is absent', () => {
    const model = buildTimelineModel(availability, null, WINDOW)
    expect(model.interfaces[0].services.every(s => s.segments.length === 0)).toBe(true)
    expect(model.unmonitoredPct).toBe(0)
  })

  it('assigns segments by monitored service id, not by name or address', () => {
    const model = buildTimelineModel(availability, {
      ...timeline,
      outage: [outage({ id: 5, ifServiceId: 10, serviceName: 'ICMP', serviceId: 1 })]
    }, WINDOW)

    expect(model.interfaces[0].services[0].segments).toHaveLength(1)
    expect(model.interfaces[0].services[1].segments).toHaveLength(0)
  })
})

describe('unmonitoredPercent', () => {
  it('is zero when the node predates the window', () => {
    expect(unmonitoredPercent(START - HOUR, WINDOW)).toBe(0)
  })

  it('covers the leading part of the window for a node created inside it', () => {
    expect(unmonitoredPercent(START + 6 * HOUR, WINDOW)).toBe(25)
  })

  it('is zero when the create time is unknown', () => {
    expect(unmonitoredPercent(undefined, WINDOW)).toBe(0)
  })
})

describe('formatAvailability', () => {
  // Matches the wording the legacy availability box uses for the same rows.
  it('reports the availability resource -1 sentinel as not monitored', () => {
    expect(formatAvailability(-1)).toBe('Not Monitored')
  })

  it('renders a whole hundred plainly', () => {
    expect(formatAvailability(100)).toBe('100%')
  })

  // '100%' has to mean no downtime at all, so a figure short of it must not round up to it.
  it('never rounds up to 100%', () => {
    expect(formatAvailability(99.9999)).not.toBe('100%')
  })

  it('trims trailing zeroes', () => {
    expect(formatAvailability(98.5)).toBe('98.5%')
    expect(formatAvailability(98.76)).toBe('98.76%')
  })
})

describe('formatCompactDuration', () => {
  it('formats seconds, minutes, hours and days compactly', () => {
    expect(formatCompactDuration(41_000)).toBe('41s')
    expect(formatCompactDuration(17 * 60_000)).toBe('17m')
    expect(formatCompactDuration(2 * HOUR + 30 * 60_000)).toBe('2h 30m')
    expect(formatCompactDuration(2 * 24 * HOUR + 4 * HOUR)).toBe('2d 4h')
  })

  it('does not produce a negative duration', () => {
    expect(formatCompactDuration(-5000)).toBe('0s')
  })
})

describe('segmentDescription', () => {
  const row: TimelineServiceRow = {
    ifServiceId: 11,
    serviceId: 2,
    serviceName: 'SNMP',
    ipAddress: '192.168.1.1',
    availability: 97,
    segments: []
  }

  // The tooltip renders these as separate lines: primevue-overrides.scss sets
  // `white-space: pre-line` on .p-tooltip-text. One long run is much harder to read.
  it('puts the identity, the loss and the recovery on their own lines', () => {
    const seg = segmentFor(outage(), WINDOW)!
    const lines = segmentDescription(row, seg).split('\n')

    expect(lines).toHaveLength(3)
    expect(lines[0]).toContain('Outage 1')
    expect(lines[0]).toContain('SNMP')
    expect(lines[0]).toContain('192.168.1.1')
    expect(lines[1]).toContain('Lost')
    expect(lines[2]).toContain('Regained')
  })

  it('reports the duration of a resolved outage', () => {
    const seg = segmentFor(outage(), WINDOW)!
    expect(segmentDescription(row, seg)).toContain('(6h)')
  })

  it('says an unresolved outage is still down instead of naming a recovery', () => {
    const seg = segmentFor(outage({ ifRegainedService: null }), WINDOW)!
    const lines = segmentDescription(row, seg).split('\n')

    expect(lines[2]).toContain('Still down')
    expect(lines[2]).not.toContain('Regained')
  })

  it('marks an outage whose start precedes the window', () => {
    const seg = segmentFor(outage({ ifLostService: START - 5 * HOUR }), WINDOW)!
    expect(segmentDescription(row, seg)).toContain('before this window')
  })
})

describe('isMonitored', () => {
  // The availability resource returns -1 when it could compute no figure: an unmanaged service, an
  // unmanaged interface or an inactive node. Painting such a strip green would claim a full window
  // of uptime for something nothing was watching.
  it('treats the -1 sentinel as unmonitored', () => {
    expect(isMonitored(-1)).toBe(false)
  })

  it('treats a real figure, including zero, as monitored', () => {
    expect(isMonitored(0)).toBe(true)
    expect(isMonitored(66.67)).toBe(true)
    expect(isMonitored(100)).toBe(true)
  })
})
