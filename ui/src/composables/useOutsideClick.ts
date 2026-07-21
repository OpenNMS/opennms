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

// Vendored from @featherds/composables (events/OutsideClick), v0.12.x, as part
// of the Phase 6 de-Feather work. Behaviour is preserved verbatim: returns an
// `active` ref; while active, invokes `listener` on click/focus outside the
// element(s) and on window blur. Depends only on Vue.
//
// The upstream source is Copyright (C) NantHealth and licensed under the
// Apache License, Version 2.0. This file was modified when vendored (re-typed
// for standalone use). Per Apache-2.0 §4, the original attribution and the full
// license text are retained in ui/THIRD-PARTY-LICENSE.md. The AGPLv3 header
// above applies to the combined OpenNMS work into which this code is
// incorporated; it does not supersede the upstream Apache-2.0 grant.
import { watch, onBeforeUnmount, ref, onMounted, Ref } from 'vue'

const useOutsideClick = (
  elementRef: Ref<HTMLElement> | Ref<HTMLElement[]>,
  listener: (e?: FocusEvent) => void,
  options: {
    click?: boolean
    focus?: boolean
    window?: boolean
  } = {
    click: true,
    focus: true,
    window: true
  }
) => {
  const active = ref(false)
  const windowBlurChecker = (e: FocusEvent) => {
    if (e.target === window) {
      listener(e)
    }
  }
  const outSideClick = (e: FocusEvent) => {
    const elementArr: HTMLElement[] = Array.isArray(elementRef.value)
      ? elementRef.value
      : [elementRef.value]
    const contained = elementArr.some(
      (el: HTMLElement) => el && el.contains(e.target as HTMLElement)
    )
    if (!contained) {
      listener(e)
    }
  }
  const removeEvents = () => {
    if (document && window) {
      if (options.click) {
        document.removeEventListener('click', outSideClick, true)
      }
      if (options.focus) {
        document.removeEventListener('focus', outSideClick, true)
      }
      if (options.window) {
        window.removeEventListener('blur', windowBlurChecker)
      }
    }
  }
  onMounted(() => {
    const unwatch = watch(
      active,
      (enabled) => {
        if (document && window && enabled) {
          if (options.click) {
            document.addEventListener('click', outSideClick, true)
          }
          if (options.focus) {
            document.addEventListener('focus', outSideClick, true)
          }
          if (options.window) {
            window.addEventListener('blur', windowBlurChecker)
          }
        } else {
          removeEvents()
        }
      },
      {
        immediate: true
      }
    )

    onBeforeUnmount(() => {
      unwatch()
      removeEvents()
    })
  })

  return active
}

export { useOutsideClick }
