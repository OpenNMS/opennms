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

import { FlowFilter, flowFilterLabel, normalizeFlowFilters, toggleFlowFilter } from '@/components/Nodes/NodeList/flowFilter'
import { describe, expect, it } from 'vitest'

describe('flowFilter', () => {
  describe('toggleFlowFilter', () => {
    it.each<[string, FlowFilter[], FlowFilter, boolean, FlowFilter[]]>([
      ['turns on Ingress alone', [], 'Ingress', true, ['Ingress']],
      ['keeps Ingress on with Egress (either kind)', ['Ingress'], 'Egress', true, ['Ingress', 'Egress']],
      ['keeps Egress on with Ingress', ['Egress'], 'Ingress', true, ['Ingress', 'Egress']],
      ['turns off Both for Ingress', ['Both'], 'Ingress', true, ['Ingress']],
      ['turns off No Flows for Egress', ['No Flows'], 'Egress', true, ['Egress']],
      ['turns off Ingress and Egress for Both', ['Ingress', 'Egress'], 'Both', true, ['Both']],
      ['turns off Both for No Flows', ['Both'], 'No Flows', true, ['No Flows']],
      ['turns off Ingress and Egress for No Flows', ['Ingress', 'Egress'], 'No Flows', true, ['No Flows']],
      ['turns off one of Ingress and Egress', ['Ingress', 'Egress'], 'Ingress', false, ['Egress']],
      ['turns off the last choice: no filter', ['Both'], 'Both', false, []]
    ])('%s', (_title, selected, value, on, expected) => {
      expect(toggleFlowFilter(selected, value, on)).toEqual(expected)
    })
  })

  describe('normalizeFlowFilters', () => {
    it.each<[string, string[], FlowFilter[]]>([
      ['keeps Ingress and Egress, in button order', ['Egress', 'Ingress'], ['Ingress', 'Egress']],
      ['lets No Flows win, as the earlier filter\'s query did', ['Ingress', 'Egress', 'No Flows'], ['No Flows']],
      ['lets Both win over Ingress and Egress', ['Ingress', 'Both'], ['Both']],
      ['drops unknown values', ['lastIngressFlow', 'Egress'], ['Egress']],
      ['keeps an empty selection empty', [], []]
    ])('%s', (_title, values, expected) => {
      expect(normalizeFlowFilters(values)).toEqual(expected)
    })
  })

  it('labels each choice as its button does', () => {
    expect(['Ingress', 'Egress', 'Both', 'No Flows'].map(flowFilterLabel)).toEqual(['Ingress', 'Egress', 'Ingress + Egress', 'No Flow Data'])
  })
})
