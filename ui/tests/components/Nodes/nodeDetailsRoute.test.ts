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
import { NODE_DETAILS_ROUTE_NAME, nodeDetailsRoute, parseNodeRouteId } from '@/components/Nodes/nodeDetailsRoute'

describe('parseNodeRouteId', () => {
  it.each([
    ['42', '42'],
    ['007', '7'],
    [' 42 ', '42']
  ])('reads %j as numeric id %s, as the legacy page did with Integer.parseInt', (raw, id) => {
    expect(parseNodeRouteId(raw)).toEqual({ kind: 'numeric', id })
  })

  it.each([
    'SmokeTests:TestMachine1',
    'My Source:a/b#c',
    'a:b:c'
  ])('reads %j as foreignSource:foreignId', (raw) => {
    expect(parseNodeRouteId(raw)).toEqual({ kind: 'foreign', value: raw })
  })

  // 'nope' and 'a:' make the REST lookup answer 500, so they never reach it.
  it.each(['', 'abc', '0', '-3', '+5', ':b', 'a:', '12.5', '99999999999999999999'])('rejects %j', (raw) => {
    expect(parseNodeRouteId(raw).kind).toBe('invalid')
  })
})

describe('nodeDetailsRoute', () => {
  it('names the Node Details route with a string id', () => {
    expect(nodeDetailsRoute(42)).toEqual({ name: NODE_DETAILS_ROUTE_NAME, params: { id: '42' }})
    expect(nodeDetailsRoute('SmokeTests:TestMachine1')).toEqual({ name: 'Node Details', params: { id: 'SmokeTests:TestMachine1' }})
  })
})
