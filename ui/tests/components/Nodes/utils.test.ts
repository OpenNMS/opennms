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

import { describe, expect, test } from 'vitest'
import { mock } from 'vitest-mock-extended'
import { getTableCssClasses, hasEgressFlow, hasIngressFlow } from '@/components/Nodes/utils'
import { Node, NodeColumnSelectionItem } from '@/types'

describe('Nodes utils test', () => {
  test('test getTableCssClasses', async () => {
    let result = getTableCssClasses([])
    expect(result).toEqual(['tl1'])

    const defaultColumns: NodeColumnSelectionItem[] = [
      { id: 'id', label: 'ID', selected: true, order: 0 },
      { id: 'label', label: 'Node Label', selected: true, order: 1 },
      { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
      { id: 'location', label: 'Location', selected: false, order: 3 },
      { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
      { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
      { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
      { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
      { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
      { id: 'flows', label: 'Flows', selected: true, order: 9 }
    ]

    result = getTableCssClasses(defaultColumns)
    
    // tl1: implicit 'action' column
    // tr2: 'id' column is right-aligned
    // tc10: 'flows' column is center-aligned
    // 10 items: implicit action + 9 selected columns ('location' is unselected)
    expect(result).toEqual(['tl1', 'tl2', 'tl3', 'tl4', 'tl5', 'tl6', 'tl7', 'tl8', 'tl9', 'tc10'])
  })

  test('test hasEgressFlow, hasIngressFlow', async () => {
    const both = mock<Node>()
    both.lastEgressFlow = 1699909194000
    both.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(both)).toBeTruthy()
    expect(hasIngressFlow(both)).toBeTruthy()

    const egress = mock<Node>()
    egress.lastEgressFlow = 1699909194000
    egress.lastIngressFlow = 0

    expect(hasEgressFlow(egress)).toBeTruthy()
    expect(hasIngressFlow(egress)).toBeFalsy()

    const ingress = mock<Node>()
    ingress.lastEgressFlow = 0
    ingress.lastIngressFlow = 1699909194000

    expect(hasEgressFlow(ingress)).toBeFalsy()
    expect(hasIngressFlow(ingress)).toBeTruthy()

    const neither = mock<Node>()
    neither.lastEgressFlow = 0
    neither.lastIngressFlow = 0

    expect(hasEgressFlow(neither)).toBeFalsy()
    expect(hasIngressFlow(neither)).toBeFalsy()
  })
})
