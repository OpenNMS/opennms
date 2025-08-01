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

// Reference: https://github.com/OpenNMS/backshift

import { ConvertedGraphValue, Metric, PrintStatement, Series } from '@/types'
import Consolidator from './Consolidator'
import RpnToJexlConverter from './RpnToJexlConverter.class'
import RrdGraphVisitor from './RrdGraphVisitor.class'

class RrdGraphConverter extends RrdGraphVisitor {
  graphDef
  resourceId
  convertRpnToJexl
  model
  consolidator
  prefix
  expressionRegexp

  constructor(args: any) {
    super()

    this.graphDef = args.graphDef
    this.resourceId = args.resourceId
    this.convertRpnToJexl = args.convertRpnToJexl === undefined ? true : args.convertRpnToJexl
    this.prefix = ''
    this.expressionRegexp = new RegExp('\\{([^}]*)}', 'g')

    this.model = {
      title: '',
      verticalLabel: '',
      metrics: [] as Metric[],
      values: [] as ConvertedGraphValue[],
      series: [] as Series[],
      printStatements: [] as PrintStatement[],
      properties: {} as Record<string, any>
    }

    this.consolidator = Consolidator()

    // Replace strings.properties tokens
    let propertyValue, i, j, n, m
    for (i = 0, n = this.graphDef.propertiesValues.length; i < n; i++) {
      propertyValue = this.graphDef.propertiesValues[i]
      this.model.properties[propertyValue] = undefined
    }

    this._visit(this.graphDef)

    for (i = 0, n = this.model.values.length; i < n; i++) {
      const metric: Metric | string = this.model.values[i].expression.metricName
      if (metric === undefined) {
        continue
      }

      let foundSeries = false
      for (j = 0, m = this.model.series.length; j < m; j++) {
        if (metric === this.model.series[j].metric) {
          foundSeries = true
          break
        }
      }

      if (!foundSeries) {
        const series = {
          metric: metric,
          type: 'hidden'
        } as Series
        this.model.series.push(series)
      }
    }

    // Determine the set of metric names that are used in the series / legends
    const nonTransientMetrics: Record<string, any> = {}
    for (i = 0, n = this.model.series.length; i < n; i++) {
      nonTransientMetrics[this.model.series[i].metric as string] = 1
    }

    // Mark all other sources as transient - if we don't use their values, then don't return them
    for (i = 0, n = this.model.metrics.length; i < n; i++) {
      const metric = this.model.metrics[i]
      metric.transient = !((metric.name as string) in nonTransientMetrics)
    }
  }

  _onTitle(title: string) {
    this.model.title = title
  }

  _onVerticalLabel(label: string) {
    this.model.verticalLabel = label
  }

  _onDEF(name: string, path: string, dsName: string, consolFun: string) {
    const columnIndex = parseInt((/\{rrd(\d+)}/.exec(path) as any[])[1]) - 1
    const attribute = this.graphDef.columns[columnIndex]

    this.prefix = name
    this.model.metrics.push({
      name: name,
      attribute: attribute,
      resourceId: this.resourceId,
      datasource: dsName,
      aggregation: consolFun
    })
  }

  _onCDEF(name: string, rpnExpression: string) {
    let expression = rpnExpression
    if (this.convertRpnToJexl) {
      const rpnToJexlConverter = new RpnToJexlConverter()
      expression = rpnToJexlConverter.convert(rpnExpression)
    }
    if (this.prefix) {
      expression = expression.replace(this.expressionRegexp, this.prefix + '.$1')
    }
    this.model.metrics.push({
      name: name,
      expression: expression
    } as Metric)
  }

  _onVDEF(name: string, rpnExpression: string) {
    this.model.values.push({
      name: name,
      expression: this.consolidator.parse(rpnExpression)
    })
  }

  _onLine(srcName: string, color: string, legend: string) {
    const series = {
      name: this.displayString(legend),
      metric: srcName,
      type: 'line',
      color: color
    } as Series
    this.maybeAddPrintStatementForSeries(srcName, legend)
    this.model.series.push(series)
  }

  _onArea(srcName: string, color: string, legend: string) {
    const series = {
      name: this.displayString(legend),
      metric: srcName,
      type: 'area',
      color: color
    } as Series
    this.maybeAddPrintStatementForSeries(srcName, legend)
    this.model.series.push(series)
  }

  _onStack(srcName: string, color: string, legend: string) {
    const series = {
      name: this.displayString(legend),
      metric: srcName,
      type: 'stack',
      color: color,
      legend: legend
    } as Series
    this.maybeAddPrintStatementForSeries(srcName, legend)
    this.model.series.push(series)
  }

  _onGPrint(srcName: string, aggregation: string, format: string) {
    if (aggregation === undefined) {
      // Modern form
      this.model.printStatements.push({
        metric: srcName,
        format: format
      } as PrintStatement)
    } else {
      // Deprecated form - create a intermediate VDEF
      const metricName = srcName + '_' + aggregation + '_' + Math.random().toString(36).substring(2)

      this.model.values.push({
        name: metricName,
        expression: this.consolidator.parse([srcName, aggregation])
      })

      this.model.printStatements.push({
        metric: metricName,
        format: format
      } as PrintStatement)
    }
  }

  _onComment(format: string) {
    this.model.printStatements.push({
      format: format
    } as PrintStatement)
  }

  maybeAddPrintStatementForSeries(series: string, legend: string) {
    if (legend === undefined || legend === null || legend === '') {
      return
    }

    this.model.printStatements.push({
      metric: series,
      value: NaN,
      format: '%g ' + legend
    })
  }
}

export default RrdGraphConverter
