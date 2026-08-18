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
import { mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { describe, expect, it } from 'vitest'
import { defineComponent } from 'vue'

// OnmsTooltip wraps PrimeVue's Tooltip directive (see the directive file's
// header comment). Pin that it is a real directive object with lifecycle hooks,
// so a broken re-export fails fast rather than rendering nothing at 14 call
// sites.
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

  // The reason this is a wrapper and not a bare re-export. PrimeVue reads the
  // configured z-index off `binding.instance.$primevue`, which Vue sets to the
  // host's exposeProxy — present on every `<script setup>` component and unable
  // to resolve app globalProperties, so upstream captured nothing and tooltips
  // fell back to ~1000, behind the fixed menubar.
  describe('z-index capture', () => {
    const zIndexOf = (el: Element) => (el as never as Record<string, unknown>).$_ptooltipZIndex

    const mountHost = (host: ReturnType<typeof defineComponent>) => mount(host, {
      global: {
        plugins: [[PrimeVue, { zIndex: { tooltip: 2100 }}]],
        directives: { 'onms-tooltip': OnmsTooltip }
      }
    })

    it('captures the configured z-index from a <script setup>-style host', () => {
      // The bare `expose()` call is what the <script setup> compiler emits for a
      // component with no defineExpose, and it is what defeats the upstream
      // lookup: it gives the instance an exposeProxy, which Vue then hands to the
      // directive as binding.instance.
      const host = defineComponent({
        template: '<span v-onms-tooltip="\'hello\'" class="host">hello</span>',
        setup: (_props, { expose }) => {
          expose()

          return {}
        }
      })

      expect(zIndexOf(mountHost(host).find('.host').element)).toBe(2100)
    })

    it('leaves an already-resolvable instance alone', () => {
      // options-API host: binding.instance is the full proxy, so PrimeVue's own
      // lookup works and the wrapper must not interfere
      const host = defineComponent({
        template: '<span v-onms-tooltip="\'hello\'" class="host">hello</span>'
      })

      expect(zIndexOf(mountHost(host).find('.host').element)).toBe(2100)
    })
  })
})
