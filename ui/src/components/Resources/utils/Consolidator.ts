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

import { Expression, GraphMetricsResponse } from '@/types'

/**
 * References:
 *   Reference: https://github.com/OpenNMS/backshift
 *   http://oss.oetiker.ch/rrdtool/doc/rrdgraph_data.en.html
 *   http://oss.oetiker.ch/rrdtool/doc/rrdgraph_rpn.en.html
 *
 * > Note that currently only aggregation functions work in VDEF rpn expressions.
 *
 * RRDtool currently supports a very limited subset of expressions - they must have the following form:
 * 'SERIES,[PARAMETER,]FUNCTION' whereas SERIES is a existing series and PARAMETER is a value.
 *
 * @author fooker
 */

interface Clazz {
  parse: (tokens: string | string[]) => Expression
  minimum?: (...p: any) => void
  maximum?: (...p: any) => void
  average?: (...p: any) => void
  stdev?: (...p: any) => void
  last?: (...p: any) => void
  first?: (...p: any) => void
  total?: (...p: any) => void
  percent?: (...p: any) => void
  percentnan?: (...p: any) => void
}

const Consolidator = () => {
  const functions: Record<string, any> = {}

  const clazz: Clazz = {
    parse: (tokens: string | string[]) => {
      // Split tokens if a single expression is passed
      if (typeof tokens === 'string') {
        tokens = tokens.split(',')
      }

      const metricName = tokens.shift()

      const functionName = (tokens.pop() as string).toLowerCase()

      if (!(functionName in functions)) {
        const msg = 'Unknown correlation function: ' + functionName
        console.log('Error: ' + msg)
        throw 'Error: ' + msg
      }

      if (tokens.length > 1) {
        const msg = 'Too many input values in RPN express. RPN: ' + tokens
        console.log('Error: ' + msg)
        throw 'Error: ' + msg
      }

      const argument = parseFloat(tokens[0]) // The remaining token is used as parameter

      return functions[functionName](metricName, argument)
    }
  }

  const valid = (value: number) => !isNaN(value) && isFinite(value)

  const forEach = (timestamps: number[], values: number[], cb: (...p: any) => void) => {
    let validCount = 0
    for (let i = 0; i < timestamps.length; i++) {
      if (!valid(values[i])) {
        continue
      }

      validCount++

      cb(timestamps[i], values[i])
    }

    return validCount
  }

  const parseResponse = (json: GraphMetricsResponse) => {
    let k, parts
    let constants: Record<string, any> = {}
    const numMetrics = json.labels.length

    const columns = new Array(1 + numMetrics)
    const columnNames = new Array(1 + numMetrics)
    const columnNameToIndex: Record<string, any> = {}

    columns[0] = json.timestamps
    columnNames[0] = 'timestamp'
    columnNameToIndex['timestamp'] = 0

    for (k = 0; k < numMetrics; k++) {
      columns[1 + k] = json.columns[k].values
      columnNames[1 + k] = json.labels[k]
      columnNameToIndex[columnNames[1 + k]] = 1 + k
    }

    if (json.constants) {
      constants = {}
      for (let c = 0, len = json.constants.length, key, value; c < len; c++) {
        key = json.constants[c].key
        value = json.constants[c].value

        // All of the constants are prefixed with the label of the associated source, but
        // the graph definitions don't support this prefix, so we just cut the prefix
        // off the constant name
        parts = key.split('.')
        if (parts.length > 1) {
          key = parts[1]
          constants[key] = value === undefined ? null : value
        }
      }
    }

    return {
      columns: columns,
      columnNames: columnNames,
      columnNameToIndex: columnNameToIndex,
      constants: constants
    }
  }

  const wrap = (func: (...p: any) => void) => {
    return (metricName: string, argument: string) => {
      return {
        metricName: metricName,
        functionName: func.name,
        argument: argument,

        consolidate: (measurementResponse: GraphMetricsResponse) => {
          const data = parseResponse(measurementResponse)

          return func(
            data.columns[data.columnNameToIndex['timestamp']],
            data.columns[data.columnNameToIndex[metricName]],
            argument
          )
        }
      }
    }
  }

  clazz['minimum'] = functions['minimum'] = wrap(function minimum(timestamps, values) {
    let minimumTimestamp = undefined
    let minimumValue = NaN

    forEach(timestamps, values, (timestamp, value) => {
      if (isNaN(minimumValue) || value < minimumValue) {
        minimumTimestamp = timestamp
        minimumValue = value
      }
    })

    return [minimumTimestamp, minimumValue]
  })

  clazz['maximum'] = functions['maximum'] = wrap(function maximum(timestamps, values) {
    let maximumTimestamp = undefined
    let maximumValue = NaN

    forEach(timestamps, values, (timestamp, value) => {
      if (isNaN(maximumValue) || value > maximumValue) {
        maximumTimestamp = timestamp
        maximumValue = value
      }
    })

    return [maximumTimestamp, maximumValue]
  })

  clazz['average'] = functions['average'] = wrap(function average(timestamps, values) {
    let sum = 0.0

    const cnt = forEach(timestamps, values, (timestamp, value) => {
      sum += value
    })

    return [undefined, sum / cnt]
  })

  clazz['stdev'] = functions['stdev'] = wrap(function stdev(timestamps, values) {
    let sum = 0.0

    const cnt = forEach(timestamps, values, (timestamp, value) => (sum += value))
    const avg = sum / cnt

    let dev = 0.0
    forEach(timestamps, values, (timestamp, value) => (dev += Math.pow(value - avg, 2.0)))

    return [undefined, Math.sqrt(dev / cnt)]
  })

  clazz['last'] = functions['last'] = wrap(function last(timestamps, values) {
    for (let i = timestamps.length - 1; i >= 0; i--) {
      if (valid(values[i])) {
        return [timestamps[i], values[i]]
      }
    }

    return [undefined, NaN]
  })

  clazz['first'] = functions['first'] = wrap(function first(timestamps, values) {
    for (let i = 0; i < timestamps.length; i++) {
      if (valid(values[i])) {
        return [timestamps[i], values[i]]
      }
    }

    return [undefined, NaN]
  })

  clazz['total'] = functions['total'] = wrap(function total(timestamps, values) {
    let sum = 0.0
    let cnt = 0

    // As we don't have a fixed step size, we can't include the first sample as RRDTool does
    for (let i = 1; i < timestamps.length; i++) {
      if (valid(values[i])) {
        sum += (values[i] * (timestamps[i] - timestamps[i - 1])) / 1000.0
        cnt += 1
      }
    }

    if (cnt > 0) {
      return [undefined, sum]
    } else {
      return [undefined, NaN]
    }
  })

  clazz['percent'] = functions['percent'] = wrap(function percent(timestamps, values, argument) {
    const sortedValues: any[] = []
    for (let i = 0; i < timestamps.length; i++) {
      sortedValues.push(values[i])
    }

    sortedValues.sort((a, b) => {
      if (isNaN(a)) return -1
      if (isNaN(b)) return 1

      if (a == Number.POSITIVE_INFINITY) return 1
      if (a == Number.NEGATIVE_INFINITY) return -1
      if (b == Number.POSITIVE_INFINITY) return -1
      if (b == Number.NEGATIVE_INFINITY) return 1

      if (a < b) return -1
      else return 1
    })

    return [undefined, sortedValues[Math.round((argument * (sortedValues.length - 1)) / 100.0)]]
  })

  clazz['percentnan'] = functions['percentnan'] = wrap(function percentnan(timestamps, values, argument) {
    const sortedValues: any[] = []
    forEach(timestamps, values, (timestamp, value) => sortedValues.push(value))

    sortedValues.sort()

    return [undefined, sortedValues[Math.round((argument * (sortedValues.length - 1)) / 100.0)]]
  })

  // For backward compatibility with deprecated (G)PRINT syntax:
  functions['min'] = functions['minimum']
  functions['max'] = functions['maximum']

  return clazz
}

export default Consolidator
