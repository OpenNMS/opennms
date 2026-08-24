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

import { AdhocGraphConfig, AdhocSeriesStyle } from '@/types/adhocGraph'
import { jexlToRpn } from './jexlToRpn'
import { expressionReferences, sanitizeLabel } from './adhocQuery'

/**
 * Emit an ad-hoc graph as a prefab graph definition — the `report.<name>.*` block
 * that lives in `$OPENNMS_HOME/etc/snmp-graph.properties.d/`.
 *
 * ## Why this only works for a single resource
 *
 * A prefab graph is a TEMPLATE bound to one resource. The `{rrd1}`..`{rrdN}`
 * placeholders in the command are filled in by
 * `DefaultRrdGraphService.getRrdNames(resource, columns)`, which looks every
 * column up in ONE `OnmsResource` and throws when an attribute is not on it:
 *
 *     throw new IllegalArgumentException("RRD attribute '" + dsNames[i] +
 *         "' is not available on resource '" + resource.getId() + "'.")
 *
 * So a graph whose series span two interfaces — let alone two nodes — has no
 * prefab representation at all. That is a property of the format, not a gap in
 * this code, which is why the caller gates on `describeIneligibility` first.
 *
 * ## Deliberate omissions
 *
 * The time range is not emitted: prefab graphs are time-agnostic and the viewer
 * supplies start/end. Nor are the CDEFs NaN-guarded the way hand-written OpenNMS
 * graphs often are (`CDEF:x=raw,UN,0,raw,IF`) — that guard turns gaps into zeroes,
 * which would make the rendered graph disagree with the ad-hoc graph it came from.
 */

export interface RrdGraphDefinition {
  /** The report id, e.g. `adhoc.interfaceSnmp.ifHCInOctets_ifHCOutOctets`. */
  reportName: string
  /** Distinct datasource names, in `{rrdN}` order. */
  columns: string[]
  /** The resource type the report applies to, e.g. `interfaceSnmp`. */
  type: string
  /** The full `report.<name>.*` block, ready to paste into a properties file. */
  properties: string
  /**
   * The effective command, as the server hands it to RRDtool once the properties
   * file has been read — single-escaped and on one line. `properties` is the same
   * command with backslashes doubled and split across continuation lines, which is
   * what a .properties file requires.
   */
  command: string
}

export interface RrdGraphDefinitionFailure {
  reason: string
}

export type RrdGraphDefinitionResult = RrdGraphDefinition | RrdGraphDefinitionFailure

export const isRrdGraphDefinition = (
  result: RrdGraphDefinitionResult
): result is RrdGraphDefinition => 'properties' in result

/**
 * The OpenNMS resource type embedded in a resource id, e.g. `interfaceSnmp` from
 * `node[1].interfaceSnmp[eth0]`.
 *
 * Parsed from the last bracket rather than by splitting on '.', because the
 * bracketed part routinely contains dots and other punctuation
 * (`node[1].hrStorageIndex[/var/log]`).
 */
export const resourceTypeOf = (resourceId: string): string | null => {
  const lastBracket = resourceId.lastIndexOf('[')

  if (lastBracket <= 0) {
    return null
  }

  const match = /([A-Za-z][A-Za-z0-9_]*)$/.exec(resourceId.slice(0, lastBracket))
  return match?.[1] ?? null
}

/**
 * RRD draw command for a series style. Every style the UI offers has one, which is
 * the point of naming them after these commands in the first place.
 */
const DRAW_COMMANDS: Readonly<Record<AdhocSeriesStyle, string>> = {
  line: 'LINE1',
  line2: 'LINE2',
  line3: 'LINE3',
  area: 'AREA',
  stack: 'AREA'
}

const drawCommandFor = (style: AdhocSeriesStyle): string => DRAW_COMMANDS[style] ?? 'LINE1'

/**
 * Escape text destined for an RRD legend. Colons separate a command's fields, so a
 * literal one has to be escaped. This is the RRD layer only; the extra doubling a
 * .properties file needs is applied once, later, by `escapeForProperties`.
 */
const escapeLegend = (text: string): string => text.replace(/\\/g, '\\\\').replace(/:/g, '\\:')

/**
 * Second escaping layer, applied only when writing the .properties form: java's
 * Properties reader consumes one level of backslash, so every backslash in the
 * effective command must be doubled. This is why the shipped graphs read
 * `"Avg  \\: %8.2lf %s"` on disk while RRDtool receives `"Avg  \: %8.2lf %s"`.
 */
const escapeForProperties = (text: string): string => text.replace(/\\/g, '\\\\')

