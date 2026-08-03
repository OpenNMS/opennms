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

import { describe, it, expect } from 'vitest'
import {
  normalQuantile, removeOutliers, holtWinters, polyfit, polyval, computeForecast, ForecastOptions
} from '@/components/Resources/utils/forecasting'

describe('normalQuantile', () => {
  it('matches known z-scores', () => {
    expect(normalQuantile(0.5)).toBeCloseTo(0, 6)
    expect(normalQuantile(0.975)).toBeCloseTo(1.959964, 4)
    expect(normalQuantile(0.95)).toBeCloseTo(1.644854, 4)
    expect(normalQuantile(0.025)).toBeCloseTo(-1.959964, 4)
  })
})

describe('polyfit / polyval', () => {
  it('recovers a known polynomial exactly and extrapolates', () => {
    // y = 2x^2 + 3x + 1
    const x = [0, 1, 2, 3, 4, 5]
    const y = x.map((v) => 2 * v * v + 3 * v + 1)
    const c = polyfit(x, y, 2)
    expect(c[0]).toBeCloseTo(1, 4)
    expect(c[1]).toBeCloseTo(3, 4)
    expect(c[2]).toBeCloseTo(2, 4)
    expect(polyval(c, 10)).toBeCloseTo(231, 3) // 2*100 + 30 + 1
  })
})

describe('removeOutliers', () => {
  it('drops points outside the quantile band, keeps the rest', () => {
    const v = [1, 1, 1, 1, 1, 1, 1, 1, 1, 1000]
    const out = removeOutliers(v, 0.9)
    expect(Number.isNaN(out[9])).toBe(true) // the spike is removed
    expect(out.slice(0, 9)).toEqual(v.slice(0, 9))
  })
})

describe('holtWinters', () => {
  it('tracks and continues a seasonal + trend signal', () => {
    const m = 12
    const seasons = 6
    const n = m * seasons
    const truth = (t: number) => 100 + 0.5 * t + 10 * Math.sin((2 * Math.PI * t) / m)
    const y = Array.from({ length: n }, (_, t) => truth(t))

    const { fit, residualStd } = holtWinters(y, m, 0.4, 0.1, 0.3, m)

    // in-sample fit tracks the signal
    for (let t = m; t < n; t++) {
      expect(Math.abs(fit[t] - y[t])).toBeLessThan(6)
    }
    // one-season-ahead forecast stays near the true continuation
    for (let h = 1; h <= m; h++) {
      expect(Math.abs(fit[n + h - 1] - truth(n + h - 1))).toBeLessThan(12)
    }
    expect(residualStd).toBeGreaterThanOrEqual(0)
  })
})

describe('computeForecast', () => {
  const step = 3600 * 1000 // 1h
  const opts = (over: Partial<ForecastOptions> = {}): ForecastOptions => ({
    trainingStart: 14, graphStart: 7, season: 1, forecasts: 1,
    outlierThreshold: 0.975, confidenceLevel: 0.95, trendOrder: 3, ...over
  })

  const seasonalSeries = (hours: number) => {
    const ts: number[] = []
    const vals: number[] = []
    for (let i = 0; i < hours; i++) {
      ts.push(1_700_000_000_000 + i * step)
      vals.push(50 + 0.1 * i + 8 * Math.sin((2 * Math.PI * i) / 24))
    }
    return { ts, vals }
  }

  it('produces fit / bounds / trend with no warning on healthy data', () => {
    const { ts, vals } = seasonalSeries(14 * 24) // 14 days hourly, season=1 day
    const r = computeForecast(ts, vals, step, opts())
    expect(r.warning).toBeNull()
    // horizon adds one season (24 samples) past the training length
    expect(r.timestamps.length).toBe(14 * 24 + 24)
    expect(r.fit.length).toBe(r.timestamps.length)
    // bounds bracket the fit
    for (let i = 0; i < r.fit.length; i++) {
      if (Number.isFinite(r.fit[i])) {
        expect(r.lower[i]).toBeLessThanOrEqual(r.fit[i])
        expect(r.upper[i]).toBeGreaterThanOrEqual(r.fit[i])
      }
    }
    // an upward-trending series trends upward at the horizon
    expect(r.trend[r.trend.length - 1]).toBeGreaterThan(r.trend[0])
  })

  it('warns when season*2 is not less than the training window', () => {
    const { ts, vals } = seasonalSeries(48)
    const r = computeForecast(ts, vals, step, opts({ season: 7, trainingStart: 14 }))
    expect(r.warning).toMatch(/season/i)
    expect(r.fit.length).toBe(0)
  })

  it('warns when there is not enough usable data', () => {
    const { ts, vals } = seasonalSeries(10) // < 2 seasons of 24
    const r = computeForecast(ts, vals, step, opts())
    expect(r.warning).toMatch(/enough/i)
  })

  it('warns on empty input', () => {
    const r = computeForecast([], [], step, opts())
    expect(r.warning).toMatch(/no data/i)
  })

  it('widens the confidence band as the forecast reaches further ahead', () => {
    const { ts, vals } = seasonalSeries(14 * 24)
    const r = computeForecast(ts, vals, step, opts())
    const n = 14 * 24
    const width = (i: number) => r.upper[i] - r.lower[i]
    // constant over the fitted region, then growing through the horizon
    expect(width(n)).toBeCloseTo(width(0), 6)
    expect(width(r.upper.length - 1)).toBeGreaterThan(width(n))
    // and every band edge stays finite
    expect(r.upper.every((v) => Number.isFinite(v))).toBe(true)
    expect(r.lower.every((v) => Number.isFinite(v))).toBe(true)
  })

  it('never emits Infinite bounds when confidence level is out of range', () => {
    const { ts, vals } = seasonalSeries(14 * 24)
    const r = computeForecast(ts, vals, step, opts({ confidenceLevel: 1 }))
    expect(r.upper.every((v) => Number.isFinite(v))).toBe(true)
    expect(r.lower.every((v) => Number.isFinite(v))).toBe(true)
  })

  it('warns when a large interior gap is bridged by interpolation', () => {
    const { ts, vals } = seasonalSeries(14 * 24)
    // blank out ~30 contiguous hours in the middle (> one 24h season)
    for (let i = 150; i < 180; i++) vals[i] = NaN
    const r = computeForecast(ts, vals, step, opts())
    expect(r.warning).toMatch(/gap|interpolat/i)
    // still produces a forecast despite the warning
    expect(r.fit.length).toBeGreaterThan(0)
  })
})
