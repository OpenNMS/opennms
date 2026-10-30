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

import { computed, onScopeDispose, readonly, ref, watch } from 'vue'

// Module-level, like useOnmsToast: the host and every plugin (through window.OnmsUI) share one state.
const requested = ref(false)
const dismissed = ref(false)
const routeChromeless = ref(false)

const isChromeless = computed<boolean>(() => !dismissed.value && (requested.value || routeChromeless.value))

const enterChromeless = () => {
  requested.value = true
  dismissed.value = false
}

const exitChromeless = () => {
  requested.value = false
  dismissed.value = true
}

// PrimeVue closes these on Escape without marking the event handled. Each is in the DOM only
// while open, and still is when this listener on document sees the event.
const OVERLAY_SELECTOR = [
  '.p-dialog-mask.p-overlay-mask',
  '.p-drawer-mask.p-overlay-mask',
  '.p-popover',
  '.p-menu-overlay',
  '.p-tieredmenu-overlay',
  '.p-contextmenu'
].join(', ')

const isEditable = (target: EventTarget | null) => {
  const el = target as HTMLElement | null
  return !!el && (el.tagName === 'INPUT' || el.tagName === 'TEXTAREA' || el.tagName === 'SELECT' || el.isContentEditable)
}

const onKeydown = (event: KeyboardEvent) => {
  if (event.key !== 'Escape' || !isChromeless.value || event.isComposing || event.defaultPrevented) {
    return
  }

  if (isEditable(event.target) || document.querySelector(OVERLAY_SELECTOR)) {
    return
  }

  exitChromeless()
}

/**
 * Whether the host app hides its chrome (menu bar, side menu, footer), e.g. for a wall display.
 * A page turns it on with `enterChromeless()`, typically when it mounts, and it stays on until the
 * next route change, `exitChromeless()` or Escape. The host also turns it on for routes it marks
 * chromeless.
 */
export const useOnmsChromeless = () => ({
  isChromeless: readonly(isChromeless),
  enterChromeless,
  exitChromeless
})

/**
 * Host only: call once from the app shell's setup. Each `routeKey` change resets the state and
 * re-reads `isRouteChromeless`; it runs before the new page renders, so that page's own
 * `enterChromeless()` still applies. Installs the Escape handler.
 */
export const useOnmsChromelessHost = (isRouteChromeless: () => boolean, routeKey: () => unknown) => {
  watch(routeKey, () => {
    routeChromeless.value = isRouteChromeless()
    requested.value = false
    dismissed.value = false
  }, { immediate: true })

  document.addEventListener('keydown', onKeydown)
  onScopeDispose(() => document.removeEventListener('keydown', onKeydown))

  return { isChromeless: readonly(isChromeless) }
}