/** Escape text destined for a quoted `--title` / `--vertical-label` argument. */
const escapeQuoted = (text: string): string =>
  text.replace(/\\/g, '\\\\').replace(/"/g, '\\"').replace(/[\r\n]+/g, ' ').trim()

/** Beyond this many datasources the id stops being readable and gets a count instead. */
const MAX_NAMED_COLUMNS = 4

/**
 * The report id, built from what the graph actually reads: its resource type and
 * its datasource names — `adhoc.interfaceSnmp.ifHCInOctets_ifHCOutOctets`.
 *
 * The resource *type* rather than the resource *id*, deliberately. A prefab graph
 * is a template that renders for every resource of its type, so putting the
 * instance in the id (`node[1]`, `GigabitEthernet0-0-1`) would tell a later reader
 * the graph is specific to that interface when it is not. The instance also drags
 * in `[`, `]` and `:`, all of which need escaping in a .properties key.
 *
 * The human-readable graph title is unaffected — it stays on `report.<id>.name`.
 */
export const reportNameFor = (resourceId: string, columns: string[]): string => {
  const type = resourceTypeOf(resourceId) ?? 'resource'
  const named = columns.slice(0, MAX_NAMED_COLUMNS).map(column => sanitizeLabel(column))
  const remainder = columns.length - named.length
  const suffix = remainder > 0 ? `_and_${remainder}_more` : ''

  return `adhoc.${sanitizeLabel(type)}.${named.join('_')}${suffix}`
}

/** Pad a legend so the GPRINT columns line up, the way the shipped graphs do. */
const padLegend = (text: string, width: number): string =>
  (text.length >= width ? text : text + ' '.repeat(width - text.length))

/**
 * Why this config cannot be expressed as a prefab graph, or null when it can.
 * Checked before emitting so the UI can explain rather than produce a definition
 * that throws at render time.
 */
export const describeIneligibility = (config: AdhocGraphConfig): string | null => {
  if (!config.series.length) {
    return 'There are no series to export yet.'
  }

  const resourceIds = [...new Set(config.series.map(series => series.resourceId))]

  if (resourceIds.length > 1) {
    return 'A graph definition is bound to a single resource, but this graph draws on ' +
      `${resourceIds.length} of them. Narrow the selection to one resource to export it.`
  }

  if (!resourceTypeOf(resourceIds[0])) {
    return `The resource type could not be determined from '${resourceIds[0]}'.`
  }

  for (const expression of config.expressions) {
    const converted = jexlToRpn(expression.value)

    if (!converted.ok) {
      return `Expression '${expression.label}' cannot be converted: ${converted.reason}`
    }

    const known = new Set(config.series.map(series => series.label))
    const unknown = converted.identifiers.filter(identifier => !known.has(identifier))

    if (unknown.length) {
      // An expression referring to another expression would need the CDEFs ordered
      // by dependency; sources only keeps the emitted order trivially correct.
      return `Expression '${expression.label}' references ${unknown.join(', ')}, ` +
        'which is not a source series. Graph definitions can only build on sources.'
    }
  }

  return null
}

/**
 * Build the definition. Returns a failure object when the config is ineligible —
 * the same check the UI runs to decide whether to offer the export at all.
 */
export const buildRrdGraphDefinition = (config: AdhocGraphConfig): RrdGraphDefinitionResult => {
  const ineligible = describeIneligibility(config)

  if (ineligible) {
    return { reason: ineligible }
  }

  const title = config.title.trim() || 'Custom performance graph'
  const type = resourceTypeOf(config.series[0].resourceId) as string

  // {rrdN} is positional over `columns`, so two series reading the same attribute
  // with different consolidation functions share one column and one placeholder.
  const columns: string[] = []

  for (const series of config.series) {
    if (!columns.includes(series.attribute)) {
      columns.push(series.attribute)
    }
  }

  const reportName = reportNameFor(config.series[0].resourceId, columns)

  const lines: string[] = [
    `--title="${escapeQuoted(title)}"`
  ]

  if (config.verticalLabel.trim()) {
    lines.push(`--vertical-label="${escapeQuoted(config.verticalLabel)}"`)
  }

  for (const series of config.series) {
    const placeholder = `{rrd${columns.indexOf(series.attribute) + 1}}`
    lines.push(`DEF:${series.label}=${placeholder}:${series.attribute}:${series.aggregation}`)
  }

  for (const expression of config.expressions) {
    const converted = jexlToRpn(expression.value)

    if (!converted.ok) {
      return { reason: converted.reason }
    }

    lines.push(`CDEF:${expression.label}=${converted.rpn}`)
  }

  // A source that is hidden AND consumed keeps its DEF (the CDEF needs it) but is
  // not drawn — the same rule the measurements query applies via `transient`.
  const drawn = [
    ...config.series.filter(series =>
      !(series.hidden && config.expressions.some(expression =>
        expressionReferences(expression.value, series.label)))),
    ...config.expressions
  ]

  const legendWidth = Math.max(...drawn.map(item => item.label.length))

  for (const item of drawn) {
    const draw = drawCommandFor(item.style)
    const stack = item.style === 'stack' ? ':STACK' : ''
    const legend = escapeLegend(padLegend(item.label, legendWidth))

    lines.push(`${draw}:${item.label}${item.color}:"${legend}"${stack}`)
    lines.push(`GPRINT:${item.label}:AVERAGE:"Avg \\: %8.2lf %s"`)
    lines.push(`GPRINT:${item.label}:MIN:"Min \\: %8.2lf %s"`)
    lines.push(`GPRINT:${item.label}:MAX:"Max \\: %8.2lf %s"`)
    lines.push(`GPRINT:${item.label}:LAST:"Last \\: %8.2lf %s\\n"`)
  }

  const command = lines.join(' ')

  // Properties-file form: escape once more, then break across continuation lines.
  // Every line but the last ends in a backslash; the one-space indent is only for
  // legibility, since leading whitespace is stripped when the file is read.
  const propertiesCommand = lines.map(escapeForProperties).join(' \\\n ')

  // A report is only rendered if its name appears in the file's `reports=` list.
  // Emitting the block alone produced a file that parsed fine and drew nothing —
  // so the list comes with it, ready to use as a standalone file, with a note for
  // the other case, since a second `reports=` key would override the first rather
  // than extend it.
  const properties = [
    `reports=${reportName}`,
    '',
    '# Appending to an existing file? Do not paste the line above — add',
    `# ${reportName} to that file's existing reports= list instead.`,
    '',
    `report.${reportName}.name=${escapeForProperties(title)}`,
    `report.${reportName}.columns=${columns.join(',')}`,
    `report.${reportName}.type=${type}`,
    `report.${reportName}.command=${propertiesCommand}`
  ].join('\n')

  return { reportName, columns, type, properties, command }
}
