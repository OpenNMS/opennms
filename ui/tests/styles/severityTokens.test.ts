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

import { resolve } from 'node:path'
import { pathToFileURL } from 'node:url'
import * as sass from 'sass'
import { describe, expect, it } from 'vitest'

// happy-dom cannot compute CSS custom properties, so the stylesheets are compiled with sass, as
// the build does, and the custom properties are resolved here through their var() chains.
const SRC = resolve(__dirname, '../../src')

const compile = (file: string): string => sass.compile(resolve(SRC, file), {
  importers: [{
    findFileUrl: (url: string) => url.startsWith('@/') ? new URL(pathToFileURL(resolve(SRC, url.slice(2)))) : null
  }],
  logger: sass.Logger.silent
}).css

// The declarations of the first rule with exactly this selector (after a rule or a comment).
const rule = (css: string, selector: string): string => {
  const match = new RegExp(`(^|\\}|\\*/)\\s*${selector.replace('.', '\\.')}\\s*\\{([^}]*)\\}`).exec(css)

  expect(match, `${selector} rule`).not.toBeNull()

  return match![2]!
}

const customProperties = (declarations: string): Map<string, string> =>
  new Map([...declarations.matchAll(/(--[\w-]+)\s*:\s*([^;]+);/g)].map(m => [m[1]!, m[2]!.trim()]))

// A theme's value for a custom property, following var() through the theme block and then :root.
// Every --onms-* token must be defined here; another one (PrimeVue's --p-*) is set at runtime, so
// it resolves to itself.
const resolver = (css: string, themeSelector: string) => {
  const props = new Map([...customProperties(rule(css, ':root')), ...customProperties(rule(css, themeSelector))])

  const value = (name: string, seen: string[] = []): string => {
    expect(seen, `var() cycle at ${name}`).not.toContain(name)
    const raw = props.get(name)

    if (raw === undefined && !name.startsWith('--onms-')) {
      return `var(${name})`
    }

    expect(raw, `${name} in ${themeSelector}`).toBeDefined()

    const ref = /^var\((--[\w-]+)\)$/.exec(raw!)

    return ref ? value(ref[1]!, [...seen, name]) : raw!
  }

  return value
}

// The SPA's stylesheet entry (src/main/main.ts); the menu bundle loads onms-theme.scss on its own.
const spa = compile('styles/themes.scss')
const menu = compile('styles/onms-theme.scss')
const themes = [['light', '.open-light'], ['dark', '.open-dark']] as const

describe('severity theme tokens', () => {
  it.each(themes)('%s theme draws Normal in the success green, icon and availability badge alike', (_name, selector) => {
    const value = resolver(spa, selector)

    expect(value('--onms-severity-normal-bg')).toBe(value('--onms-success'))
    expect(value('--onms-availability-normal-bg')).toBe(value('--onms-success'))
    expect(value('--onms-availability-normal-fg')).toBe(value('--onms-severity-normal-fg'))
  })

  it('draws Critical in the error red in light, and the red error icon colour in dark', () => {
    const light = resolver(spa, '.open-light')
    const dark = resolver(spa, '.open-dark')

    expect(light('--onms-severity-critical-bg')).toBe(light('--onms-error'))
    expect(dark('--onms-severity-critical-bg')).toBe(dark('--onms-error-icon'))
    expect(dark('--onms-severity-critical-bg')).not.toBe(dark('--onms-error'))
  })

  it.each(themes)('%s theme draws Indeterminate in its own colour and Cleared in grey', (_name, selector) => {
    const value = resolver(spa, selector)

    expect(value('--onms-severity-indeterminate-bg')).toBe(value('--onms-indeterminate'))
    expect(value('--onms-severity-none-bg')).toBe(value('--onms-cleared'))
    expect(value('--onms-severity-indeterminate-bg')).not.toBe(value('--onms-severity-none-bg'))
  })

  it.each(['critical', 'major', 'minor', 'warning', 'normal', 'indeterminate', 'none'])('defines the %s icon colours in both themes, for the SPA and the menu bundle', (key) => {
    for (const css of [spa, menu]) {
      for (const [, selector] of themes) {
        const value = resolver(css, selector)

        expect(value(`--onms-severity-${key}-bg`)).not.toBe('')
        expect(value(`--onms-severity-${key}-fg`)).not.toBe('')
      }
    }
  })
})

describe('severity classes', () => {
  it('colours .critical and .normal with the severity tokens', () => {
    const severities = compile('styles/_severities.scss')

    expect(rule(severities, '.critical')).toContain('var(--onms-critical)')
    expect(rule(severities, '.normal')).toContain('var(--onms-normal)')
  })

  it.each(['critical', 'major', 'minor', 'warning', 'normal', 'indeterminate', 'cleared'])('ships .severity-tint--%s in the SPA stylesheet', (severity) => {
    expect(rule(spa, `.severity-tint--${severity}`)).toContain(`var(--onms-${severity})`)
  })
})
