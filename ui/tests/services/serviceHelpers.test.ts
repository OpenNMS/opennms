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
import { withNodeFilter } from '@/services/serviceHelpers'

describe('withNodeFilter', () => {
  it('filters to the node when the caller has no FIQL of its own', () => {
    expect(withNodeFilter('42')).toEqual({ _s: 'node.id==42' })
  })

  it('keeps the caller\'s other parameters', () => {
    expect(withNodeFilter(42, { limit: 5, offset: 10 })).toEqual({ limit: 5, offset: 10, _s: 'node.id==42' })
  })

  // FIQL binds ; tighter than , -- unparenthesised, `node.id==42;a,b` reads as (node AND a) OR b
  // and returns other nodes' rows that match b.
  it('parenthesises the caller\'s FIQL so an OR in it stays within the node', () => {
    expect(withNodeFilter('42', { _s: 'severity==MAJOR,severity==CRITICAL' }))
      .toEqual({ _s: 'node.id==42;(severity==MAJOR,severity==CRITICAL)' })
  })
})
