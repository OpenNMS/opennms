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

/**
 * Geographic placement for the geomap background: node positions come from
 * asset lat/long, projected to graph units with the Web Mercator projection
 * the map tiles use, so nodes and tiles stay aligned at every zoom.
 */

export interface GeoPoint {
  lat: number
  lon: number
}

export interface GraphPoint {
  x: number
  y: number
}

/** One graph unit is one screen pixel at this tile zoom (street level). */
export const GEO_BASE_ZOOM = 17

/**
 * Dark theme for map tiles. No dark basemap exists to switch to, so the light
 * tiles are inverted and hue-rotate restores their hues. Shared by the canvas
 * map, its PNG export, and the inspector's location map.
 */
export const DARK_TILE_FILTER = 'invert(1) hue-rotate(180deg) brightness(0.92) contrast(0.9) saturate(0.85)'

/** What an export says about the map: left out entirely, or only partly drawn. */
export type ExportMapResult = 'map-omitted' | 'map-partial' | undefined
const WORLD = 256 * 2 ** GEO_BASE_ZOOM
/** Half the projected world's width (and height), in graph units. */
export const WORLD_HALF_EXTENT = WORLD / 2
// Web Mercator's latitude limit: the projection is undefined at the poles.
const MAX_LAT = 85.05112878

/**
 * One coordinate, or null when the asset does not really carry it. The nodes
 * API sends an unset asset field as JSON null, and Number(null) is 0.
 */
export const coordinate = (value: unknown): number | null => {
  if (value === null || value === undefined || value === '') {
    return null
  }
  const n = Number(value)
  return Number.isFinite(n) ? n : null
}

/** A node's asset location, or null when it lacks either coordinate or sits at 0,0. */
export const assetLocation = (
  asset: { latitude?: unknown, longitude?: unknown } | null | undefined
): GeoPoint | null => {
  const lat = coordinate(asset?.latitude)
  const lon = coordinate(asset?.longitude)
  // 0,0 is the null island, not a location anyone provisioned.
  return lat !== null && lon !== null && (lat !== 0 || lon !== 0) ? { lat, lon } : null
}

/** Graph position of a location; graph y points up, so north is +y. */
export const geoToGraph = ({ lat, lon }: GeoPoint): GraphPoint => {
  const s = Math.sin((Math.max(-MAX_LAT, Math.min(MAX_LAT, lat)) * Math.PI) / 180)
  const mx = (lon + 180) / 360
  const my = 0.5 - Math.log((1 + s) / (1 - s)) / (4 * Math.PI)
  return { x: (mx - 0.5) * WORLD, y: (0.5 - my) * WORLD }
}

export const graphToGeo = ({ x, y }: GraphPoint): GeoPoint => {
  const my = 0.5 - y / WORLD
  return {
    lat: (180 / Math.PI) * Math.atan(Math.sinh(Math.PI * (1 - 2 * my))),
    lon: (x / WORLD + 0.5) * 360 - 180
  }
}

/**
 * Longitudes shifted by 360° where that keeps a group together: the widest gap
 * between neighbors (going round the globe) is where the map is cut, so a
 * network spanning the Pacific is framed across the Pacific.
 */
export const unwrapLongitudes = (points: GeoPoint[]): GeoPoint[] => {
  if (points.length < 2) {
    return points
  }
  const lons = points.map(p => p.lon).sort((a, b) => a - b)
  // The gap across the date line, from the easternmost back round to the westernmost.
  let widest = lons[0] + 360 - lons[lons.length - 1]
  let cut: number | null = null
  for (let i = 1; i < lons.length; i++) {
    const gap = lons[i] - lons[i - 1]
    if (gap > widest) {
      widest = gap
      cut = lons[i]
    }
  }
  if (cut === null) {
    return points
  }
  const edge = cut
  // Everything west of the widest gap moves east of the rest.
  return points.map(p => (p.lon < edge ? { lat: p.lat, lon: p.lon + 360 } : p))
}

/**
 * Viewport positions with overlapping nodes spread out: nodes closer than
 * `minDistance` px are grouped around their center, at least `minDistance`
 * apart. Small groups form a ring; larger ones a sunflower spiral, whose
 * radius grows with the square root of the count rather than the count.
 */
const RING_MAX = 8
const GOLDEN_ANGLE = Math.PI * (3 - Math.sqrt(5))

