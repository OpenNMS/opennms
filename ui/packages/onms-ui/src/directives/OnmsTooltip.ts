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
// PrimeVue's Tooltip captures the configured tooltip z-index onto the host
// element as `$_ptooltipZIndex`, and does it in `beforeMount` only, by reading
// `binding.instance.$primevue`. Two things go wrong there.
//
// 1. Vue fills `binding.instance` in with `getComponentPublicInstance()`, which
//    returns the component's *exposeProxy* whenever the component has called
//    `expose()` — which the `<script setup>` compiler output always does. That
//    proxy resolves Vue's own `$`-properties but not app-level
//    `globalProperties`, so `$primevue` came back undefined for every tooltip
//    hosted in a `<script setup>` component, i.e. all of them.
// 2. `beforeMount` returns early when the directive value is empty, before the
//    capture, and `updated` re-binds the events but never sets the property. So
//    a tooltip whose text arrives with data — `v-onms-tooltip="row.label"` in a
//    table cell, or a label computed from a store — mounted empty and stayed at
//    the fallback z-index even once it had something to say.
//
// Either way nothing was captured, the ZIndex util fell back to ~1000, and the
// tooltip painted behind the fixed menubar (1030) and the side-menu rail (2000)
// instead of at the configured `zIndex.tooltip` of 2100 — looking like "the
// tooltip didn't open".
//
// Both are fixed the same way: resolve the configured z-index off the vnode's
// app context — the fallback PrimeVue's own BaseDirective._getConfig already
// uses for the rest of its config, which is why everything except the z-index
// worked — and stamp it on the host after both `beforeMount` and `updated`.
// `updated` is enough for late values because the property is read at show
// time, in `tooltipActions` (`ZIndex.set('tooltip', tooltipElement,
// el.$_ptooltipZIndex)`), not at bind time. Nothing has to be remounted for a
// tooltip to arrive late.
type PrimeVueHost = { $primevue?: { config?: { zIndex?: { tooltip?: number }}}}

type PrimeVueAppConfig = { globalProperties?: PrimeVueHost }

type VNodeWithContext = VNode & {
  ctx?: { appContext?: { config?: PrimeVueAppConfig }}
}

const resolveZIndex = (binding: DirectiveBinding, vnode: VNode): number | undefined => {
  const fromInstance = (binding.instance as PrimeVueHost | null)?.$primevue
  const fromAppContext = (vnode as VNodeWithContext).ctx?.appContext?.config?.globalProperties?.$primevue

  return (fromInstance ?? fromAppContext)?.config?.zIndex?.tooltip
}

// PrimeVue hangs its tooltip state on the wrapped <input> for input wrappers and
// on the element itself otherwise; mirror `Tooltip.getTarget` so the z-index
// lands where `tooltipActions` will look for it.
const tooltipTarget = (el: HTMLElement): HTMLElement =>
  el.classList.contains('p-inputwrapper') ? el.querySelector('input') ?? el : el

const captureZIndex = (el: HTMLElement, binding: DirectiveBinding, vnode: VNode) => {
  const zIndex = resolveZIndex(binding, vnode)

  if (zIndex !== undefined) {
    (tooltipTarget(el) as HTMLElement & { $_ptooltipZIndex?: number }).$_ptooltipZIndex = zIndex
  }
}

const primeVueTooltip = Tooltip as ObjectDirective

const OnmsTooltip: ObjectDirective = {
  ...primeVueTooltip,
  beforeMount(el, binding, vnode, prevVnode) {
    primeVueTooltip.beforeMount?.call(this, el, binding, vnode, prevVnode)
    captureZIndex(el, binding, vnode)
  },
  updated(el, binding, vnode, prevVnode) {
    primeVueTooltip.updated?.call(this, el, binding, vnode, prevVnode)
    captureZIndex(el, binding, vnode)
  }
}

export default OnmsTooltip
