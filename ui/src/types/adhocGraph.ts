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

import { ConsolidationFunctionType } from '@/types/timeSeries'

/**
 * How a single series is drawn, named after the RRDtool draw commands these map
 * onto: `line`/`line2`/`line3` are LINE1/LINE2/LINE3 and differ only in stroke
 * weight, exactly as they do in a prefab graph definition.
 *
 * Chart.js has no native "stack" type (stacking is a scale-level concern), so
 * `stack` means "filled area, and switch the y scale to stacked", matching how the
 * prefab graphs express AREA vs STACK.
 */
export type AdhocSeriesStyle = 'line' | 'line2' | 'line3' | 'area' | 'stack'

/**
 * A node the picker knows about. `id` is whatever criterion the server will accept
 * for the node: the database id when it came from a search, or the id form found in
 * a resource id (`1` or `fs:fid`) when it came from a link. The foreign-source pair
 * and location ride along only to be shown.
 */
export interface AdhocNodeOption {
  id: string
  label: string
  foreignSource?: string
  foreignId?: string
  location?: string
}

/**
 * A graphable resource beneath a node, as /rest/resources/fornode lists it. `name`
 * is the instance name (`eth0-005056b6b6b6`), distinct from the human `label`.
 * `attributes` are the graphable attribute names, so the datasource column never
 * needs a request of its own.
 */
export interface AdhocResourceOption {
  id: string
  label: string
  name: string
  typeLabel: string
  nodeId: string
  nodeLabel: string
  attributes: string[]
}

/**
 * One graphable attribute on one resource. `key` is the selection identity used
 * everywhere (list selection, series lookup, URL state) — a resource id alone is not
 * unique because a resource exposes many attributes.
 */
export interface AdhocDatasourceOption {
  key: string
  resourceId: string
  resourceLabel: string
  nodeId: string
  nodeLabel: string
  attribute: string
}

/**
 * A plotted source series. `label` doubles as the JEXL identifier an expression can
 * reference, so it is sanitized and unique across the config (see adhocQuery.ts).
 * `hidden` maps to the measurements API's `transient` flag: fetch the data so
 * expressions can use it, but do not return it as a column to plot.
 */
export interface AdhocSeries {
  key: string
  label: string
  resourceId: string
  attribute: string
  aggregation: ConsolidationFunctionType
  color: string
  style: AdhocSeriesStyle
  hidden: boolean
}

/**
 * The parts of a series the user changed by hand. Series are generated from the
 * filters, so an edit has to live apart from the series it decorates: when the
 * filters re-evaluate, the generated series is rebuilt and the override re-applied.
 */
export type AdhocSeriesOverride = Partial<Pick<AdhocSeries, 'label' | 'aggregation' | 'color' | 'style' | 'hidden'>>

/** A derived series computed server-side from source labels via a JEXL expression. */
export interface AdhocExpression {
  id: string
  label: string
  value: string
  color: string
  style: AdhocSeriesStyle
}

/** Everything that defines an ad-hoc graph, minus the time range. */
export interface AdhocGraphConfig {
  series: AdhocSeries[]
  expressions: AdhocExpression[]
  title: string
  verticalLabel: string
  stacked: boolean
  /** Target number of data points across the range; drives the computed step. */
  resolution: number
}

/**
 * What the three picker columns are set to: a filter per column, and the items
 * the user picked by hand in each. An empty pick list means "everything the
 * filter matches", so a graph can be defined entirely by filters and grow with
 * inventory, or pinned down item by item, or anything in between.
 */
export interface AdhocSelectionState {
  nodeFilter: string
  resourceFilter: string
  datasourceFilter: string
  pickedNodeIds: string[]
  pickedResourceIds: string[]
  pickedDatasourceKeys: string[]
}