// Relaxation stops after this many passes even if a few nodes still touch.
const FAN_RELAX_PASSES = 40

/**
 * Call `visit` for each pair of points closer than `distance`, bucketing them
 * in a grid of that size so only neighboring cells are compared.
 */
const forEachClosePair = (points: GraphPoint[], distance: number, visit: (i: number, j: number) => void) => {
  if (!(distance > 0)) {
    return
  }
  const cells = new Map<string, number[]>()
  points.forEach((p, i) => {
    const key = `${Math.floor(p.x / distance)},${Math.floor(p.y / distance)}`
    const cell = cells.get(key)
    if (cell) {
      cell.push(i)
    } else {
      cells.set(key, [i])
    }
  })
  points.forEach((p, i) => {
    const cx = Math.floor(p.x / distance)
    const cy = Math.floor(p.y / distance)
    for (let dx = -1; dx <= 1; dx++) {
      for (let dy = -1; dy <= 1; dy++) {
        for (const j of cells.get(`${cx + dx},${cy + dy}`) ?? []) {
          if (j > i && Math.hypot(p.x - points[j].x, p.y - points[j].y) < distance) {
            visit(i, j)
          }
        }
      }
    }
  })
}

/** Each group's members around the group's center: a ring, or a sunflower spiral. */
const arrangeGroups = (points: GraphPoint[], groups: Iterable<number[]>, minDistance: number): GraphPoint[] => {
  const out = points.map(p => ({ ...p }))
  for (const members of groups) {
    if (members.length < 2) {
      continue
    }
    const cx = members.reduce((sum, i) => sum + points[i].x, 0) / members.length
    const cy = members.reduce((sum, i) => sum + points[i].y, 0) / members.length
    if (members.length <= RING_MAX) {
      // A chord of `minDistance` between ring neighbors.
      const radius = minDistance / (2 * Math.sin(Math.PI / members.length))
      members.forEach((i, k) => {
        const angle = -Math.PI / 2 + (2 * Math.PI * k) / members.length
        out[i] = { x: cx + radius * Math.cos(angle), y: cy + radius * Math.sin(angle) }
      })
      continue
    }
    // 0.65 is the smallest spacing that keeps every pair `minDistance` apart.
    members.forEach((i, k) => {
      const r = 0.65 * minDistance * Math.sqrt(k + 0.5)
      out[i] = { x: cx + r * Math.cos(k * GOLDEN_ANGLE), y: cy + r * Math.sin(k * GOLDEN_ANGLE) }
    })
  }
  return out
}

export const fanOut = (points: GraphPoint[], minDistance: number): GraphPoint[] => {
  const parent = points.map((_, i) => i)
  const find = (i: number): number => (parent[i] === i ? i : (parent[i] = find(parent[i])))
  forEachClosePair(points, minDistance, (i, j) => {
    parent[find(i)] = find(j)
  })
  const groups = new Map<number, number[]>()
  points.forEach((_, i) => {
    const root = find(i)
    const group = groups.get(root)
    if (group) {
      group.push(i)
    } else {
      groups.set(root, [i])
    }
  })
  const out = arrangeGroups(points, groups.values(), minDistance)
  // A fanned group can reach a neighbor. Merging the two and fanning them around
  // one center would chain across a dense region and pull nodes far from where
  // they are, so the close pairs are pushed apart instead, a little at a time.
  // Ring neighbors sit exactly minDistance apart; float error must not count as close.
  const target = minDistance - 1e-9
  for (let pass = 0; pass < FAN_RELAX_PASSES; pass++) {
    let moved = false
    forEachClosePair(out, target, (i, j) => {
      const dx = out[j].x - out[i].x
      const dy = out[j].y - out[i].y
      const dist = Math.hypot(dx, dy)
      const [ux, uy] = dist > 0 ? [dx / dist, dy / dist] : [1, 0]
      // A hair over half the gap each, so the pair ends at or past minDistance.
      const push = (minDistance - dist) / 2 + 1e-7
      out[i] = { x: out[i].x - ux * push, y: out[i].y - uy * push }
      out[j] = { x: out[j].x + ux * push, y: out[j].y + uy * push }
      moved = true
    })
    if (!moved) {
      break
    }
  }
  return out
}

