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
import { getNodeIpInterfaceQuery } from '@/services/ipInterfaceService'

describe('ipInterfaceService', () => {
  describe('getNodeIpInterfaceQuery', () => {
    it('builds a query for a single node id without the managed-only filter', () => {
      expect(getNodeIpInterfaceQuery(['1'], false)).toEqual('(node.id==1)')
    })

    it('builds an OR-joined query for multiple node ids without the managed-only filter', () => {
      expect(getNodeIpInterfaceQuery(['1', '2', '3'], false)).toEqual('(node.id==1,node.id==2,node.id==3)')
    })

    it('ANDs the managed-only filter onto a single node id as its own parenthesized group', () => {
      expect(getNodeIpInterfaceQuery(['1'], true)).toEqual('(node.id==1);(isManaged==M)')
    })

    it('ANDs the managed-only filter onto the whole OR-group of node ids, not just the last one', () => {
      // FIQL ';' (AND) binds tighter than ',' (OR): the old single-group form
      // '(node.id==1,node.id==2;isManaged==M)' meant 'node.id==1 OR (node.id==2 AND isManaged==M)',
      // returning ALL of node 1's interfaces even when managedOnly was requested.
      expect(getNodeIpInterfaceQuery(['1', '2'], true)).toEqual('(node.id==1,node.id==2);(isManaged==M)')
    })
  })
})
