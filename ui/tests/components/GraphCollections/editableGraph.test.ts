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

import { describe, expect, it } from 'vitest'
import { toEditableGraphs, toStoredGraph } from '@/components/GraphCollections/editableGraph'
import { GraphCollectionGraph, ResolvedGraphCollectionGraph } from '@/types/graphCollection'

const stored: GraphCollectionGraph[] = [
  { title: 'Old', resourceId: null, graphtype: 'mib2.bits', timespan: '7_day', nodeId: '1', interfaceId: 'eth0' },
  { title: 'Gone', resourceId: null, graphtype: 'mib2.bits', timespan: '7_day', nodeId: '9' }
]

const resolved: ResolvedGraphCollectionGraph[] = [
  {
    index: 0, kind: 'prefab', title: 'Old', timespan: '7_day', graphtype: 'mib2.bits', resourceId: 'node[1].interfaceSnmp[eth0]',
    resource: { id: 'node[1].interfaceSnmp[eth0]', label: 'eth0', typeName: 'interfaceSnmp', typeLabel: 'SNMP Interface Data', link: null, parent: { id: 'node[1]', label: 'rtr-01', typeName: 'node', typeLabel: 'Node', link: null }},
    valid: true, start: 0, end: 3600000
  },
  { index: 1, kind: 'prefab', title: 'Gone', timespan: '7_day', graphtype: 'mib2.bits', resourceId: null, resource: null, valid: false, error: 'The resource this graph refers to no longer exists.', start: 0, end: 3600000 }
]

describe('editableGraph', () => {
  it('gives an older entry the resource id the server resolved for it', () => {
    const [old, gone] = toEditableGraphs(stored, resolved)
    expect(old).toMatchObject({ resourceId: 'node[1].interfaceSnmp[eth0]', resourceLabel: 'eth0', parentLabel: 'rtr-01', nodeId: '1', valid: true })
    // nothing resolved, so the older address is all there is
    expect(gone).toMatchObject({ resourceId: null, nodeId: '9', valid: false })
  })

  it('stores one locator: the resourceId when there is one, the older address otherwise', () => {
    const [old, gone] = toEditableGraphs(stored, resolved)
    expect(toStoredGraph(old)).toEqual({
      kind: 'prefab', title: 'Old', resourceId: 'node[1].interfaceSnmp[eth0]', graphtype: 'mib2.bits', timespan: '7_day', extlink: null,
      nodeId: null, nodeSource: null, domain: null, interfaceId: null, adhocState: null
    })
    expect(toStoredGraph(gone)).toMatchObject({ resourceId: null, nodeId: '9', nodeSource: null, domain: null, interfaceId: null })
  })

  it('stores an ad-hoc entry as its builder state alone', () => {
    const [adhoc] = toEditableGraphs([
      { kind: 'adhoc', title: 'CPU', resourceId: null, graphtype: null, timespan: '1_day', adhocState: 'nf=rtr&df=CpuRawUser&n=3' }
    ], [{ index: 0, kind: 'adhoc', title: 'CPU', timespan: '1_day', graphtype: null, resourceId: null, adhocState: 'nf=rtr&df=CpuRawUser&n=3', valid: true, start: 0, end: 3600000 }])
    expect(adhoc).toMatchObject({ kind: 'adhoc', adhocState: 'nf=rtr&df=CpuRawUser&n=3', valid: true })
    expect(toStoredGraph({ ...adhoc, resourceId: 'node[1].nodeSnmp[]', graphtype: 'leftover', nodeId: '1' })).toEqual({
      kind: 'adhoc', title: 'CPU', resourceId: null, graphtype: null, timespan: '1_day', adhocState: 'nf=rtr&df=CpuRawUser&n=3', extlink: null,
      nodeId: null, nodeSource: null, domain: null, interfaceId: null
    })
  })
})
