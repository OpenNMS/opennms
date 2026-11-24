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

import { afterEach, describe, expect, it } from 'vitest'
import { readCssColor } from '@/lib/themeColors'

describe('readCssColor', () => {
  afterEach(() => {
    document.documentElement.style.removeProperty('--onms-major')
  })

  it('returns the trimmed value of a custom property', () => {
    document.documentElement.style.setProperty('--onms-major', ' #e35302 ')
    expect(readCssColor('--onms-major')).toBe('#e35302')
  })

  it('returns an empty string when the property is unset', () => {
    expect(readCssColor('--onms-does-not-exist')).toBe('')
  })

  it('reads from a passed element', () => {
    const el = document.createElement('div')
    el.style.setProperty('--onms-major', '#123456')
    document.body.appendChild(el)
    expect(readCssColor('--onms-major', el)).toBe('#123456')
    el.remove()
  })
})
