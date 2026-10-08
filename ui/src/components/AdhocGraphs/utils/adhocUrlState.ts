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
  AdhocExpression,
  AdhocGraphConfig,
  AdhocSelectionState,
  AdhocSeriesOverride,
  AdhocSeriesStyle
} from '@/types/adhocGraph'
import { ConsolidationFunctionType } from '@/types/timeSeries'
import { RelativeTimeRange, StartEndTime } from '@/types'
import { RANGE_UNITS } from '@/components/Common/utils/timeRangeOptions'
import { datasourceKey, nodeCriteriaOf, splitDatasourceKey } from './adhocIds'
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

/**
 * Everything a link carries besides the graph config and time range: the picker
 * (filters and picks), the per-series edits to re-apply once the picker has been
 * evaluated, and how many series the graph had when the link was made, so the
 * page can say when a re-evaluated link now draws something different.
 */
export interface AdhocLinkState {
  selection: AdhocSelectionState
  overrides: Record<string, AdhocSeriesOverride>
  seriesCount: number
}

export interface AdhocUrlState {
  config: AdhocGraphConfig
  time: StartEndTime
  link: AdhocLinkState
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

const isStyle = (value: string): value is AdhocSeriesStyle => STYLES.includes(value as AdhocSeriesStyle)

const asStyle = (value: string): AdhocSeriesStyle => (isStyle(value) ? value : 'line')

const isAggregation = (value: string): value is ConsolidationFunctionType =>
  AGGREGATIONS.includes(value as ConsolidationFunctionType)

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

const isStatic = (selection: AdhocSelectionState): boolean =>
  !selection.nodeFilter && !selection.resourceFilter && !selection.datasourceFilter

const resourcesOf = (datasourceKeys: string[]): string[] =>
  [...new Set(datasourceKeys.map(key => splitDatasourceKey(key).resourceId))]

const nodesOf = (resourceIds: string[]): string[] =>
  [...new Set(resourceIds.map(id => nodeCriteriaOf(id) ?? '').filter(Boolean))]

const sameSet = (a: string[], b: string[]): boolean =>
  a.length === b.length && a.every(item => b.includes(item))

/**
 * One override entry: `resourceId~attribute~aggregation~label~style~color~hidden`,
 * with a blank for anything not overridden. The same positional shape the old
 * static `s` entries used, so a legacy link reads as a set of fully-specified
 * overrides.
 */
const encodeOverride = (key: string, override: AdhocSeriesOverride): string | null => {
  const { resourceId, attribute } = splitDatasourceKey(key)

  if (!attribute) {
    return null
  }

  const fields = [
    resourceId,
    attribute,
    override.aggregation ?? '',
    override.label ?? '',
    override.style ?? '',
    override.color ?? '',
    override.hidden === undefined ? '' : (override.hidden ? '1' : '0')
  ]

  // Nothing overridden means nothing to carry.
  if (fields.slice(2).every(field => field === '')) {
    return null
  }

  return fields.map(escapeField).join(FIELD)
}

const decodeOverride = (entry: string): [string, AdhocSeriesOverride] | null => {
  const [resourceId, attribute, aggregation, label, style, color, hidden] = entry.split(FIELD).map(unescapeField)

  if (!resourceId || !attribute) {
    return null
  }

  const override: AdhocSeriesOverride = {}

  if (aggregation && isAggregation(aggregation)) {
    override.aggregation = aggregation
  }

  if (label) {
    override.label = label
  }

  if (style && isStyle(style)) {
    override.style = style
  }

  if (color && asColor(color)) {
    override.color = asColor(color)
  }

  if (hidden === '1' || hidden === '0') {
    override.hidden = hidden === '1'
  }

  return [datasourceKey(resourceId, attribute), override]
}

/**
 * Encode a config + time range + picker as a flat route query.
 *
 * Deliberately positional rather than JSON: a JSON blob of twenty series
 * percent-encodes into something several times longer than the cap, and the point
 * of this state is that a user can copy the address bar and send it to someone.
 *
 * The series themselves are NOT written. They are whatever the filters and picks
 * produce when the link is opened, which is what makes a link to "all production
 * routers" follow inventory. Only the per-series edits travel, keyed by series.
 */
export const encodeAdhocState = (config: AdhocGraphConfig, time: StartEndTime, link: AdhocLinkState): RouteQuery => {
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

  const { selection, overrides, seriesCount } = link

  if (selection.nodeFilter) {
    query.nf = selection.nodeFilter
  }

  if (selection.resourceFilter) {
    query.rf = selection.resourceFilter
  }

  if (selection.datasourceFilter) {
    query.df = selection.datasourceFilter
  }

  // A static link (no filters) is defined by its datasources alone: the resource
  // and node picks are exactly what those datasources belong to, and writing them
  // out would triple the length of every link made before filters existed.
  const derivable = isStatic(selection) && sameSet(selection.pickedResourceIds, resourcesOf(selection.pickedDatasourceKeys)) &&
    sameSet(selection.pickedNodeIds, nodesOf(selection.pickedResourceIds))

  if (selection.pickedNodeIds.length && !derivable) {
    query.pn = selection.pickedNodeIds
  }

  if (selection.pickedResourceIds.length && !derivable) {
    query.pr = selection.pickedResourceIds
  }

  const encodedOverrides = Object.entries(overrides)
    .map(([key, override]) => [key, encodeOverride(key, override)] as const)
    .filter((pair): pair is readonly [string, string] => pair[1] !== null)

  // Likewise, a static link whose every datasource has an override needs no
  // separate list of datasources: the overrides name them.
  const datasourcesImplied = isStatic(selection) && selection.pickedDatasourceKeys.length > 0 &&
    sameSet(selection.pickedDatasourceKeys, encodedOverrides.map(([key]) => key))

  if (selection.pickedDatasourceKeys.length && !datasourcesImplied) {
    query.pd = selection.pickedDatasourceKeys
  }

  if (encodedOverrides.length) {
    query.o = encodedOverrides.map(([, entry]) => entry)
  }

  if (seriesCount > 0) {
    query.n = String(seriesCount)
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
 * Rebuild a config + time range + picker from a route query.
 *
 * Never throws and never returns a half-built entry: a hand-edited or truncated
 * link should degrade to "the parts that parsed" rather than to a blank page.
 * Returns null when the query carries no ad-hoc state at all.
 *
 * A link from before filters existed carries its series as `s` entries. Those are
 * read as explicit picks of each node, resource and datasource plus a full set of
 * overrides, which draws exactly the graph the link was made for.
 */
export const decodeAdhocState = (query: RouteQuery): AdhocUrlState | null => {
  const legacySeries = many(query.s)
  const rawOverrides = many(query.o)
  const rawExpressions = many(query.e)
  const start = first(query.start)
  const end = first(query.end)
  const range = asRange(first(query.range))

  const selection: AdhocSelectionState = {
    nodeFilter: first(query.nf),
    resourceFilter: first(query.rf),
    datasourceFilter: first(query.df),
    pickedNodeIds: many(query.pn),
    pickedResourceIds: many(query.pr),
    pickedDatasourceKeys: many(query.pd)
  }

  const hasSelection = Boolean(selection.nodeFilter || selection.resourceFilter || selection.datasourceFilter ||
    selection.pickedNodeIds.length || selection.pickedResourceIds.length || selection.pickedDatasourceKeys.length)

  if (!legacySeries.length && !rawOverrides.length && !rawExpressions.length && !start && !range && !hasSelection) {
    return null
  }

  const overrides: Record<string, AdhocSeriesOverride> = {}

  // A legacy entry spells out every field, defaults included. Only what differs
  // from the generated series is an override; the rest would just pad the link.
  for (const entry of legacySeries) {
    const decoded = decodeOverride(entry)

    if (decoded) {
      const [key, full] = decoded
      const override: AdhocSeriesOverride = {}

      if (full.label) {
        override.label = full.label
      }

      if (full.color) {
        override.color = full.color
      }

      if (full.aggregation && full.aggregation !== ConsolidationFunctionType.AVERAGE) {
        override.aggregation = full.aggregation
      }

      if (full.style && full.style !== 'line') {
        override.style = full.style
      }

      if (full.hidden) {
        override.hidden = true
      }

      overrides[key] = override
    }
  }

  for (const entry of rawOverrides) {
    const decoded = decodeOverride(entry)

    if (decoded) {
      overrides[decoded[0]] = { ...overrides[decoded[0]], ...decoded[1] }
    }
  }

  // Legacy series become picks at every level, so the three panes show them and
  // the graph is exactly what was shared.
  if (legacySeries.length && !hasSelection) {
    const keys = new Set<string>()

    for (const entry of legacySeries) {
      const decoded = decodeOverride(entry)

      if (decoded) {
        keys.add(decoded[0])
      }
    }

    selection.pickedDatasourceKeys = [...keys]
  }

  // A static link may leave its datasources implied by its overrides, and its
  // resource and node picks implied by its datasources.
  if (isStatic(selection) && !selection.pickedDatasourceKeys.length && rawOverrides.length) {
    selection.pickedDatasourceKeys = Object.keys(overrides)
  }

  if (isStatic(selection) && selection.pickedDatasourceKeys.length) {
    if (!selection.pickedResourceIds.length) {
      selection.pickedResourceIds = resourcesOf(selection.pickedDatasourceKeys)
    }

    if (!selection.pickedNodeIds.length) {
      selection.pickedNodeIds = nodesOf(selection.pickedResourceIds)
    }
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
      series: [],
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
    },
    link: {
      selection,
      overrides,
      seriesCount: asPositiveInt(first(query.n), 0)
    }
  }
}
