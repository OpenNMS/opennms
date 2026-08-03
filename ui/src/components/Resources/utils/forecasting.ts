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

// Pure client-side forecasting for the Resource Graphs page. Reimplements what
// the legacy forecast.jsp delegated to server-side measurement filters (which
// required R for Holt-Winters): outlier removal, additive Holt-Winters with
// confidence bounds, and a polynomial trend. Everything here is deterministic
// and unit-testable — no R, no server round-trip beyond fetching the raw series.

export interface ForecastOptions {
  trainingStart: number // days of history used to train
  graphStart: number // days back where the displayed range begins
  season: number // seasonal period, in days
  forecasts: number // number of seasonal periods to project ahead
  outlierThreshold: number // quantile (e.g. 0.975) — points outside are dropped
  confidenceLevel: number // e.g. 0.95
  trendOrder: number // polynomial order for the trend line
}

export interface ForecastResult {
  timestamps: number[] // uniform grid: training window + forecast horizon
  fit: number[] // Holt-Winters in-sample fit, then the projection
  lower: number[] // HW lower confidence bound
  upper: number[] // HW upper confidence bound
  trend: number[] // polynomial trend + extrapolation
  warning: string | null
}

const isNum = (v: unknown): v is number => typeof v === 'number' && Number.isFinite(v)

