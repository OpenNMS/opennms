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

import Tooltip from 'primevue/tooltip'
import type { DirectiveBinding, ObjectDirective, VNode } from 'vue'

// Seam re-export (NMS-20054) of PrimeVue's Tooltip directive. The host app
// registers it as `v-onms-tooltip` (ui/src/theme/primevue-setup.ts), so app
// and plugin templates carry OpenNMS vocabulary instead of PrimeVue's.
// PrimeVue keys the directive's internals (pt name, data-pc-name, z-index
// bucket) off BaseTooltip.extend('tooltip', ...), not the registration name,
// so the rename is behavior-neutral (verified against primevue@4.5.5).
//
// It is not a bare re-export, because of one upstream bug (NMS-20162):
//
// PrimeVue's Tooltip captures the configured tooltip z-index in `beforeMount`,
// and only there, by reading `binding.instance.$primevue` — the single place the
// directive touches `binding.instance` at all. Vue fills that in with
// `getComponentPublicInstance()`, which returns the component's *exposeProxy*
// whenever the component has called `expose()` — which the `<script setup>`
// compiler output always does. That proxy resolves Vue's own `$`-properties but
// not app-level `globalProperties`, so `$primevue` came back undefined for every
// tooltip hosted in a `<script setup>` component, i.e. all of them. The z-index
// was never captured and the ZIndex util fell back to ~1000, so tooltips painted
// behind the fixed menubar (1030) and the side-menu rail (2000) instead of at the
// configured `zIndex.tooltip` of 2100 — looking like "the tooltip didn't open".
//
// Hand the hook an instance that can resolve `$primevue` off the vnode's app
// context: the same fallback PrimeVue's own BaseDirective._getConfig already uses
// for the rest of its config, which is why everything except the z-index worked.
type PrimeVueHost = { $primevue?: unknown }

type PrimeVueAppConfig = { globalProperties?: PrimeVueHost }

type VNodeWithContext = VNode & {
  ctx?: { appContext?: { config?: PrimeVueAppConfig }}
}

const bindingWithPrimeVueConfig = (binding: DirectiveBinding, vnode: VNode): DirectiveBinding => {
  if ((binding.instance as PrimeVueHost | null)?.$primevue) {
    return binding
  }

  const $primevue = (vnode as VNodeWithContext).ctx?.appContext?.config?.globalProperties?.$primevue

  return $primevue ? { ...binding, instance: { $primevue } as never } : binding
}

const primeVueTooltip = Tooltip as ObjectDirective

const OnmsTooltip: ObjectDirective = {
  ...primeVueTooltip,
  beforeMount(el, binding, vnode, prevVnode) {
    primeVueTooltip.beforeMount?.call(this, el, bindingWithPrimeVueConfig(binding, vnode), vnode, prevVnode)
  }
}

export default OnmsTooltip
