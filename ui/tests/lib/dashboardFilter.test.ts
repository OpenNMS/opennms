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
import { filterFiqlClauses, isFilterActive, MAX_NODE_IDS } from '@/lib/dashboardFilter'

const filter = (over: Partial<{ surveillanceCategories: string[]; ipMatch: string | null }> = {}) => ({
  surveillanceCategories: over.surveillanceCategories ?? [],
  ipMatch: over.ipMatch ?? null
})

describe('dashboard filter helper', () => {
  it('isFilterActive reflects categories or IP', () => {
    expect(isFilterActive(filter())).toBe(false)
    expect(isFilterActive(filter({ ipMatch: '10.0.0.1' }))).toBe(true)
    expect(isFilterActive(filter({ surveillanceCategories: ['Routers'] }))).toBe(true)
  })

  it('emits an IP clause and no node group when only IP is set', () => {
    expect(filterFiqlClauses(filter({ ipMatch: '10.1.*.*' }), null)).toEqual(['ipInterface.ipAddress==10.1.*.*'])
  })

  it('groups the resolved node ids as an OR set', () => {
    expect(filterFiqlClauses(filter(), [1, 2, 3])).toEqual(['(node.id==1,node.id==2,node.id==3)'])
  })

  it('matches nothing when a category resolves to zero nodes', () => {
    expect(filterFiqlClauses(filter(), [])).toEqual(['node.id==-1'])
  })

  it('uses the root id property when the nodes endpoint itself is queried', () => {
    // /api/v2/nodes has no `node` alias: `node.id` is unmapped there and 500s
    expect(filterFiqlClauses(filter(), [1, 2], 'id')).toEqual(['(id==1,id==2)'])
    expect(filterFiqlClauses(filter(), [], 'id')).toEqual(['id==-1'])
  })

  it('caps the node id group at MAX_NODE_IDS', () => {
    const ids = Array.from({ length: MAX_NODE_IDS + 50 }, (_, n) => n + 1)
    const [clause] = filterFiqlClauses(filter(), ids)
    expect(clause.split(',')).toHaveLength(MAX_NODE_IDS)
  })
})
