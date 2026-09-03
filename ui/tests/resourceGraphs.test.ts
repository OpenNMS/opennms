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
import { tokenizeStatement, TOKENS, formatStatement, getFormattedLegendStatements } from '@/components/Resources/utils/LegendFormatter'

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

const renderStatement = (statement: PrintStatement) => {
  const r = { texts: [] as string[], drawText: (t: string) => r.texts.push(t), drawNewline: () => r.texts.push('\n') }
  formatStatement(statement, r)
  return r.texts.join('')
}

test('Legend renders No Data / Invalid Data instead of NaN', () => {
  // a real value of 0 must render "0.00", not "NaN" (formatPrefix can't prefix 0)
  const ok = renderStatement({ format: 'Min: %8.2lf %s', metric: 'm', value: 0, dataState: 'ok' })
  assert.include(ok, '0.00')
  assert.notInclude(ok, 'NaN')
  assert.notInclude(ok, 'No Data')

  const okNonZero = renderStatement({ format: 'Avg: %8.2lf %s', metric: 'm', value: 1024, dataState: 'ok' })
  assert.include(okNonZero, '1.02k')

  const noData = renderStatement({ format: 'Min: %8.2lf %s', metric: 'm', value: NaN, dataState: 'nodata' })
  assert.include(noData, 'No Data')
  assert.notInclude(noData, 'NaN')

  const invalid = renderStatement({ format: 'Max: %8.2lf %s', metric: 'm', value: NaN, dataState: 'invalid' })
  assert.include(invalid, 'Invalid Data')
  assert.notInclude(invalid, 'NaN')
})

test('Legend classifies non-finite stats: no valid samples -> nodata, some samples -> invalid', () => {
  const stat = (value: number) => ({ metricName: 's', consolidate: () => [undefined, value] })

  // column has no finite samples -> "No Data"
  const noData: any = { labels: ['s'], timestamps: [1, 2], columns: [{ values: [NaN, NaN] }] }
  let converted: any = {
    metrics: [{ name: 's' }],
    printStatements: [{ format: 'Min: %8.2lf %s', metric: 'v1', value: NaN }],
    values: [{ name: 'v1', expression: stat(NaN) }]
  }
  getFormattedLegendStatements(noData, converted)
  assert.equal(converted.printStatements[0].dataState, 'nodata')

  // column has a finite sample but the aggregation is still non-finite -> "Invalid Data"
  const hasData: any = { labels: ['s'], timestamps: [1, 2], columns: [{ values: [0, 5] }] }
  converted = {
    metrics: [{ name: 's' }],
    printStatements: [{ format: 'Min: %8.2lf %s', metric: 'v1', value: NaN }],
    values: [{ name: 'v1', expression: stat(NaN) }]
  }
  getFormattedLegendStatements(hasData, converted)
  assert.equal(converted.printStatements[0].dataState, 'invalid')

  // finite aggregation -> "ok"
  converted = {
    metrics: [{ name: 's' }],
    printStatements: [{ format: 'Min: %8.2lf %s', metric: 'v1', value: 3 }],
    values: [{ name: 'v1', expression: stat(3) }]
  }
  getFormattedLegendStatements(hasData, converted)
  assert.equal(converted.printStatements[0].dataState, 'ok')
})
