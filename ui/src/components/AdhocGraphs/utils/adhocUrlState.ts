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

import { AdhocExpression, AdhocGraphConfig, AdhocSeries, AdhocSeriesStyle } from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { RelativeTimeRange, StartEndTime } from '@/types'
import { RANGE_UNITS } from '@/components/Resources/utils/timeRangeOptions'
import { DEFAULT_RESOLUTION } from './adhocQuery'

/** Field separator inside one entry. */
const FIELD = '~'

/**
 * Escape a field so the separator survives the round trip.
 *
 * An earlier version assumed `~` could not appear inside a field. That is false
 * for anything the user types: `=~` is JEXL's match operator, so the perfectly
 * ordinary expression `a =~ [1,2] ? 1 : 0` used to split into extra fields and
 * decode back as `a =`, silently, taking the style and color with it. Resource ids
 * can carry one too (a Windows short name such as `PROGRA~1` in a storage path).
 *
 * Only `%` and `~` are touched, so a normal link is byte-for-byte what it was —
 * running whole fields through encodeURIComponent would escape every bracket in
 * every resource id and inflate the URL against its shareable-length budget for no
 * benefit. `%` must be escaped first, and unescaped last, or a literal `%7E` would
 * come back as a separator.
 */
const escapeField = (value: string): string =>
  value.replace(/%/g, '%25').replace(/~/g, '%7E')

const unescapeField = (value: string): string =>
  value.replace(/%7E/gi, '~').replace(/%25/gi, '%')

/**
 * Above this many characters the query string stops being something a person can
 * paste into chat or a ticket, and some proxies start truncating it. Past the cap
 * the graph still works — it just stops being shareable, and the caller says so.
 */
export const MAX_QUERY_LENGTH = 6000

// A link written before 'scatter' was dropped decodes to 'line' via asStyle.
const STYLES: AdhocSeriesStyle[] = ['line', 'line2', 'line3', 'area', 'stack']

const AGGREGATIONS = Object.values(ConsolidationFunctionType)

export interface AdhocUrlState {
  config: AdhocGraphConfig
  time: StartEndTime
}

/**
 * A route query as vue-router hands it over. Repeated keys arrive as arrays, and a
 * valueless key (`?stacked`) arrives as null — both are accepted so `route.query`
 * can be passed straight in.
 */
export type RouteQuery = Record<string, string | (string | null)[] | null | undefined>

const first = (value: RouteQuery[string]): string => {
  if (Array.isArray(value)) {
    return value[0] ?? ''
  }
  return value ?? ''
}

const many = (value: RouteQuery[string]): string[] => {
  if (Array.isArray(value)) {
    return value.filter((entry): entry is string => typeof entry === 'string')
  }
  return typeof value === 'string' ? [value] : []
}

const asStyle = (value: string): AdhocSeriesStyle =>
  (STYLES.includes(value as AdhocSeriesStyle) ? value as AdhocSeriesStyle : 'line')

const asAggregation = (value: string): ConsolidationFunctionType =>
  (AGGREGATIONS.includes(value as ConsolidationFunctionType) ?
    value as ConsolidationFunctionType :
    ConsolidationFunctionType.AVERAGE)

