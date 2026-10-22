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

import { describe, expect, it } from 'vitest'
import { readdirSync, readFileSync } from 'fs'
import { join, relative } from 'path'

/*
 * Sass treats a declaration whose property name starts with `--` as a custom property and leaves
 * its value as plain CSS: SassScript is NOT evaluated there, only `#{}` interpolation is.
 *
 * So `--x: var(variables.$success)` compiles without complaint and emits the literal string
 * `var(variables.$success)` -- a property name that never resolves, leaving whatever it colours
 * unstyled. The correct form is `var(#{variables.$success})`.
 *
 * This is invisible to the compiler, to eslint and to stylelint, and it silently drops colour, so
 * it is worth a source-level check.
 */

const UI_ROOT = join(__dirname, '../..')

const styleFilesUnder = (dir: string): string[] =>
  readdirSync(dir, { recursive: true, withFileTypes: true })
    .filter(entry => entry.isFile() && /\.(vue|scss)$/.test(entry.name))
    .map(entry => join(entry.parentPath, entry.name))
    .filter(path => !path.includes('node_modules') && !path.includes('/dist'))

// A custom property declaration whose value mentions a Sass namespace or variable outside #{}.
const CUSTOM_PROP = /^\s*--[\w-]+\s*:\s*(?<value>[^;{}]*)/

const mentionsSassScript = (value: string): boolean => {
  // Strip every interpolation, which is the one thing Sass DOES evaluate here.
  const withoutInterpolation = value.replace(/#\{[^}]*\}/g, '')
  return /[\w-]+\.\$[\w-]+|(?<![#\w])\$[\w-]+/.test(withoutInterpolation)
}

describe('Sass custom property declarations', () => {
  it('never reference Sass variables outside an interpolation', () => {
    const files = [
      ...styleFilesUnder(join(UI_ROOT, 'src')),
      ...styleFilesUnder(join(UI_ROOT, 'packages'))
    ]

    expect(files.length).toBeGreaterThan(0)

    const offenders: string[] = []

    for (const file of files) {
      readFileSync(file, 'utf8').split('\n').forEach((line, i) => {
        const match = CUSTOM_PROP.exec(line)

        if (match?.groups?.value && mentionsSassScript(match.groups.value)) {
          offenders.push(`${relative(UI_ROOT, file)}:${i + 1}  ${line.trim()}`)
        }
      })
    }

    expect(offenders, [
      'A Sass variable inside a custom property value is emitted verbatim, not evaluated.',
      'Wrap it in an interpolation: --x: var(#{variables.$token});'
    ].join('\n')).toEqual([])
  })
})
