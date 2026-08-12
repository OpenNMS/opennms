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

import { OnmsTooltip } from '@opennms/onms-ui'
import { describe, expect, it } from 'vitest'

// OnmsTooltip is a re-export of PrimeVue's Tooltip directive (see the
// directive file's header comment). Pin that it is a real directive object
// with lifecycle hooks, so a broken re-export fails fast rather than
// rendering nothing at 14 call sites.
//
// The hook names asserted below were confirmed against the installed
// primevue@4.5.5 by inspecting Object.keys(await import('primevue/tooltip')
// .default), which returns:
// ['extend', 'created', 'beforeMount', 'mounted', 'beforeUpdate', 'updated',
//  'beforeUnmount', 'unmounted']
describe('OnmsTooltip', () => {
  it('is a directive object with lifecycle hooks', () => {
    expect(OnmsTooltip).toBeTypeOf('object')
    expect(Object.keys(OnmsTooltip as Record<string, unknown>).length).toBeGreaterThan(0)
  })

  it('exposes the real PrimeVue Tooltip directive lifecycle hooks', () => {
    const tooltip = OnmsTooltip as Record<string, unknown>
    expect(tooltip).toHaveProperty('created')
    expect(tooltip).toHaveProperty('beforeMount')
    expect(tooltip).toHaveProperty('mounted')
    expect(tooltip).toHaveProperty('beforeUpdate')
    expect(tooltip).toHaveProperty('updated')
    expect(tooltip).toHaveProperty('beforeUnmount')
    expect(tooltip).toHaveProperty('unmounted')
  })
})