const asColor = (value: string): string =>
  (/^#[0-9a-fA-F]{6}$/.test(value) ? value.toLowerCase() : '')

/**
 * Parse `range=<unit>:<amount>`, e.g. `range=hours:24`.
 *
 * Spelled out rather than encoded as an ISO-8601 duration so the link stays
 * readable, and so `minutes` can never be confused with `months` the way `PT1M`
 * and `P1M` can.
 */
const asRange = (value: string): RelativeTimeRange | null => {
  const [unit, rawAmount] = value.split(':')
  const amount = Number.parseInt(rawAmount ?? '', 10)

  if (!RANGE_UNITS.includes(unit as RelativeTimeRange['unit'])) {
    return null
  }

  if (!Number.isFinite(amount) || amount <= 0) {
    return null
  }

  return { unit: unit as RelativeTimeRange['unit'], amount }
}

const asPositiveInt = (value: string, fallback: number): number => {
  const parsed = Number.parseInt(value, 10)
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback
}

/**
 * Encode a config + time range as a flat route query.
 *
 * Deliberately positional rather than JSON: a JSON blob of twenty series
 * percent-encodes into something several times longer than the cap, and the point
 * of this state is that a user can copy the address bar and send it to someone.
 */
export const encodeAdhocState = (config: AdhocGraphConfig, time: StartEndTime): RouteQuery => {
  // A relative window travels as the range itself, NOT as the instants it happened
  // to resolve to — otherwise a bookmarked "last two days" is frozen to the two
  // days that were current when the link was made. Only an explicit custom range
  // is written as absolute start/end.
  const query: RouteQuery = time.range ?
    { range: `${time.range.unit}:${time.range.amount}` } :
    {
      start: String(time.startTime),
      end: String(time.endTime),
      fmt: time.format
    }

  if (config.series.length) {
    query.s = config.series.map(series => [
      series.resourceId,
      series.attribute,
      series.aggregation,
      series.label,
      series.style,
      series.color,
      series.hidden ? '1' : '0'
    ].map(escapeField).join(FIELD))
  }

  if (config.expressions.length) {
    query.e = config.expressions.map(expression => [
      expression.label,
      expression.value,
      expression.style,
      expression.color
    ].map(escapeField).join(FIELD))
  }

  if (config.title) {
    query.title = config.title
  }

  if (config.verticalLabel) {
    query.vlabel = config.verticalLabel
  }

  if (config.stacked) {
    query.stacked = '1'
  }

  if (config.resolution !== DEFAULT_RESOLUTION) {
    query.res = String(config.resolution)
  }

  return query
}

/** Rough length of the encoded query, used to decide whether it is still shareable. */
export const encodedQueryLength = (query: RouteQuery): number =>
  Object.entries(query).reduce((total, [key, value]) => {
    const values = many(value)
    const parts = values.length ? values : [first(value)]
    return total + parts.reduce(
      (sum, part) => sum + key.length + encodeURIComponent(part).length + 2,
      0
    )
  }, 0)

/**
 * Rebuild a config + time range from a route query.
 *
 * Never throws and never returns a half-built series: a hand-edited or truncated
 * link should degrade to "the parts that parsed" rather than to a blank page.
 * Returns null when the query carries no ad-hoc state at all.
 */
export const decodeAdhocState = (query: RouteQuery): AdhocUrlState | null => {
  const rawSeries = many(query.s)
  const rawExpressions = many(query.e)
  const start = first(query.start)
  const end = first(query.end)
  const range = asRange(first(query.range))

  if (!rawSeries.length && !rawExpressions.length && !start && !range) {
    return null
  }

  const series: AdhocSeries[] = []
  const takenKeys = new Set<string>()

  for (const entry of rawSeries) {
    const [resourceId, attribute, aggregation, label, style, color, hidden] =
      entry.split(FIELD).map(unescapeField)

    // resourceId + attribute identify the series; without both there is nothing to query.
    if (!resourceId || !attribute) {
      continue
    }

    const key = `${resourceId}|${attribute}`

    if (takenKeys.has(key)) {
      continue
    }
    takenKeys.add(key)

    series.push({
      key,
      label: label || attribute,
      resourceId,
      attribute,
      aggregation: asAggregation(aggregation ?? ''),
      color: asColor(color ?? ''),
      style: asStyle(style ?? ''),
      hidden: hidden === '1'
    })
  }

  const expressions: AdhocExpression[] = []

  for (const [index, entry] of rawExpressions.entries()) {
    const [label, value, style, color] = entry.split(FIELD).map(unescapeField)

    if (!label || !value) {
      continue
    }

    expressions.push({
      id: `expr-${index}`,
      label,
      value,
      style: asStyle(style ?? ''),
      color: asColor(color ?? '')
    })
  }

  const startTime = asPositiveInt(start, 0)
  const endTime = asPositiveInt(end, 0)

  return {
    config: {
      series,
      expressions,
      title: first(query.title),
      verticalLabel: first(query.vlabel),
      stacked: first(query.stacked) === '1',
      resolution: asPositiveInt(first(query.res), DEFAULT_RESOLUTION)
    },
    // A range is returned unresolved: the caller resolves it against the clock at
    // the moment the page loads, which is the whole point.
    time: {
      startTime,
      endTime,
      format: first(query.fmt) || 'hours',
      ...(range ? { range } : {})
    }
  }
}
