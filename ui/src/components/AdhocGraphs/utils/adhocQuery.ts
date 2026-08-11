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

import {
  AdhocDatasourceOption,
  AdhocExpression,
  AdhocGraphConfig,
  AdhocSeries
} from '@/types/adhocGraph'
import { ConvertedGraphData, GraphMetricsPayload, Metric, StartEndTime } from '@/types'

/** Target number of samples across the range when the user has not chosen one. */
export const DEFAULT_RESOLUTION = 400

/** Ceiling the measurements API is asked for; also its `maxrows`. */
export const MAX_RESOLUTION = 4000

/** Smallest step (ms) we will ever ask for — one second. */
const MIN_STEP_MS = 1000

/**
 * Reduce a node/resource/attribute triple to a legal JEXL identifier.
 *
 * A source label is not cosmetic here: it is the name an expression refers to, and
 * the measurements API hands it straight to JEXL. Anything that is not a letter,
 * digit or underscore is collapsed to a single underscore, and a leading digit is
 * prefixed, because `2_eth0` is not a valid identifier.
 */
export const sanitizeLabel = (...parts: (string | undefined)[]): string => {
  const joined = parts.filter(Boolean).join('_')
  const cleaned = joined
    .replace(/[^a-zA-Z0-9]/g, '_')
    .replace(/_+/g, '_')
    .replace(/^_+|_+$/g, '')

  if (!cleaned) {
    return 'series'
  }

  return /^[0-9]/.test(cleaned) ? `_${cleaned}` : cleaned
}

/**
 * Make `candidate` unique against `taken`, appending _2, _3, … as needed.
 * Two interfaces on two nodes can easily sanitize to the same string, and a
 * duplicate label silently makes one series unreachable from expressions.
 */
export const uniqueLabel = (candidate: string, taken: Iterable<string>): string => {
  const used = new Set(taken)

  if (!used.has(candidate)) {
    return candidate
  }

  let suffix = 2
  while (used.has(`${candidate}_${suffix}`)) {
    suffix++
  }

  return `${candidate}_${suffix}`
}

/** The default, collision-free label for a newly selected datasource. */
export const labelForDatasource = (
  datasource: AdhocDatasourceOption,
  taken: Iterable<string>
): string => uniqueLabel(
  sanitizeLabel(datasource.nodeLabel, datasource.resourceLabel, datasource.attribute),
  taken
)

/**
 * Whether `expression` references `label` as a whole identifier.
 *
 * Word boundaries alone are not enough: `ifInOctets` is a prefix of
 * `ifInOctets_2`, and a substring match would wrongly mark the shorter series as
 * consumed. Underscore counts as an identifier character, so the guard rejects a
 * neighbouring `_` as well as alphanumerics.
 */
export const expressionReferences = (expression: string, label: string): boolean => {
  if (!label) {
    return false
  }

  const escaped = label.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  return new RegExp(`(^|[^A-Za-z0-9_])${escaped}([^A-Za-z0-9_]|$)`).test(expression)
}

/** The step (ms) that yields roughly `resolution` samples across the range. */
export const stepForRange = (startMs: number, endMs: number, resolution: number): number => {
  const span = Math.max(0, endMs - startMs)
  const samples = Math.min(Math.max(Math.round(resolution) || DEFAULT_RESOLUTION, 1), MAX_RESOLUTION)
  return Math.max(MIN_STEP_MS, Math.floor(span / samples) || MIN_STEP_MS)
}

/**
 * Build the POST /rest/measurements body for an ad-hoc graph.
 *
 * Two behaviours worth calling out:
 *  - `relaxed: true`. An ad-hoc selection goes stale the moment a resource is
 *    deleted or an interface is renamed, and a strict query fails the *whole*
 *    request over one missing source. Relaxed returns the rest with NaNs.
 *  - `transient` is derived, not taken on trust. A source is only suppressed when
 *    the user asked to hide it AND some expression actually consumes it; hiding a
 *    source nothing references would just produce an empty graph.
 */
export const buildMeasurementsPayload = (
  config: AdhocGraphConfig,
  time: StartEndTime
): GraphMetricsPayload => {
  const start = Number(time.startTime) * 1000
  const end = Number(time.endTime) * 1000
  const step = stepForRange(start, end, config.resolution)

  const isConsumed = (series: AdhocSeries) =>
    config.expressions.some(expression => expressionReferences(expression.value, series.label))

  const source: Metric[] = config.series.map(series => ({
    aggregation: series.aggregation,
    attribute: series.attribute,
    label: series.label,
    resourceId: series.resourceId,
    transient: Boolean(series.hidden && isConsumed(series))
  }))

  const payload: GraphMetricsPayload = {
    start,
    end,
    step,
    maxrows: MAX_RESOLUTION,
    relaxed: true,
    source
  }

  if (config.expressions.length) {
    payload.expression = config.expressions.map(expression => ({
      label: expression.label,
      value: expression.value,
      transient: false
    }))
  }

  return payload
}

/** Every series that should appear as a plotted column, sources then expressions. */
export const plottedSeries = (config: AdhocGraphConfig): (AdhocSeries | AdhocExpression)[] => {
  const isConsumed = (series: AdhocSeries) =>
    config.expressions.some(expression => expressionReferences(expression.value, series.label))

  return [
    ...config.series.filter(series => !(series.hidden && isConsumed(series))),
    ...config.expressions
  ]
}

