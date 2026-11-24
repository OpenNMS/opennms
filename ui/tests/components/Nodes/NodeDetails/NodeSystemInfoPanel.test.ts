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

import NodeSystemInfoPanel from '@/components/Nodes/NodeDetails/NodeSystemInfoPanel.vue'
import { Node } from '@/types/index'
import { mount } from '@vue/test-utils'
import { describe, expect, it } from 'vitest'

const snmpNode = {
  id: '42',
  label: 'srv-42',
  sysName: 'srv-42.example.com',
  sysObjectId: '.1.3.6.1.4.1.8072.3.2.10',
  sysLocation: 'Rack 7',
  sysContact: 'ops@example.com',
  sysDescription: 'Linux srv-42 6.1.0'
} as Node

const mountPanel = (node: Node) => mount(NodeSystemInfoPanel, { props: { node }})

// Each column's fields, as [label, text] in the order shown.
const columns = (wrapper: ReturnType<typeof mountPanel>) =>
  wrapper.findAll('.onms-col-6').map(column => column.findAll('[data-test="field"]')
    .map(field => [field.find('.label').text(), field.findAll('span')[1].text()]))

describe('NodeSystemInfoPanel.vue', () => {
  it('is titled System Information', () => {
    expect(mountPanel(snmpNode).find('.title').text()).toBe('System Information')
  })

  it('shows name, object id and location in the first column, contact and description in the second', () => {
    expect(columns(mountPanel(snmpNode))).toEqual([
      [['Sys Name', 'srv-42.example.com'], ['Sys Object ID', '.1.3.6.1.4.1.8072.3.2.10'], ['Sys Location', 'Rack 7']],
      [['Sys Contact', 'ops@example.com'], ['Sys Description', 'Linux srv-42 6.1.0']]
    ])
  })

  // A node not polled over SNMP has none of these.
  it('shows -- for a value the node does not have', () => {
    expect(columns(mountPanel({ id: '43', label: 'no-snmp', sysName: '' } as Node)).flat().map(([, text]) => text))
      .toEqual(['--', '--', '--', '--', '--'])
  })
})
