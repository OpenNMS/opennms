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
import { getNodeSnmpInterfaceQuery } from '@/services/snmpInterfaceService'

describe('snmpInterfaceService', () => {
  describe('getNodeSnmpInterfaceQuery', () => {
    it('builds a query for a single node id with no narrowing', () => {
      expect(getNodeSnmpInterfaceQuery(['1'])).toEqual('(node.id==1)')
    })

    it('builds an OR-joined query for multiple node ids with no narrowing', () => {
      expect(getNodeSnmpInterfaceQuery(['1', '2', '3'])).toEqual('(node.id==1,node.id==2,node.id==3)')
    })

    it('ANDs a narrowing expression onto a single node id as its own parenthesized group', () => {
      expect(getNodeSnmpInterfaceQuery(['1'], 'physAddr==*aabb*')).toEqual('(node.id==1);(physAddr==*aabb*)')
    })

    it('ANDs a narrowing expression onto the whole OR-group of node ids, not just the last one', () => {
      expect(getNodeSnmpInterfaceQuery(['1', '2'], 'physAddr==*aabb*')).toEqual(
        '(node.id==1,node.id==2);(physAddr==*aabb*)'
      )
    })
  })
})
