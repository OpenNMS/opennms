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

import { NodeAvailability } from '@/types'
import { NodeOutageTimeline, NodeOutageTimelineEntry } from '@/types/nodeAvailabilityTimeline'
import { formatInDisplayZone } from '@/lib/displayTimeZone'

/** The window the panel is showing, in epoch milliseconds. */
export interface TimelineWindow {
  start: number
  end: number
}

/** One outage, already clamped to the window and expressed as percentages of it. */
export interface TimelineSegment {
  outageId: number
  /** Distance from the left edge of the strip, 0-100. */
  startPct: number
  /** Width as a percentage of the strip. May be 0 for an outage shorter than the strip resolves. */
  widthPct: number
  lost: number
  regained: number | null
  /** The outage began before the window, so its left edge is a cut rather than a start. */
  startsBeforeWindow: boolean
  /** Still down when the window ends, so its right edge is a cut rather than an end. */
  openAtWindowEnd: boolean
}

export interface TimelineServiceRow {
  ifServiceId: number
  serviceId: number
  serviceName: string
  ipAddress: string
  /** From the availability resource, not derived from the segments. */
  availability: number
  segments: TimelineSegment[]
}

export interface TimelineInterfaceGroup {
  ipInterfaceId: number
  ipAddress: string
  availability: number
  services: TimelineServiceRow[]
}

export interface TimelineModel {
  window: TimelineWindow
  availability: number
  interfaces: TimelineInterfaceGroup[]
  /**
   * Width of the leading band in which the node did not yet exist, 0-100. Zero whenever the node
   * was provisioned before the window, which is the usual case.
   */
  unmonitoredPct: number
}

/**
 * Place one outage on the strip, or return null when it does not overlap the window.
 *
 * The interval is treated as half-open, [lost, regained): an outage that ended exactly when the
 * window opened, or began exactly as it closed, contributes nothing and is dropped rather than
 * drawn as a zero-width sliver at the very edge.
 *
 * An unresolved outage runs to the right edge, which is what the server-rendered strip did --
 * drawOutage defaulted its end to the window end.
 */
export const segmentFor = (
  outage: NodeOutageTimelineEntry,
  window: TimelineWindow
): TimelineSegment | null => {
  const span = window.end - window.start

  if (span <= 0) {
    return null
  }

  const lost = outage.ifLostService
  const regained = outage.ifRegainedService ?? window.end

  // A row whose regained time precedes its lost time is corrupt; drawing it would produce a
  // negative width that CSS would silently render as zero at the wrong offset.
  if (regained < lost) {
    return null
  }

  if (regained <= window.start || lost >= window.end) {
    return null
  }

  const visibleStart = Math.max(lost, window.start)
  const visibleEnd = Math.min(regained, window.end)

  return {
    outageId: outage.id,
    startPct: ((visibleStart - window.start) / span) * 100,
    // Deliberately not filtered on being greater than zero: a very short outage is kept and made
    // visible by a min-width in CSS, the same way the PNG used a one-pixel floor.
    widthPct: ((visibleEnd - visibleStart) / span) * 100,
    lost,
    regained: outage.ifRegainedService,
    startsBeforeWindow: lost < window.start,
    openAtWindowEnd: outage.ifRegainedService === null || outage.ifRegainedService > window.end
  }
}

/**
 * Join the availability document (the service roster and the percentages) with the outage
 * timeline (the bars). The roster drives the rows: a service with no outages is a healthy row, and
 * would be invisible if the outages drove them.
 */
export const buildTimelineModel = (
  availability: NodeAvailability,
  timeline: NodeOutageTimeline | null,
  window: TimelineWindow
): TimelineModel => {
  const segmentsByService = new Map<number, TimelineSegment[]>()

  for (const outage of timeline?.outage ?? []) {
    const segment = segmentFor(outage, window)

    if (!segment) {
      continue
    }

    const existing = segmentsByService.get(outage.ifServiceId)

    if (existing) {
      existing.push(segment)
    } else {
      segmentsByService.set(outage.ifServiceId, [segment])
    }
  }

  for (const segments of segmentsByService.values()) {
    // The endpoint orders by lost-service time descending; left to right reads better and makes
    // the DOM order match the visual order for anyone tabbing through.
    segments.sort((a, b) => a.lost - b.lost)
  }

  const interfaces: TimelineInterfaceGroup[] = (availability.ipinterfaces ?? [])
    // An interface with no monitored services has no availability to report. The resource still
    // gives it a figure -- 100, because the stored procedure divides by a zero total service time
    // -- so keeping it would put a row claiming a full window of uptime above nothing at all, and
    // would stop the panel's empty state ever appearing. The previous panel skipped these too.
    .filter(iface => (iface.services ?? []).length > 0)
    .map(iface => ({
      ipInterfaceId: iface.id,
      ipAddress: iface.address,
      availability: iface.availability,
      // Sorted by name: the availability resource returns services in database order, so without
      // this the rows shuffle between refreshes of the same node. The legacy page sorts them too.
      services: (iface.services ?? [])
        .map(service => ({
          ifServiceId: service.id,
          serviceId: service.serviceId,
          serviceName: service.name,
          ipAddress: iface.address,
          availability: service.availability,
          segments: segmentsByService.get(service.id) ?? []
        }))
        .sort((a, b) => a.serviceName.localeCompare(b.serviceName))
    }))

  return {
    window,
    availability: availability.availability,
    interfaces,
    unmonitoredPct: unmonitoredPercent(timeline?.nodeCreateTime, window)
  }
}

