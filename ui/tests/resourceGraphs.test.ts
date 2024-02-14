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

import { PrintStatement } from '@/types'
import { assert, test } from 'vitest'
import { tokenizeStatement, TOKENS, formatStatement } from '@/components/Resources/utils/LegendFormatter'

test('Tokenizing a statement', () => {
  let tokens = tokenizeStatement('Max  : %8.2lf %s\\n')
  assert.equal(tokens.length, 5)
  assert.equal(tokens[0].type, TOKENS.Text)
  assert.equal(tokens[0].value, 'Max  : ')
  assert.equal(tokens[1].type, TOKENS.Lf)
  assert.equal(tokens[1].length, 8)
  assert.equal(tokens[1].precision, 2)
  assert.equal(tokens[2].type, TOKENS.Text)
  assert.equal(tokens[2].value, ' ')
  assert.equal(tokens[3].type, TOKENS.Unit)
  assert.equal(tokens[4].type, TOKENS.Newline)

  tokens = tokenizeStatement('%10.5lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, 10)
  assert.equal(tokens[0].precision, 5)

  tokens = tokenizeStatement('%.3lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, null)
  assert.equal(tokens[0].precision, 3)

  tokens = tokenizeStatement('%lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, null)
  assert.equal(tokens[0].precision, null)

  tokens = tokenizeStatement('%7lf')
  assert.equal(tokens[0].type, TOKENS.Lf)
  assert.equal(tokens[0].length, 7)
  assert.equal(tokens[0].precision, null)
})

test('Format statement', () => {
  const renderer = {
    texts: <string[]>[],
    drawText: (text: string) => {
      renderer.texts.push(text)
    },
    drawNewline: () => {
      renderer.texts.push('\n')
    }
  }

  const statement: PrintStatement = {
    format: 'Avg: %8.2lf %s\\n',
    metric: 'test',
    value: 1024
  }

  formatStatement(statement, renderer)

  assert.equal(renderer.texts.length, 5)
  assert.equal(renderer.texts[0], ' Avg: ')
  assert.equal(renderer.texts[1].trim(), '1.02k')
  assert.equal(renderer.texts[4], '\n')
})