// Normal quantile via the Acklam rational approximation — good to ~1e-9, plenty
// for a confidence-level z-score, and avoids pulling in a stats dependency.
export const normalQuantile = (p: number): number => {
  if (p <= 0) return -Infinity
  if (p >= 1) return Infinity
  const a = [-3.969683028665376e1, 2.209460984245205e2, -2.759285104469687e2, 1.38357751867269e2, -3.066479806614716e1, 2.506628277459239]
  const b = [-5.447609879822406e1, 1.615858368580409e2, -1.556989798598866e2, 6.680131188771972e1, -1.328068155288572e1]
  const c = [-7.784894002430293e-3, -3.223964580411365e-1, -2.400758277161838, -2.549732539343734, 4.374664141464968, 2.938163982698783]
  const d = [7.784695709041462e-3, 3.224671290700398e-1, 2.445134137142996, 3.754408661907416]
  const pl = 0.02425
  let q, r
  if (p < pl) {
    q = Math.sqrt(-2 * Math.log(p))
    return (((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
  } else if (p <= 1 - pl) {
    q = p - 0.5
    r = q * q
    return (((((a[0] * r + a[1]) * r + a[2]) * r + a[3]) * r + a[4]) * r + a[5]) * q / (((((b[0] * r + b[1]) * r + b[2]) * r + b[3]) * r + b[4]) * r + 1)
  }
  q = Math.sqrt(-2 * Math.log(1 - p))
  return -(((((c[0] * q + c[1]) * q + c[2]) * q + c[3]) * q + c[4]) * q + c[5]) / ((((d[0] * q + d[1]) * q + d[2]) * q + d[3]) * q + 1)
}

// Linear-interpolation quantile over already-sorted values.
const quantileSorted = (sorted: number[], p: number): number => {
  if (sorted.length === 0) return NaN
  if (sorted.length === 1) return sorted[0]
  const idx = p * (sorted.length - 1)
  const lo = Math.floor(idx)
  const hi = Math.ceil(idx)
  if (lo === hi) return sorted[lo]
  return sorted[lo] + (sorted[hi] - sorted[lo]) * (idx - lo)
}

// Two-sided quantile clamp: values outside [1-q, q] quantiles become NaN.
export const removeOutliers = (values: number[], q: number): number[] => {
  const finite = values.filter(isNum).sort((a, b) => a - b)
  if (finite.length < 3 || q >= 1 || q <= 0.5) {
    return values.slice()
  }
  const upper = quantileSorted(finite, q)
  const lower = quantileSorted(finite, 1 - q)
  return values.map((v) => (isNum(v) && v >= lower && v <= upper ? v : NaN))
}

// Linear interpolation across interior NaN gaps; leading/trailing NaN kept as NaN.
const interpolateGaps = (values: number[]): number[] => {
  const out = values.slice()
  let last = -1
  for (let i = 0; i < out.length; i++) {
    if (isNum(out[i])) {
      if (last >= 0 && i - last > 1) {
        const step = (out[i] - out[last]) / (i - last)
        for (let j = last + 1; j < i; j++) out[j] = out[last] + step * (j - last)
      }
      last = i
    }
  }
  return out
}

// Additive Holt-Winters over an evenly-spaced series. Returns the in-sample fit
// followed by `horizon` projected points, plus the residual standard deviation.
export const holtWinters = (
  y: number[], m: number, alpha: number, beta: number, gamma: number, horizon: number
): { fit: number[]; residualStd: number } => {
  const n = y.length
  const seasonal = new Array(m)
  const firstMean = mean(y.slice(0, m))
  const secondMean = mean(y.slice(m, 2 * m))
  let level = firstMean
  let trend = (secondMean - firstMean) / m
  for (let i = 0; i < m; i++) seasonal[i] = y[i] - firstMean

  const fit = new Array(n + horizon)
  let sse = 0
  let cnt = 0
  for (let t = 0; t < n; t++) {
    const s = seasonal[t % m]
    const oneStep = level + trend + s
    fit[t] = oneStep
    if (t >= m) {
      const resid = y[t] - oneStep
      sse += resid * resid
      cnt++
    }
    const prevLevel = level
    level = alpha * (y[t] - s) + (1 - alpha) * (level + trend)
    trend = beta * (level - prevLevel) + (1 - beta) * trend
    seasonal[t % m] = gamma * (y[t] - level) + (1 - gamma) * s
  }
  for (let h = 1; h <= horizon; h++) {
    fit[n + h - 1] = level + h * trend + seasonal[(n + h - 1) % m]
  }
  return { fit, residualStd: cnt > 1 ? Math.sqrt(sse / cnt) : 0 }
}

// Grid-search alpha/beta/gamma minimising in-sample SSE (mirrors R optimising
// the smoothing constants). Coarse but ample for a monotone-ish error surface.
const optimizeHoltWinters = (y: number[], m: number, horizon: number) => {
  const grid = [0.05, 0.15, 0.3, 0.45, 0.6, 0.75, 0.9]
  let best = { alpha: 0.3, beta: 0.1, gamma: 0.3, sse: Infinity }
  for (const alpha of grid) {
    for (const beta of grid) {
      for (const gamma of grid) {
        const { fit } = holtWinters(y, m, alpha, beta, gamma, 0)
        let sse = 0
        for (let t = m; t < y.length; t++) {
          const r = y[t] - fit[t]
          sse += r * r
        }
        if (sse < best.sse) best = { alpha, beta, gamma, sse }
      }
    }
  }
  return holtWinters(y, m, best.alpha, best.beta, best.gamma, horizon)
}

const mean = (arr: number[]): number => {
  const f = arr.filter(isNum)
  return f.length ? f.reduce((a, b) => a + b, 0) / f.length : NaN
}

// Least-squares polynomial fit via the normal equations. Pass a bounded x
// (e.g. a [0,1]-normalised index) — the callers do — so the matrix stays
// well-conditioned for higher orders.
export const polyfit = (x: number[], y: number[], order: number): number[] => {
  const pts: [number, number][] = []
  for (let i = 0; i < x.length; i++) if (isNum(y[i])) pts.push([x[i], y[i]])
  const deg = Math.min(order, Math.max(1, pts.length - 1))
  const A: number[][] = []
  const b: number[] = []
  for (let r = 0; r <= deg; r++) {
    A[r] = []
    for (let c = 0; c <= deg; c++) {
      let s = 0
      for (const [xi] of pts) s += Math.pow(xi, r + c)
      A[r][c] = s
    }
    let sb = 0
    for (const [xi, yi] of pts) sb += Math.pow(xi, r) * yi
    b[r] = sb
  }
  return solveLinear(A, b)
}

export const polyval = (coeffs: number[], x: number): number => {
  let v = 0
  for (let i = coeffs.length - 1; i >= 0; i--) v = v * x + coeffs[i]
  return v
}

// Gaussian elimination with partial pivoting.
const solveLinear = (A: number[][], b: number[]): number[] => {
  const n = b.length
  const M = A.map((row, i) => [...row, b[i]])
  for (let col = 0; col < n; col++) {
    let piv = col
    for (let r = col + 1; r < n; r++) if (Math.abs(M[r][col]) > Math.abs(M[piv][col])) piv = r
    ;[M[col], M[piv]] = [M[piv], M[col]]
    if (Math.abs(M[col][col]) < 1e-12) continue
    for (let r = 0; r < n; r++) {
      if (r === col) continue
      const f = M[r][col] / M[col][col]
      for (let c = col; c <= n; c++) M[r][c] -= f * M[col][c]
    }
  }
  return M.map((row, i) => (Math.abs(M[i][i]) < 1e-12 ? 0 : row[n] / M[i][i]))
}

/**
 * Compute a forecast for one metric column sampled on a uniform time grid.
 * @param timestamps ms epoch, evenly spaced by `stepMs`, covering the training window
 * @param rawValues  the metric's values aligned to `timestamps` (NaN/strings allowed)
 */
export const computeForecast = (
  timestamps: number[], rawValues: unknown[], stepMs: number, options: ForecastOptions
): ForecastResult => {
  const values = rawValues.map((v) => (isNum(v) ? v : NaN))
  const empty: ForecastResult = { timestamps: [], fit: [], lower: [], upper: [], trend: [], warning: null }

  if (!timestamps.length || !stepMs || stepMs <= 0) {
    return { ...empty, warning: 'No data available to forecast.' }
  }
  if (options.season * 2 >= options.trainingStart) {
    return { ...empty, warning: 'The season length must be less than half the training window.' }
  }

  // samples per season and forecast horizon (in samples)
  const m = Math.max(2, Math.round((options.season * 86400 * 1000) / stepMs))
  const horizon = Math.max(1, Math.round(options.forecasts * m))

  const cleaned = removeOutliers(values, options.outlierThreshold)
  const validCount = cleaned.filter(isNum).length
  if (validCount < 2 * m) {
    return { ...empty, warning: 'Not enough usable data in the training window to forecast (need at least two full seasons).' }
  }

  // confidence level must be a probability in (0,1); z would be ±Infinity otherwise
  const cl = options.confidenceLevel > 0 && options.confidenceLevel < 1 ? options.confidenceLevel : 0.95

  const series = interpolateGaps(cleaned)
  // trim any leading NaN (before the first real sample) so HW starts on data
  let startIdx = 0
  while (startIdx < series.length && !isNum(series[startIdx])) startIdx++
  let endIdx = series.length - 1
  while (endIdx > startIdx && !isNum(series[endIdx])) endIdx--
  const trainVals = series.slice(startIdx, endIdx + 1)
  if (trainVals.some((v) => !isNum(v)) || trainVals.length < 2 * m) {
    return { ...empty, warning: 'Forecast produced no valid values — gaps or outliers left too few usable samples after filtering.' }
  }

  // how much of the training window was fabricated by interpolation (honesty check)
  const rawWindow = cleaned.slice(startIdx, endIdx + 1)
  let interpolated = 0
  let run = 0
  let maxGap = 0
  for (const v of rawWindow) {
    if (!isNum(v)) { interpolated++; run++; maxGap = Math.max(maxGap, run) } else run = 0
  }
  const interpFraction = interpolated / trainVals.length

  const { fit, residualStd } = optimizeHoltWinters(trainVals, m, horizon)
  const z = normalQuantile(1 - (1 - cl) / 2)

  // build the output grid: training timestamps + horizon steps into the future
  const n = trainVals.length
  const outTs: number[] = []
  for (let i = 0; i < n + horizon; i++) {
    outTs.push(timestamps[startIdx] + i * stepMs)
  }

  // Confidence bounds: constant ±z·σ over the fitted region, widening with the
  // forecast horizon (σ·√h at h steps ahead) — a projected value further in the
  // future is less certain, matching prediction-interval behaviour.
  const lower = fit.map((f, i) => f - z * residualStd * (i >= n ? Math.sqrt(i - n + 1) : 1))
  const upper = fit.map((f, i) => f + z * residualStd * (i >= n ? Math.sqrt(i - n + 1) : 1))

  // polynomial trend fit on a normalised index (training in [0,1]) so the
  // normal-equations matrix stays well-conditioned even at order 3+
  const norm = Math.max(1, n - 1)
  const coeffs = polyfit(Array.from({ length: n }, (_, i) => i / norm), trainVals, options.trendOrder)
  const trend = outTs.map((_, i) => polyval(coeffs, i / norm))

  let warning: string | null = null
  if (maxGap >= m || interpFraction > 0.25) {
    warning = 'The training window has large gaps; much of the fit is interpolated, so the forecast may be unreliable.'
  } else if (residualStd === 0) {
    warning = 'Confidence bounds have zero width — the training residuals had no variance.'
  }

  return { timestamps: outTs, fit, lower, upper, trend, warning }
}
