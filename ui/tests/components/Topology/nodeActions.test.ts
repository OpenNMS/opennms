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

import { describe, it, expect } from 'vitest'
import { nodeActionLinks } from '@/components/Topology/nodeActions'

describe('nodeActionLinks', () => {
  it('builds the node-data cross-links for a node id', () => {
    expect(nodeActionLinks(42)).toEqual([
      { label: 'Node Details', url: '/opennms/element/node.jsp?node=42' },
      { label: 'Resource Graphs', url: '/opennms/graph/chooseresource.jsp?node=42' },
      { label: 'Events', url: '/opennms/event/list?filter=node%3D42' },
      { label: 'Alarms', url: '/opennms/alarm/list.htm?filter=node%3D42' }
    ])
  })

  it('encodes the node filter so it is a single query value', () => {
    const events = nodeActionLinks(7).find(l => l.label === 'Events')!
    expect(events.url).toContain('filter=node%3D7')
  })
})
