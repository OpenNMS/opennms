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
  Node,
  NodeColumnSelectionItem
} from '@/types'
import { isNumber } from '@/lib/utils'

/**
 * Construct an array of Feather Table CSS classes for the given configured node table columns. 
 * These start with 't', then ('l', 'r', 'c') for (left, right, center), then the 1 based column index.
 * e.g. 'tl1': left-align 1st column
 * 'tr7': right-align 7th colunn
 */
export const getTableCssClasses = (columns: NodeColumnSelectionItem[]) => {
  const classes: string[] = columns.filter(col => col.selected).map((col, i) => {
    let t = 'tl'

    if (col.id === 'flows') {
      t = 'tc'
    }

    // +2 : one since Feather table column classes are 1-based, one for the first action column which isn't in 'columns'
    return `${t}${i + 2}`
  })

  // add 'action' column
  return ['tl1', ...classes]
}

export const hasIngressFlow = (node: Node) => {
  return node.lastIngressFlow && isNumber(node.lastIngressFlow)
}

export const hasEgressFlow = (node: Node) => {
  return node.lastEgressFlow && isNumber(node.lastEgressFlow)
}

export const defaultColumns: NodeColumnSelectionItem[] = [
  { id: 'id', label: 'ID', selected: false, order: 0 },
  { id: 'label', label: 'Node Label', selected: true, order: 1 },
  { id: 'ipaddress', label: 'IP Address', selected: true, order: 2 },
  { id: 'location', label: 'Location', selected: true, order: 3 },
  { id: 'foreignSource', label: 'Foreign Source', selected: true, order: 4 },
  { id: 'foreignId', label: 'Foreign ID', selected: true, order: 5 },
  { id: 'sysContact', label: 'Sys Contact', selected: true, order: 6 },
  { id: 'sysLocation', label: 'Sys Location', selected: true, order: 7 },
  { id: 'sysDescription', label: 'Sys Description', selected: true, order: 8 },
  { id: 'flows', label: 'Flows', selected: true, order: 9 }
]