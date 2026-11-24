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

// The Node List's Flows filter: four on/off choices.
// Ingress and Egress can be on together, which means "either kind". Both and
// No Flows each stand alone. No choice on means no Flows filter.
//
// The values are what the filter stores (queryFilter.selectedFlows, saved
// preferences), so they keep the names the earlier multiselect used.

import { FlowFilter } from '@/types/index'

export type { FlowFilter }

export const FLOW_FILTERS: { value: FlowFilter, label: string }[] = [
  { value: 'Ingress', label: 'Ingress' },
  { value: 'Egress', label: 'Egress' },
  { value: 'Both', label: 'Ingress + Egress' },
  { value: 'No Flows', label: 'No Flow Data' }
]

const isFlowFilter = (value: string): value is FlowFilter => FLOW_FILTERS.some(f => f.value === value)

const combinesWith = (a: FlowFilter, b: FlowFilter) =>
  (a === 'Ingress' && b === 'Egress') || (a === 'Egress' && b === 'Ingress')

export const flowFilterLabel = (value: string) => FLOW_FILTERS.find(f => f.value === value)?.label ?? value

/**
 * The selection after one choice is turned on or off. Turning a choice on turns
 * off every other choice it cannot combine with.
 */
export const toggleFlowFilter = (selected: readonly string[], value: FlowFilter, on: boolean): FlowFilter[] => {
  const kept = normalizeFlowFilters(selected).filter(v => v !== value)

  if (!on) {
    return kept
  }

  const next = [...kept.filter(v => combinesWith(v, value)), value]

  return FLOW_FILTERS.map(f => f.value).filter(v => next.includes(v))
}

/**
 * A selection that the four choices can show. Saved preferences and links can
 * hold combinations the earlier multiselect allowed (e.g. Ingress and No Flows);
 * No Flows wins, as it did in that filter's query, then Both. Unknown values
 * are dropped.
 */
export const normalizeFlowFilters = (values: readonly string[]): FlowFilter[] => {
  const known = values.filter(isFlowFilter)

  if (known.includes('No Flows')) {
    return ['No Flows']
  }

  if (known.includes('Both')) {
    return ['Both']
  }

  return FLOW_FILTERS.map(f => f.value).filter(v => known.includes(v))
}