/**
 * How much of the left of the strip predates the node. The server-rendered strip left this region
 * unpainted rather than green, because green meant "monitored", not "up". It matters more now that
 * the window is selectable: over ninety days, a node provisioned yesterday would otherwise read as
 * having been available the whole time.
 */
export const unmonitoredPercent = (
  nodeCreateTime: number | undefined,
  window: TimelineWindow
): number => {
  const span = window.end - window.start

  if (!nodeCreateTime || span <= 0 || nodeCreateTime <= window.start) {
    return 0
  }

  return (Math.min(nodeCreateTime, window.end) - window.start) / span * 100
}

/**
 * True when an availability figure could actually be computed for this row.
 *
 * The availability resource returns -1 when its query matched no row, which it requires to be a
 * managed service, on a managed interface, on an active node. A strip for one of those must not be
 * painted as available: green would claim uptime for something nothing was watching. The legacy
 * page draws an empty strip in the same situation.
 */
export const isMonitored = (availability: number): boolean => availability >= 0

/**
 * Availability for display.
 *
 * 'Not Monitored' matches the wording the legacy availability box uses for the same rows. That page
 * further distinguishes 'Remotely Monitored', which this payload cannot express -- the availability
 * resource reports one number and no managed or perspective state.
 *
 * Never rounds up to 100%: '100%' has to mean no downtime at all, so anything short of it is
 * reported short of it.
 */
export const formatAvailability = (pct: number): string => {
  if (!isMonitored(pct)) {
    return 'Not Monitored'
  }

  if (pct >= 100) {
    return '100%'
  }

  return `${Number(Math.min(pct, 99.999).toFixed(3))}%`
}

/** '2d 4h', '17m', '41s' -- date-fns' formatDuration is far too long for a tooltip. */
export const formatCompactDuration = (ms: number): string => {
  if (ms < 0) {
    return '0s'
  }

  const seconds = Math.floor(ms / 1000)
  const days = Math.floor(seconds / 86400)
  const hours = Math.floor((seconds % 86400) / 3600)
  const minutes = Math.floor((seconds % 3600) / 60)
  const secs = seconds % 60

  if (days > 0) {
    return hours > 0 ? `${days}d ${hours}h` : `${days}d`
  }

  if (hours > 0) {
    return minutes > 0 ? `${hours}h ${minutes}m` : `${hours}h`
  }

  if (minutes > 0) {
    return secs > 0 ? `${minutes}m ${secs}s` : `${minutes}m`
  }

  return `${secs}s`
}

/**
 * The text for an outage segment's tooltip and its aria-label. One string for both, so what a
 * screen reader hears is what a sighted user sees.
 *
 * Newline separated: a timestamp per line reads far better than one long run, and the tooltip
 * renders the breaks because primevue-overrides.scss sets `white-space: pre-line` on
 * `.p-tooltip-text`. Assistive technology treats a newline as whitespace, so one string still
 * serves both.
 */
export const segmentDescription = (row: TimelineServiceRow, seg: TimelineSegment): string => {
  const lines = [`Outage ${seg.outageId} — ${row.serviceName} on ${row.ipAddress}`]

  lines.push(seg.startsBeforeWindow
    ? `Lost ${formatInDisplayZone(seg.lost)} (before this window)`
    : `Lost ${formatInDisplayZone(seg.lost)}`)

  lines.push(seg.regained === null
    ? `Still down, ${formatCompactDuration(Date.now() - seg.lost)}`
    : `Regained ${formatInDisplayZone(seg.regained)} (${formatCompactDuration(seg.regained - seg.lost)})`)

  return lines.join('\n')
}
