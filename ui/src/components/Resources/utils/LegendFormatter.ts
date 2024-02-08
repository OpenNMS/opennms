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

import { ConvertedGraphData, GraphMetricsResponse } from '@/types'
import { fromUnixTime, format } from 'date-fns'
import { PrintStatement } from '@/types'
import { formatPrefix } from 'd3'

interface Renderer {
  texts: string[]
  drawText: (text: string) => void
  drawNewline: () => void
}

interface Token {
  type: string
  value?: string
  length?: null | number
  precision?: null | number
}

const formatTimestamps = (metrics: GraphMetricsResponse, format: string): GraphMetricsResponse => {
  const timestamps = metrics.timestamps
  metrics.formattedTimestamps = timestamps.map((timestamp) => formatTimestamp(timestamp, format))
  return metrics
}

const formatTimestamp = (timestamp: number, formatStr: string) => {
  const date = fromUnixTime(timestamp / 1000)

  switch (formatStr) {
    case 'minutes':
      return format(date, 'HH:mm:ss')
    case 'hours':
      return format(date, 'HH:mm')
    case 'days':
      return format(date, 'dd/MMM HH:mm')
    case 'months':
      return format(date, 'dd/MMM')
    case 'years':
      return format(date, 'MMM/y')
    default:
      return format(date, 'dd/MMM :HH:mm')
  }
}

const getFormattedLegendStatements = (
  graphMetrics: GraphMetricsResponse,
  convertedGraphData: ConvertedGraphData
): GraphMetricsResponse => {
  const metrics = convertedGraphData.metrics
  const printStatements = convertedGraphData.printStatements
  const values = convertedGraphData.values

  // add the value to the print statement
  for (const statement of printStatements) {
    for (const val of values) {
      if (statement.metric === val.name) {
        statement.value = val.expression.consolidate(graphMetrics)[1]

        const renderer: Renderer = {
          texts: <string[]>[],
          drawText: (text: string) => {
            renderer.texts.push(text)
          },
          drawNewline: () => {
            renderer.texts.push('\n')
          }
        }

        formatStatement(statement, renderer)
        statement.text = renderer.texts.join('')
      }
    }

    if (statement.format.includes('%g ')) {
      statement.header = statement.format.replace('%g ', '')
    }
  }

  // separate print statements by metric
  const legendArrays = []
  for (const metric of metrics) {
    const statementsForMetric = []
    for (const statement of printStatements) {
      if (statement.metric.split('_')[0] === (metric.name as string)) {
        statementsForMetric.push(statement)
      }
    }
    legendArrays.push({ metricName: metric.name as string, statements: statementsForMetric })
  }

  // combine each legend array into a legend statement
  const legendStatements: { name: string; statement: string }[] = []
  for (const legendArray of legendArrays) {
    let statement: any[] = []
    for (const obj of legendArray.statements) {
      statement = [...statement, obj.header, obj.text]
    }

    legendStatements.push({ name: legendArray.metricName, statement: statement.join(' ') })
  }

  graphMetrics.formattedLabels = legendStatements
  return graphMetrics
}

/* eslint-disable no-prototype-builtins */
/* 
  Updated from https://github.com/OpenNMS/flot-legend/blob/master/src/main.js
  Logic to apply specified gprint format to legend values 
*/
const TOKENS = Object.freeze({
  Text: 'text',
  Unit: 'unit',
  Lf: 'lf',
  Newline: 'newline'
})

const tokenizeStatement = (value: string) => {
  const tokens: Token[] = []
  const types: { [x: string]: number } = {}
  const lfRegex = /^%(\d*)(\.(\d+))?lf/
  let stack: string[] = []

  const accountForTokenType = (type: string) => {
    if (types.hasOwnProperty(type)) {
      types[type] += 1
    } else {
      types[type] = 1
    }
  }

  const numTokensWithType = (type: string) => (types.hasOwnProperty(type) ? types[type] : 0)

  const pushToken = (token?: Token) => {
    if (stack.length > 0) {
      tokens.push({
        type: TOKENS.Text,
        value: stack.join('')
      })
      stack = []
      accountForTokenType(TOKENS.Text)
    }

    if (token !== undefined) {
      tokens.push(token)
      accountForTokenType(token.type)
    }
  }

  for (let i = 0, len = value.length; i < len; i++) {
    const c = value[i]
    // Grab the next character, bounded by the size of the string
    const nextc = value[Math.min(i + 1, len - 1)]
    let match

    if (c === '%' && nextc === 's') {
      pushToken({
        type: TOKENS.Unit
      })

      i++
    } else if (c === '%' && nextc === '%') {
      stack.push('%')

      i++
    } else if (c == '\\' && nextc == 'n') {
      pushToken({
        type: TOKENS.Newline
      })

      i++
    } else if (c == '\\' && nextc == 'l') {
      pushToken({
        type: TOKENS.Newline
      })

      i++
    } else if (c == '\\' && nextc == 's') {
      pushToken({
        type: TOKENS.Newline
      })

      i++
    } else if ((match = lfRegex.exec(value.slice(i))) !== null) {
      let length = NaN
      try {
        length = parseInt(match[1])
      } catch (err) {
        // pass
      }
      let precision = NaN
      try {
        precision = parseInt(match[3])
      } catch (err) {
        // pass
      }

      pushToken({
        type: TOKENS.Lf,
        length: isNaN(length) ? null : length,
        precision: isNaN(precision) ? null : precision
      })

      i += match[0].length - 1
    } else {
      stack.push(c)
    }
  }

  // Add a space after the %lf statement if there is no unit
  if (
    numTokensWithType(TOKENS.Lf) > 0 &&
    numTokensWithType(TOKENS.Unit) === 0 &&
    tokens[tokens.length - 1].type === TOKENS.Lf
  ) {
    stack.push(' ')
  }

  // Convert any remaining characters on the stack to a text token
  pushToken()

  return tokens
}

const formatStatement = (statement: PrintStatement, renderer: Renderer) => {
  // Parse the statement into a series of tokens
  const tokens = tokenizeStatement(statement.format)
  // Used to store the unit symbol from the last LF statement, we need this in the following UNIT statement

  for (const token of tokens) {
    if (token.type === TOKENS.Text) {
      if (token.value) renderer.drawText(' ' + token.value)
    } else if (token.type === TOKENS.Newline) {
      renderer.drawNewline()
    } else if (token.type === TOKENS.Unit) {
      renderer.drawText(' ')
    } else if (token.type === TOKENS.Lf) {
      const value = statement.value
      let scaledValue: string | number = value
      let format = ''

      if (token.length !== null) {
        format += token.length
      }
      if (token.precision !== null) {
        format += '.' + token.precision
      }
      format += 'f'

      if (!isNaN(value)) {
        const f = formatPrefix(format, value)
        scaledValue = f(value)
      }

      renderer.drawText(scaledValue as string)
    } else {
      throw 'Unsupported token: ' + JSON.stringify(token)
    }
  }
}

export { TOKENS, tokenizeStatement, formatTimestamps, getFormattedLegendStatements, formatStatement }
