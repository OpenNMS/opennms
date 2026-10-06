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

import { describe, expect, it } from 'vitest'
import {
  GEO_BASE_ZOOM,
  assetLocation,
  fanOut,
  geoToGraph,
  graphToGeo,
  parkedLabelLimit,
  parkingLayout,
  parkingPages,
  tileZoomFor,
  unwrapLongitudes
} from '@/components/Topology/geo'

describe('geo', () => {
  it('reads a location only when both coordinates are set', () => {
    expect(assetLocation({ latitude: '35.78', longitude: -78.64 })).toEqual({ lat: 35.78, lon: -78.64 })
    expect(assetLocation({ latitude: null, longitude: -78.64 })).toBeNull()
    expect(assetLocation({ latitude: '', longitude: '' })).toBeNull()
    expect(assetLocation({ latitude: 0, longitude: 0 })).toBeNull()
    expect(assetLocation(undefined)).toBeNull()
  })

  it('puts north up and east right', () => {
    const origin = geoToGraph({ lat: 0, lon: 0 })
    expect(origin.x).toBeCloseTo(0)
    expect(origin.y).toBeCloseTo(0)
    const raleigh = geoToGraph({ lat: 35.78, lon: -78.64 })
    const london = geoToGraph({ lat: 51.51, lon: -0.13 })
    expect(london.x).toBeGreaterThan(raleigh.x)
    expect(london.y).toBeGreaterThan(raleigh.y)
  })

  it('round-trips a location', () => {
    for (const p of [{ lat: 35.7796, lon: -78.6382 }, { lat: -33.8688, lon: 151.2093 }, { lat: 60, lon: 0.5 }]) {
      const back = graphToGeo(geoToGraph(p))
      expect(back.lat).toBeCloseTo(p.lat, 9)
      expect(back.lon).toBeCloseTo(p.lon, 9)
    }
  })

  it('clamps latitude to the Web Mercator limit', () => {
    expect(Number.isFinite(geoToGraph({ lat: 90, lon: 0 }).y)).toBe(true)
    expect(geoToGraph({ lat: 90, lon: 0 }).y).toBeCloseTo(geoToGraph({ lat: 85.05112878, lon: 0 }).y)
  })

  it('draws one graph unit per pixel at the base zoom', () => {
    expect(tileZoomFor(1)).toBe(GEO_BASE_ZOOM)
    expect(tileZoomFor(0.5)).toBe(GEO_BASE_ZOOM - 1)
    // A pixel spans 256 * 2^z world units at zoom z; the world here is 256 * 2^17 units.
    const a = geoToGraph({ lat: 0, lon: 0 })
    const b = geoToGraph({ lat: 0, lon: 360 / 2 ** GEO_BASE_ZOOM / 256 })
    expect(b.x - a.x).toBeCloseTo(1, 6)
  })

  it('gives each parked node room for its label', () => {
    const layout = parkingLayout([40, 120, 60], 1000, 600, 10)
    expect(layout.slots).toHaveLength(3)
    expect(layout.overflow).toBe(0)
    // Node diameter + label offset + label + gap separates consecutive centers.
    expect(layout.slots[1].x - layout.slots[0].x).toBe(20 + 3 + 40 + 18)
    expect(layout.slots[2].x - layout.slots[1].x).toBe(20 + 3 + 120 + 18)
    for (const slot of layout.slots) {
      expect(slot.y).toBe(layout.slots[0].y)
      expect(slot.y).toBeGreaterThan(600 - layout.height)
    }
  })

  it('wraps onto more rows and grows the strip', () => {
    const one = parkingLayout([100, 100], 1000, 600, 10)
    const wrapped = parkingLayout(Array(8).fill(100), 700, 600, 10)
    expect(wrapped.overflow).toBe(0)
    expect(new Set(wrapped.slots.map(s => s.y)).size).toBeGreaterThan(1)
    expect(wrapped.height).toBeGreaterThan(one.height)
    for (const slot of wrapped.slots) {
      expect(slot.y).toBeLessThan(600)
    }
  })

  it('counts what does not fit in the rows allowed, leaving room for the pager', () => {
    const layout = parkingLayout(Array(50).fill(100), 700, 600, 10, 2)
    expect(layout.slots.length + layout.overflow).toBe(50)
    expect(layout.overflow).toBeGreaterThan(0)
    const lastRowY = Math.max(...layout.slots.map(s => s.y))
    const lastRow = layout.slots.filter(s => s.y === lastRowY)
    // The last row's final label ends before the pager.
    expect(lastRow[lastRow.length - 1].x - 10 + 20 + 3 + 100 + 18).toBeLessThanOrEqual(700 - 16 - 120)
    expect(parkingLayout([], 700, 600, 10)).toMatchObject({ slots: [], overflow: 0, height: 0 })
  })

  it('keeps the last row when only one node fits it', () => {
    const layout = parkingLayout(Array(6).fill(160), 400, 600, 20)
    expect(layout.slots.length).toBeGreaterThan(0)
    expect(layout.slots.length + layout.overflow).toBe(6)
  })

  it('limits parked labels so one node fits a row', () => {
    expect(parkedLabelLimit(2000, 10)).toBe(160)
    const narrow = parkedLabelLimit(300, 10)
    expect(narrow).toBeLessThan(160)
    expect(parkingLayout([narrow], 300, 600, 10).overflow).toBe(0)
  })

  it('frames a Pacific group across the Pacific', () => {
    const [tokyo, la] = unwrapLongitudes([{ lat: 35.7, lon: 139.7 }, { lat: 34.1, lon: -118.2 }])
    expect(tokyo.lon).toBe(139.7)
    expect(la.lon).toBeCloseTo(241.8)
    // A group already together is left alone.
    const us = [{ lat: 40.7, lon: -74 }, { lat: 34.1, lon: -118.2 }]
    expect(unwrapLongitudes(us)).toEqual(us)
  })

  it('spreads nodes that overlap onto a ring', () => {
    const out = fanOut([{ x: 100, y: 100 }, { x: 101, y: 100 }, { x: 100, y: 101 }, { x: 500, y: 500 }], 30)
    expect(out[3]).toEqual({ x: 500, y: 500 })
    for (let i = 0; i < 3; i++) {
      for (let j = i + 1; j < 3; j++) {
        expect(Math.hypot(out[i].x - out[j].x, out[i].y - out[j].y)).toBeGreaterThanOrEqual(30 - 1e-9)
      }
    }
  })

  it('pages the strip when it overflows, covering every node once', () => {
    expect(parkingPages([100, 100], 1000, 600, 10)).toEqual([0])
    const widths = Array(50).fill(100)
    const starts = parkingPages(widths, 700, 600, 10, 2)
    expect(starts.length).toBeGreaterThan(1)
    expect(starts[0]).toBe(0)
    for (let i = 1; i < starts.length; i++) {
      expect(starts[i]).toBeGreaterThan(starts[i - 1])
    }
    // Every page's nodes fit it, with room for the pager.
    starts.forEach((start, i) => {
      const end = starts[i + 1] ?? widths.length
      const page = parkingLayout(widths.slice(start, end), 700, 600, 10, 2, true)
      expect(page.slots).toHaveLength(end - start)
    })
  })

  it('pushes apart fanned groups that land on each other, keeping each near its place', () => {
    // Two clusters just over minDistance apart: each fans into a ring that reaches the other.
    const points = [
      ...Array.from({ length: 6 }, () => ({ x: 100, y: 100 })),
      ...Array.from({ length: 6 }, () => ({ x: 135, y: 100 })),
      { x: 900, y: 900 }
    ]
    const out = fanOut(points, 30)
    for (let i = 0; i < out.length; i++) {
      for (let j = i + 1; j < out.length; j++) {
        expect(Math.hypot(out[i].x - out[j].x, out[i].y - out[j].y)).toBeGreaterThanOrEqual(30 - 1e-9)
      }
    }
    expect(out[12]).toEqual({ x: 900, y: 900 })
    // Each cluster stays around its own spot rather than one shared center.
    const center = (from: number) => out.slice(from, from + 6).reduce((c, p) => ({ x: c.x + p.x / 6, y: c.y + p.y / 6 }), { x: 0, y: 0 })
    expect(center(0).x).toBeLessThan(center(6).x - 30)
    for (let i = 0; i < 12; i++) {
      expect(Math.hypot(out[i].x - points[i].x, out[i].y - points[i].y)).toBeLessThan(90)
    }
  })

  it('fans a large co-located group quickly', () => {
    const points = Array.from({ length: 2000 }, () => ({ x: 400, y: 300 }))
    const started = performance.now()
    fanOut(points, 30)
    expect(performance.now() - started).toBeLessThan(500)
  })

  it('spirals large groups compactly, still keeping them apart', () => {
    const points = Array.from({ length: 100 }, () => ({ x: 400, y: 300 }))
    const out = fanOut(points, 30)
    let nearest = Infinity
    for (let i = 0; i < out.length; i++) {
      for (let j = i + 1; j < out.length; j++) {
        nearest = Math.min(nearest, Math.hypot(out[i].x - out[j].x, out[i].y - out[j].y))
      }
    }
    expect(nearest).toBeGreaterThanOrEqual(30)
    // A ring of 100 would need a radius of about 480px.
    expect(Math.max(...out.map(p => Math.hypot(p.x - 400, p.y - 300)))).toBeLessThan(200)
  })
})
