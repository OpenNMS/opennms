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
import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

// happy-dom cannot compute CSS custom properties, so the token lines are asserted as text.
const read = (name: string) => readFileSync(resolve(__dirname, '../../src/styles', name), 'utf8')

const block = (css: string, selector: string): string => {
  const start = css.indexOf(`${selector} {`)

  expect(start).toBeGreaterThanOrEqual(0)

  return css.slice(start, css.indexOf('\n}', start))
}

const theme = read('onms-theme.scss')
const severities = read('_severities.scss')
const light = block(theme, '.open-light')
const dark = block(theme, '.open-dark')

describe('severity theme tokens', () => {
  it.each([['light', light], ['dark', dark]])('%s theme makes Normal green and Critical follow --onms-critical', (_name, css) => {
    expect(css).toContain('--onms-normal: var(--onms-success);')
    expect(css).toContain('--onms-severity-normal-bg: var(--onms-normal);')
    expect(css).toContain('--onms-severity-critical-bg: var(--onms-critical);')
    expect(css).toContain('--onms-availability-normal-bg: var(--onms-severity-normal-bg);')
    expect(css).toContain('--onms-availability-normal-fg: var(--onms-severity-normal-fg);')
  })

  it('light Critical is --onms-error, dark Critical is the red error icon', () => {
    expect(light).toContain('--onms-critical: var(--onms-error);')
    expect(dark).toContain('--onms-critical: var(--onms-error-icon);')
  })

  it('Normal glyph is white on light and near-black on dark', () => {
    expect(light).toContain('--onms-severity-normal-fg: #ffffff;')
    expect(dark).toContain('--onms-severity-normal-fg: #0a0c1b;')
  })

  it('leaves --onms-error unchanged', () => {
    expect(light).toContain('--onms-error: #a5021f;')
    expect(dark).toContain('--onms-error: #ffa3b5;')
  })
})

describe('severity classes', () => {
  it('.critical and .normal use the severity tokens', () => {
    expect(block(severities, '.critical')).toContain('$critical')
    expect(block(severities, '.normal')).toContain('$normal')
    expect(block(severities, '.critical-color')).toContain('$critical')
    expect(block(severities, '.normal-color')).toContain('$normal')
  })

  it.each(['critical', 'major', 'minor', 'warning', 'normal', 'indeterminate', 'cleared'])('has .severity-tint--%s', (sev) => {
    expect(severities).toContain(`.severity-tint--${sev} {`)
    expect(block(severities, `.severity-tint--${sev}`)).toContain(`color-mix(in srgb, var(--onms-${sev}) 20%, transparent)`)
  })
})
