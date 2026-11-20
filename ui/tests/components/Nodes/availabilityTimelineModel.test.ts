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
  availabilityClass,
  buildTimelineModel,
  formatAvailability,
  formatCompactDuration,
  interfaceAvailabilityClass,
  interfaceAvailabilityText,
  isMonitored,
  isServiceMonitored,
  limitTimelineRows,
  nodeAvailabilityClass,
  nodeAvailabilityText,
  segmentDescription,
  segmentFor,
  serviceAvailabilityClass,
  serviceAvailabilityText,
  unmonitoredPercent,
  TimelineInterfaceGroup,
  TimelineModel,
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

describe('buildTimelineModel, interfaces with no monitored services', () => {
  // The availability resource reports 100 for an interface with no managed services, because the
  // stored procedure divides by a zero total service time. Rendering that as an interface row
  // claims a full window of uptime for something with nothing under it, and it also kept the
  // panel's empty state from ever appearing.
  const withEmptyInterface: NodeAvailability = {
    availability: 100,
    id: 101,
    'service-count': 1,
    'service-down-count': 0,
    ipinterfaces: [
      {
        address: '192.168.1.1', availability: 100, id: 1,
        services: [{ id: 10, name: 'ICMP', serviceId: 1, availability: 100 }]
      },
      { address: '192.168.1.9', availability: 100, id: 2, services: [] }
    ]
  }

  it('drops an interface that has no services', () => {
    const model = buildTimelineModel(withEmptyInterface, null, WINDOW)

    expect(model.interfaces.map(i => i.ipAddress)).toEqual(['192.168.1.1'])
  })

  it('leaves no interfaces at all when none has a service', () => {
    const model = buildTimelineModel({
      ...withEmptyInterface,
      ipinterfaces: [{ address: '192.168.1.9', availability: 100, id: 2, services: [] }]
    }, null, WINDOW)

    expect(model.interfaces).toEqual([])
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

  // Measured to the end of the window, not to the clock: the old form froze at whatever time the
  // model happened to be built, and on an absolute window the clock is not the right reference.
  it('measures an open outage to the end of the window', () => {
    const seg = segmentFor(outage({
      ifLostService: START + 6 * HOUR,
      ifRegainedService: null
    }), WINDOW)!

    expect(seg.durationMs).toBe(18 * HOUR)
    expect(segmentDescription(row, seg)).toContain('18h')
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

describe('availability text for a level with nothing monitored', () => {
  /*
   * The stored procedures return 100 when there is no managed service time -- they divide by a zero
   * total -- so passing the figure straight through printed '100%' above 'No monitored services on
   * this node', and above interface rows that all read 'Not Monitored'. The legacy box printed
   * 'Unmanaged' at node level instead, because the value it reads is -1 in that situation.
   */
  const service = (availability: number) => ({
    ifServiceId: 1, serviceId: 1, serviceName: 'ICMP', ipAddress: '10.0.0.1',
    availability, segments: []
  })
  const group = (...avails: number[]) => ({
    ipInterfaceId: 1, ipAddress: '10.0.0.1', availability: 100,
    monitored: avails.some(a => a >= 0),
    services: avails.map(service)
  })

  it('calls a node with nothing monitored unmanaged rather than 100%', () => {
    expect(nodeAvailabilityText({
      window: WINDOW, availability: 100, unmonitoredPct: 0, monitored: false, interfaces: []
    })).toBe('Unmanaged')
  })

  it('reports the real figure for a node that has something monitored', () => {
    expect(nodeAvailabilityText({
      window: WINDOW, availability: 66.666, unmonitoredPct: 0, monitored: true,
      interfaces: [group(100)]
    })).toBe('66.666%')
  })

  it('calls an interface whose services are all unmonitored not monitored', () => {
    expect(interfaceAvailabilityText(group(-1, -1))).toBe('Not Monitored')
  })

  it('reports the real figure for an interface with at least one monitored service', () => {
    expect(interfaceAvailabilityText({ ...group(-1, 100), availability: 99.5 })).toBe('99.5%')
  })
})

describe('buildTimelineModel monitored flags', () => {
  const doc = (services: { id: number; name: string; serviceId: number; availability: number }[]): NodeAvailability => ({
    availability: 100,
    id: 101,
    'service-count': services.length,
    'service-down-count': 0,
    ipinterfaces: [{ address: '10.0.0.1', availability: 100, id: 1, services }]
  })

  it('marks a node and interface unmonitored when every service is', () => {
    const model = buildTimelineModel(doc([
      { id: 10, name: 'ICMP', serviceId: 1, availability: -1 },
      { id: 11, name: 'SNMP', serviceId: 2, availability: -1 }
    ]), null, WINDOW)

    expect(model.monitored).toBe(false)
    expect(model.interfaces[0].monitored).toBe(false)
  })

  it('marks them monitored when at least one service has a figure', () => {
    const model = buildTimelineModel(doc([
      { id: 10, name: 'ICMP', serviceId: 1, availability: -1 },
      { id: 11, name: 'SNMP', serviceId: 2, availability: 0 }
    ]), null, WINDOW)

    expect(model.monitored).toBe(true)
    expect(model.interfaces[0].monitored).toBe(true)
  })

  it('marks a node with no interfaces left unmonitored', () => {
    expect(buildTimelineModel(doc([]), null, WINDOW).monitored).toBe(false)
  })
})

describe('limitTimelineRows', () => {
  const group = (id: number, services: number): TimelineInterfaceGroup => ({
    ipInterfaceId: id,
    ipAddress: `10.0.0.${id}`,
    availability: 100,
    monitored: true,
    services: Array.from({ length: services }, (_, i) => ({
      ifServiceId: id * 100 + i,
      serviceId: i,
      serviceName: `SVC${i}`,
      ipAddress: `10.0.0.${id}`,
      availability: 100,
      segments: []
    }))
  })

  const model = (...services: number[]): TimelineModel => ({
    window: { start: 0, end: 1 },
    availability: 99,
    monitored: true,
    interfaces: services.map((count, i) => group(i + 1, count)),
    unmonitoredPct: 0
  })

  // Services kept per interface.
  const shape = (m: TimelineModel) => m.interfaces.map(g => g.services.length)

  it.each([
    ['keeps a model that fits', [2, 3], [2, 3]],
    ['keeps exactly 8 rows', [3, 3], [3, 3]],
    ['cuts the services of the interface that runs over', [3, 5], [3, 3]],
    ['cuts one interface with too many services', [12], [7]],
    ['leaves out an interface that would fit only its heading', [2, 3, 2], [2, 3]],
    ['shows a heading with the one service that fits', [2, 2, 2], [2, 2, 1]],
    ['leaves out every interface after the limit', [1, 1, 1, 1, 1], [1, 1, 1, 1]]
  ])('%s', (_title, services, expected) => {
    expect(shape(limitTimelineRows(model(...services), 8))).toEqual(expected)
  })

  // The cut interface's heading keeps the figure for all its services, though only some show.
  it('keeps the heading figure of an interface whose services are cut', () => {
    const input = model(5, 5)
    input.interfaces[1].availability = 87.5
    const limited = limitTimelineRows(input, 8)

    expect(shape(limited)).toEqual([5, 1])
    expect(limited.interfaces[1].availability).toBe(87.5)
    expect(limited.interfaces[1].services.map(s => s.serviceName)).toEqual(['SVC0'])
  })

  it('keeps the node\'s own figures', () => {
    const limited = limitTimelineRows(model(12), 8)

    expect(limited.availability).toBe(99)
    expect(limited.monitored).toBe(true)
  })
})

describe('availability status and thresholds', () => {
  const thresholds = { normal: 99.99, warning: 97 }

  const doc = (over: Partial<NodeAvailability> = {}, serviceOver: Record<string, unknown> = {}): NodeAvailability => ({
    id: 1,
    availability: 99,
    'service-count': 1,
    'service-down-count': 0,
    ipinterfaces: [{
      id: 5,
      address: '10.0.0.1',
      availability: 99,
      services: [{ id: 11, name: 'ICMP', serviceId: 1, availability: 99, ...serviceOver }]
    }],
    ...over
  })

  const row = (over: Partial<TimelineServiceRow> = {}): TimelineServiceRow => ({
    ifServiceId: 11,
    serviceId: 1,
    serviceName: 'ICMP',
    ipAddress: '10.0.0.1',
    availability: 99.5,
    segments: [],
    ...over
  })

  describe('availabilityClass', () => {
    it.each([
      [99.99, 'normal'],
      [99.989, 'warning'],
      [97, 'warning'],
      [96.99, 'critical']
    ])('classes %s as %s', (value, expected) => {
      expect(availabilityClass(value, thresholds)).toBe(expected)
    })

    it('has no class for a figure that could not be computed', () => {
      expect(availabilityClass(-1, thresholds)).toBeUndefined()
    })

    it('has no class without thresholds', () => {
      expect(availabilityClass(99.5)).toBeUndefined()
    })
  })

  describe('node monitored', () => {
    it('is false for a managed service count of 0, whatever the page holds', () => {
      const model = buildTimelineModel(doc({ 'managed-service-count': 0 }), null, WINDOW)
      expect(model.monitored).toBe(false)
      expect(nodeAvailabilityText(model)).toBe('Unmanaged')
    })

    it('is true for a positive count even when the page has no figures', () => {
      const model = buildTimelineModel(doc({ 'managed-service-count': 3 }, { availability: -1 }), null, WINDOW)
      expect(model.monitored).toBe(true)
    })

    it('falls back to the page when the count is absent', () => {
      expect(buildTimelineModel(doc(), null, WINDOW).monitored).toBe(true)
      expect(buildTimelineModel(doc({}, { availability: -1 }), null, WINDOW).monitored).toBe(false)
    })
  })

  describe('thresholds', () => {
    it('are set when both are present', () => {
      const model = buildTimelineModel(doc({ 'normal-threshold': 99.99, 'warning-threshold': 97 }), null, WINDOW)
      expect(model.thresholds).toEqual(thresholds)
    })

    it.each([
      [{ 'normal-threshold': 99.99 }],
      [{ 'warning-threshold': 97 }]
    ])('are absent when one is missing (%o)', (over) => {
      expect(buildTimelineModel(doc(over), null, WINDOW).thresholds).toBeUndefined()
    })
  })

  describe('service status', () => {
    it('is copied into each row', () => {
      const model = buildTimelineModel(doc({}, { status: 'F', availability: -1 }), null, WINDOW)
      expect(model.interfaces[0]!.services[0]!.status).toBe('F')
    })

    it('reads a non-managed status as its label', () => {
      expect(serviceAvailabilityText(row({ availability: -1, status: 'F' }))).toBe('Forced Unmanaged')
    })

    it('reads no status and no figure as Not Monitored', () => {
      expect(serviceAvailabilityText(row({ availability: -1 }))).toBe('Not Monitored')
    })

    it('reads a managed service as its figure', () => {
      expect(serviceAvailabilityText(row({ status: 'A' }))).toBe('99.5%')
    })

    // The availability resource's -1: managed, but no figure could be computed.
    it('reads a managed service with no figure as Not Monitored, with no class', () => {
      const managedNoFigure = row({ status: 'A', availability: -1 })

      expect(serviceAvailabilityText(managedNoFigure)).toBe('Not Monitored')
      expect(serviceAvailabilityClass(managedNoFigure, thresholds)).toBeUndefined()
    })

    it('names an undefined code', () => {
      expect(serviceAvailabilityText(row({ availability: -1, status: 'Q' }))).toBe('Unknown (Q)')
    })

    it('is monitored only with a figure and a status that is absent or A', () => {
      expect(isServiceMonitored(row())).toBe(true)
      expect(isServiceMonitored(row({ status: 'A' }))).toBe(true)
      expect(isServiceMonitored(row({ status: 'F' }))).toBe(false)
      expect(isServiceMonitored(row({ availability: -1 }))).toBe(false)
    })
  })

  // The JSP showed an interface that is not managed as one row with its status, for example
  // 'Forced Unmanaged'. Its figure is -1 then, which alone would read 'Not Monitored'.
  describe('interface is-managed', () => {
    const groupFor = (ifaceOver: Record<string, unknown>, serviceOver: Record<string, unknown> = {}) => {
      const ipinterfaces = doc({}, serviceOver).ipinterfaces.map(iface => ({ ...iface, ...ifaceOver }))
      return buildTimelineModel(doc({ ipinterfaces }), null, WINDOW).interfaces[0]!
    }

    it('is copied into the group', () => {
      expect(groupFor({ 'is-managed': 'F' }).isManaged).toBe('F')
    })

    it.each([
      ['F', 'Forced Unmanaged'],
      ['U', 'Unmanaged'],
      ['D', 'Deleted'],
      ['Q', 'Unknown (Q)']
    ])('reads an interface with code %s as %s, with no class', (code, label) => {
      const group = groupFor({ 'is-managed': code })

      expect(interfaceAvailabilityText(group)).toBe(label)
      expect(interfaceAvailabilityClass(group, thresholds)).toBeUndefined()
    })

    it('reads a managed interface as its figure', () => {
      const group = groupFor({ 'is-managed': 'M' })

      expect(interfaceAvailabilityText(group)).toBe('99%')
      expect(interfaceAvailabilityClass(group, thresholds)).toBe('warning')
    })

    it('reads a managed interface with nothing monitored as Not Monitored', () => {
      const group = groupFor({ 'is-managed': 'M' }, { availability: -1 })

      expect(interfaceAvailabilityText(group)).toBe('Not Monitored')
    })

    // null from the JSON provider, absent from an older server: the figure alone decides, as before.
    it.each([
      ['null', { 'is-managed': null }],
      ['absent', {}]
    ])('lets the figure decide when the code is %s', (_, ifaceOver) => {
      const group = groupFor(ifaceOver)

      expect(group.isManaged).toBeUndefined()
      expect(interfaceAvailabilityText(group)).toBe('99%')
      expect(interfaceAvailabilityClass(group, thresholds)).toBe('warning')
    })
  })

  describe('class functions', () => {
    it('class a figure that is shown as a percentage', () => {
      const model = buildTimelineModel(doc({ 'normal-threshold': 99.99, 'warning-threshold': 97 }), null, WINDOW)
      expect(serviceAvailabilityClass(row({ availability: 98 }), thresholds)).toBe('warning')
      expect(interfaceAvailabilityClass(model.interfaces[0]!, thresholds)).toBe('warning')
      expect(nodeAvailabilityClass(model)).toBe('warning')
    })

    it('have no class where the text is not a percentage', () => {
      const unmanaged = row({ availability: 99.5, status: 'U' })
      expect(serviceAvailabilityClass(unmanaged, thresholds)).toBeUndefined()

      const model = buildTimelineModel(
        doc({ 'managed-service-count': 0, 'normal-threshold': 99.99, 'warning-threshold': 97 }), null, WINDOW)
      expect(nodeAvailabilityClass(model)).toBeUndefined()
      expect(interfaceAvailabilityClass({ ...model.interfaces[0]!, monitored: false }, thresholds)).toBeUndefined()
    })
  })

  // The v1 JSON provider writes an unset attribute as null rather than leaving it out, so an
  // undefined category sends null thresholds. `value >= null` is true, which made every badge normal.
  describe('null fields, as the JSON provider sends them', () => {
    const nulls = {
      'normal-threshold': null,
      'warning-threshold': null,
      'managed-service-count': null
    }

    it('builds a model with no thresholds and no classes', () => {
      const model = buildTimelineModel(doc(nulls), null, WINDOW)
      const group = model.interfaces[0]!
      const service = group.services[0]!

      expect(model.thresholds).toBeUndefined()
      expect(nodeAvailabilityClass(model)).toBeUndefined()
      expect(interfaceAvailabilityClass(group, model.thresholds)).toBeUndefined()
      expect(serviceAvailabilityClass(service, model.thresholds)).toBeUndefined()
    })

    it('has no thresholds when only one of them is null', () => {
      expect(buildTimelineModel(doc({ 'normal-threshold': 99.99, 'warning-threshold': null }), null, WINDOW).thresholds).toBeUndefined()
      expect(buildTimelineModel(doc({ 'normal-threshold': null, 'warning-threshold': 97 }), null, WINDOW).thresholds).toBeUndefined()
    })

    it('decides whether the node is monitored from the page when the count is null', () => {
      expect(buildTimelineModel(doc(nulls), null, WINDOW).monitored).toBe(true)
      expect(buildTimelineModel(doc(nulls, { availability: -1 }), null, WINDOW).monitored).toBe(false)
    })

    it('reads a null service status as no status', () => {
      const model = buildTimelineModel(doc({}, { status: null }), null, WINDOW)
      const service = model.interfaces[0]!.services[0]!

      expect(isServiceMonitored(service)).toBe(true)
      expect(serviceAvailabilityText(service)).toBe('99%')
    })
  })

  describe('older server without status, thresholds or count', () => {
    it('builds a model with no thresholds and no classes', () => {
      const model = buildTimelineModel(doc(), null, WINDOW)
      const group = model.interfaces[0]!
      const service = group.services[0]!

      expect(model.thresholds).toBeUndefined()
      expect(service.status).toBeUndefined()
      expect(nodeAvailabilityClass(model)).toBeUndefined()
      expect(interfaceAvailabilityClass(group, model.thresholds)).toBeUndefined()
      expect(serviceAvailabilityClass(service, model.thresholds)).toBeUndefined()
    })
  })
})
