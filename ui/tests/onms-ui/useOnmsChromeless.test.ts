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

import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { effectScope, nextTick, ref } from 'vue'
import { useOnmsChromeless, useOnmsChromelessHost } from '@opennms/onms-ui'

const pressEscape = (target: EventTarget = document) => {
  const event = new KeyboardEvent('keydown', { key: 'Escape', bubbles: true, cancelable: true })
  target.dispatchEvent(event)
  return event
}

const addToBody = (html: string) => {
  const holder = document.createElement('div')
  holder.innerHTML = html
  return document.body.appendChild(holder.firstElementChild!)
}

describe('useOnmsChromeless', () => {
  const { isChromeless, enterChromeless, exitChromeless } = useOnmsChromeless()
  const routeFlag = ref(false)
  const routeKey = ref('/a')
  let scope: ReturnType<typeof effectScope>

  // A route change: the host re-reads the flag when the key changes.
  const navigate = async (key: string, chromeless: boolean) => {
    routeFlag.value = chromeless
    routeKey.value = key
    await nextTick()
  }

  beforeEach(() => {
    scope = effectScope()
    scope.run(() => useOnmsChromelessHost(() => routeFlag.value, () => routeKey.value))
  })

  afterEach(async () => {
    scope.stop()
    exitChromeless()
    await navigate('/reset', false)
    document.body.innerHTML = ''
  })

  it('follows the route flag', async () => {
    await navigate('/wall', true)
    expect(isChromeless.value).toBe(true)
    await navigate('/nodes', false)
    expect(isChromeless.value).toBe(false)
  })

  it('keeps an enter until the next route change', async () => {
    enterChromeless()
    expect(isChromeless.value).toBe(true)
    await navigate('/nodes', false)
    expect(isChromeless.value).toBe(false)
  })

  it('keeps an enter until exit on the same route', () => {
    enterChromeless()
    exitChromeless()
    expect(isChromeless.value).toBe(false)
  })

  it('lets an exit on a chromeless route last until the next route change', async () => {
    await navigate('/wall', true)
    exitChromeless()
    expect(isChromeless.value).toBe(false)
    await navigate('/wall2', true)
    expect(isChromeless.value).toBe(true)
  })

  it.each([
    '<div class="p-dialog-mask p-overlay-mask"></div>',
    '<div class="p-drawer-mask p-overlay-mask"></div>',
    '<div class="p-popover p-component"></div>',
    '<div class="p-menu p-component p-menu-overlay"></div>',
    '<div class="p-tieredmenu p-component p-tieredmenu-overlay"></div>',
    '<div class="p-contextmenu p-component"></div>'
  ])('ignores Escape while an overlay closes on it: %s', (html) => {
    enterChromeless()
    const overlay = addToBody(html)
    pressEscape()
    expect(isChromeless.value).toBe(true)

    overlay.remove()
    pressEscape()
    expect(isChromeless.value).toBe(false)
  })

  it('does not count an inline menu as an overlay', () => {
    enterChromeless()
    addToBody('<div class="p-tieredmenu p-component"></div>')
    pressEscape()
    expect(isChromeless.value).toBe(false)
  })

  it('ignores Escape a component already handled', () => {
    enterChromeless()
    const field = addToBody('<div></div>')
    field.addEventListener('keydown', event => event.preventDefault())
    pressEscape(field)
    expect(isChromeless.value).toBe(true)
  })

  it.each(['<input>', '<textarea></textarea>', '<select></select>'])('ignores Escape typed into %s', (html) => {
    enterChromeless()
    pressEscape(addToBody(html))
    expect(isChromeless.value).toBe(true)
  })

  it('stops handling Escape once the host scope is gone', () => {
    scope.stop()
    enterChromeless()
    pressEscape()
    expect(isChromeless.value).toBe(true)
  })
})