/** The tile zoom that draws one graph unit as `pixelsPerUnit` screen pixels. */
export const tileZoomFor = (pixelsPerUnit: number): number => GEO_BASE_ZOOM + Math.log2(pixelsPerUnit)

// Room for the strip's "No location" caption on the left, a margin on the
// right, and on the last row, room for the pager (two buttons and "n / m").
const PARKING_LEFT_PX = 110
const PARKING_RIGHT_PX = 16
const PARKING_MORE_PX = 120
const PARKING_PAD_PX = 6
// Between one node's label and the next node.
const PARKING_GAP_PX = 18
// Label anchor offset to the right of a node, as drawPlacedNodeLabel places it.
const LABEL_OFFSET_PX = 3

/**
 * The widest a parked label may be on a canvas this wide: one node always fits
 * a row, and, since any row can end up last on a page, beside the pager.
 */
export const parkedLabelLimit = (width: number, nodeRadius: number, cap = 160): number =>
  Math.max(24, Math.min(cap, width - PARKING_LEFT_PX - PARKING_RIGHT_PX - PARKING_MORE_PX
    - 2 * nodeRadius - LABEL_OFFSET_PX - PARKING_GAP_PX))

/**
 * Where each page of the strip starts, in `labelWidths` order: one page when
 * everything fits, otherwise as many full pages as it takes, each leaving
 * room for the pager.
 */
export const parkingPages = (
  labelWidths: number[],
  width: number,
  height: number,
  nodeRadius: number,
  maxRows = 3
): number[] => {
  if (parkingLayout(labelWidths, width, height, nodeRadius, maxRows).overflow === 0) {
    return [0]
  }
  const starts: number[] = []
  for (let start = 0; start < labelWidths.length;) {
    starts.push(start)
    const page = parkingLayout(labelWidths.slice(start), width, height, nodeRadius, maxRows, true)
    start += Math.max(1, page.slots.length)
  }
  return starts
}

export interface ParkingLayout {
  /** Viewport centers for the first `slots.length` nodes; the rest don't fit. */
  slots: GraphPoint[]
  overflow: number
  /** Height of the strip along the bottom of the canvas, 0 when nothing is parked. */
  height: number
  rowHeight: number
}

/**
 * Lay nodes without a location out along the bottom of the canvas: left to
 * right with room for each label, wrapping onto up to `maxRows` rows, and
 * counting whatever still doesn't fit as overflow.
 */
export const parkingLayout = (
  labelWidths: number[],
  width: number,
  height: number,
  nodeRadius: number,
  maxRows = 3,
  reservePager = false
): ParkingLayout => {
  const rowHeight = Math.max(2 * nodeRadius + PARKING_PAD_PX, 28)
  if (labelWidths.length === 0) {
    return { slots: [], overflow: 0, height: 0, rowHeight }
  }
  const itemWidth = (labelWidth: number) => 2 * nodeRadius + LABEL_OFFSET_PX + labelWidth + PARKING_GAP_PX
  const end = width - PARKING_RIGHT_PX
  const placed: { row: number, x: number, width: number }[] = []
  let row = 0
  let x = PARKING_LEFT_PX
  for (const labelWidth of labelWidths) {
    const w = itemWidth(labelWidth)
    if (x + w > end && x > PARKING_LEFT_PX) {
      row++
      x = PARKING_LEFT_PX
    }
    if (row >= maxRows) {
      break
    }
    placed.push({ row, x, width: w })
    x += w
  }
  let overflow = labelWidths.length - placed.length
  // Make room on the last row for the pager (when more follows, or when asked),
  // but never empty the row: on a narrow canvas it then sits past its last node.
  while ((overflow > 0 || reservePager) && placed.length > 1) {
    const last = placed[placed.length - 1]
    if (last.x + last.width <= end - PARKING_MORE_PX || placed[placed.length - 2].row !== last.row) {
      break
    }
    placed.pop()
    overflow++
  }
  const rows = placed.length > 0 ? placed[placed.length - 1].row + 1 : 1
  const stripHeight = rows * rowHeight + 2 * PARKING_PAD_PX
  const top = height - stripHeight + PARKING_PAD_PX
  return {
    slots: placed.map(p => ({ x: p.x + nodeRadius, y: top + rowHeight * (p.row + 0.5) })),
    overflow,
    height: stripHeight,
    rowHeight
  }
}
