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
import {
  placedIdFor,
  paletteIdFromPlacedId,
  isLabelId,
  nodeIdFromPlacedId
} from '@/components/Topology/nodeIds'

describe('topology node id helpers', () => {
  it('round-trips a placed-node id', () => {
    expect(placedIdFor('42')).toBe('placed-42')
    expect(paletteIdFromPlacedId('placed-42')).toBe('42')
    expect(paletteIdFromPlacedId('label-3')).toBeNull()
  })

  it('classifies label ids', () => {
    expect(isLabelId('label-1')).toBe(true)
    expect(isLabelId('placed-1')).toBe(false)
    expect(isLabelId('edge-1')).toBe(false)
  })

  it('extracts a numeric OnmsNode id only for numeric placed ids', () => {
    expect(nodeIdFromPlacedId('placed-42')).toBe(42)
    expect(nodeIdFromPlacedId('placed-abc')).toBeNull()
    expect(nodeIdFromPlacedId('label-1')).toBeNull()
    expect(nodeIdFromPlacedId('edge-7')).toBeNull()
  })
})