/**
 * Per-series label problems, keyed by series key.
 *
 * A label is a JEXL identifier, not decoration: blank, duplicated or
 * non-identifier labels either fail the query outright or silently bind an
 * expression to the wrong series, so they are errors rather than warnings.
 */
export const seriesLabelIssues = (config: AdhocGraphConfig): Record<string, string> => {
  const counts = new Map<string, number>()

  for (const entry of [...config.series, ...config.expressions]) {
    counts.set(entry.label, (counts.get(entry.label) ?? 0) + 1)
  }

  const issues: Record<string, string> = {}

  for (const entry of config.series) {
    if (!entry.label.trim()) {
      issues[entry.key] = 'A label is required.'
    } else if ((counts.get(entry.label) ?? 0) > 1) {
      issues[entry.key] = 'Labels must be unique.'
    } else if (sanitizeLabel(entry.label) !== entry.label) {
      issues[entry.key] = 'Use letters, digits and underscores only.'
    }
  }

  return issues
}

/**
 * Per-expression problems, keyed by expression id.
 *
 * JEXL syntax itself is left to the server — guessing at it here would reject
 * valid expressions — but a blank expression, or one whose every identifier is
 * unknown, is always a mistake and is worth catching before the round trip.
 */
export interface AdhocExpressionIssue {
  /** Which input the message belongs beside. */
  field: 'label' | 'value'
  message: string
}

export const expressionIssues = (config: AdhocGraphConfig): Record<string, AdhocExpressionIssue> => {
  const counts = new Map<string, number>()

  for (const entry of [...config.series, ...config.expressions]) {
    counts.set(entry.label, (counts.get(entry.label) ?? 0) + 1)
  }

  const known = new Set(config.series.map(entry => entry.label).filter(Boolean))
  const issues: Record<string, AdhocExpressionIssue> = {}

  for (const expression of config.expressions) {
    if (!expression.label.trim()) {
      issues[expression.id] = { field: 'label', message: 'A name is required.' }
      continue
    }

    if ((counts.get(expression.label) ?? 0) > 1) {
      issues[expression.id] = { field: 'label', message: 'Already used by another series.' }
      continue
    }

    if (sanitizeLabel(expression.label) !== expression.label) {
      issues[expression.id] = { field: 'label', message: 'Use letters, digits and underscores only.' }
      continue
    }

    const value = expression.value.trim()

    if (!value) {
      issues[expression.id] = { field: 'value', message: 'An expression is required.' }
      continue
    }

    const identifiers = value.match(/[A-Za-z_][A-Za-z0-9_]*/g) ?? []
    const unresolved = identifiers.filter(identifier => !known.has(identifier))

    if (identifiers.length && unresolved.length === identifiers.length) {
      issues[expression.id] = {
        field: 'value',
        message: `Unknown label${unresolved.length > 1 ? 's' : ''}: ${unresolved.slice(0, 3).join(', ')}`
      }
    }
  }

  return issues
}

/** Whether the config is complete enough to send to the measurements API. */
export const configIsQueryable = (config: AdhocGraphConfig): boolean =>
  config.series.length > 0 &&
  !Object.keys(seriesLabelIssues(config)).length &&
  !Object.keys(expressionIssues(config)).length

/**
 * The parts of a config that change the server response. Style, colour, title,
 * vertical label and stacking are re-rendered from data already in hand, so they
 * deliberately do not appear here and never trigger a refetch.
 *
 * For a relative window the identity is the RANGE, not the instants it currently
 * resolves to. Re-resolving "last two days" against a newer clock is not a change
 * of query — and treating it as one would make every Refresh fire twice, once
 * explicitly and once from the watcher that observes this.
 */
export const querySignature = (config: AdhocGraphConfig, time: StartEndTime): string => JSON.stringify([
  config.series.map(entry => [entry.resourceId, entry.attribute, entry.aggregation, entry.label, entry.hidden]),
  config.expressions.map(expression => [expression.label, expression.value]),
  config.resolution,
  time.range ? ['range', time.range.unit, time.range.amount] : [time.startTime, time.endTime]
])

/**
 * Adapt an ad-hoc config to the `ConvertedGraphData` shape that the prefab-graph
 * components already speak, so `GraphDataTable.vue` and `downloadGraphCsv` work
 * unchanged. Ad-hoc graphs have no RRD print statements, so each metric gets a
 * trivial one whose header is simply the series label.
 */
export const toConvertedGraphData = (config: AdhocGraphConfig): ConvertedGraphData => {
  const plotted = plottedSeries(config)

  return {
    title: config.title,
    verticalLabel: config.verticalLabel,
    series: [],
    values: [],
    properties: {},
    metrics: plotted.map(item => ({
      name: item.label,
      label: item.label,
      aggregation: 'aggregation' in item ? item.aggregation : 'AVERAGE',
      attribute: 'attribute' in item ? item.attribute : '',
      resourceId: 'resourceId' in item ? item.resourceId : ''
    })),
    printStatements: plotted.map(item => ({
      format: '',
      header: item.label,
      metric: item.label,
      value: NaN
    }))
  }
}
