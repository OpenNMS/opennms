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

const pressEscape = () => document.dispatchEvent(new KeyboardEvent('keydown', { key: 'Escape' }))

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

  it('keeps an enter across route changes until exit', async () => {
    enterChromeless()
    await navigate('/nodes', false)
    expect(isChromeless.value).toBe(true)
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

  it('exits on Escape, but not while a dialog or drawer is closing on it', () => {
    enterChromeless()
    const mask = document.createElement('div')
    mask.className = 'p-drawer-mask p-overlay-mask'
    document.body.appendChild(mask)
    pressEscape()
    expect(isChromeless.value).toBe(true)

    mask.remove()
    pressEscape()
    expect(isChromeless.value).toBe(false)
  })

  it('stops handling Escape once the host scope is gone', () => {
    scope.stop()
    enterChromeless()
    pressEscape()
    expect(isChromeless.value).toBe(true)
  })
})
