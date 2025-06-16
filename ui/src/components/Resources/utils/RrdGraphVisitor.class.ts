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
abstract class RrdGraphVisitor {
  abstract _onTitle(title: string): void
  abstract _onVerticalLabel(label: string): void
  abstract _onDEF(name: string, path: string, dsName: string, consolFun: string): void
  abstract _onCDEF(name: string, rpnExpression: string): void
  abstract _onVDEF(name: string, rpnExpression: string): void
  abstract _onLine(srcName: string, color: string | undefined, legend: string, width: number): void
  abstract _onArea(srcName: string, color: string | undefined, legend: string): void
  abstract _onStack(srcName: string, color: string | undefined, legend: string): void
  abstract _onGPrint(srcName: string, aggregation: string | undefined, value: string): void
  abstract _onComment(value: string): void

  _visit(graphDef: { command: string }) {
    // Inspired from http://krasimirtsonev.com/blog/article/Simple-command-line-parser-in-JavaScript
    const CommandLineParser = (function () {
      const parse = (str: string, lookForQuotes: boolean) => {
        const args = []
        let readingPart = false
        let part = ''
        const n = str.length
        for (let i = 0; i < n; i++) {
          if (str.charAt(i) === ' ' && !readingPart) {
            args.push(part)
            part = ''
          } else {
            if (str.charAt(i) === '"' && lookForQuotes) {
              readingPart = !readingPart
              part += str.charAt(i)
            } else {
              part += str.charAt(i)
            }
          }
        }
        args.push(part)
        return args
      }
      return {
        parse: parse
      }
    })()

    let i,
      args,
      command,
      name,
      path,
      dsName,
      consolFun,
      rpnExpression,
      subParts,
      width,
      srcName,
      color,
      legend,
      aggregation,
      value
    const parts = CommandLineParser.parse(graphDef.command, true)
    const n = parts.length
    for (i = 0; i < n; i++) {
      if (parts[i].indexOf('--') === 0) {
        args = /--(.*)=(.*)/.exec(parts[i])
        if (args === null) {
          continue
        }

        if (args[1] === 'title') {
          this._onTitle(this.displayString(this._decodeString(args[2])))
        } else if (args[1] === 'vertical-label') {
          this._onVerticalLabel(this.displayString(this._decodeString(args[2])))
        }
      }

      args = parts[i].match(/(\\.|[^:])+/g)
      if (args === null) {
        continue
      }
      command = args[0]

      if (command === 'DEF') {
        subParts = args[1].split('=')
        name = subParts[0]
        path = subParts[1]
        dsName = args[2]
        consolFun = args[3]
        this._onDEF(name, path, dsName, consolFun)
      } else if (command === 'CDEF') {
        subParts = args[1].split('=')
        name = subParts[0]
        rpnExpression = subParts[1]
        this._onCDEF(name, rpnExpression)
      } else if (command === 'VDEF') {
        subParts = args[1].split('=')
        name = subParts[0]
        rpnExpression = subParts[1]
        this._onVDEF(name, rpnExpression)
      } else if (command.match(/LINE/)) {
        width = parseInt((/LINE(\d+)/.exec(command) as any).toString())
        subParts = args[1].split('#')
        srcName = subParts[0]
        color = this._getColor(subParts[1])
        legend = this._decodeString(args[2])
        this._onLine(srcName, color, legend, width)
      } else if (command === 'AREA') {
        subParts = args[1].split('#')
        srcName = subParts[0]
        color = this._getColor(subParts[1])
        legend = this._decodeString(args[2])
        this._onArea(srcName, color, legend)
      } else if (command === 'STACK') {
        subParts = args[1].split('#')
        srcName = subParts[0]
        color = this._getColor(subParts[1])
        legend = this._decodeString(args[2])
        this._onStack(srcName, color, legend)
      } else if (command === 'GPRINT') {
        if (args.length === 3) {
          srcName = args[1]
          aggregation = undefined
          value = this._decodeString(args[2])
        } else {
          srcName = args[1]
          aggregation = args[2]
          value = this._decodeString(args[3])
        }
        this._onGPrint(srcName, aggregation, value)
      } else if (command === 'COMMENT') {
        value = this._decodeString(args[1])
        this._onComment(value)
      }
    }
  }

  _getColor(color: string) {
    if (color === undefined || color === '') {
      return undefined
    }
    return '#' + color
  }

  _decodeString(string: string) {
    if (string === undefined) {
      return string
    }

    // Remove any quotes
    string = string.replace(/"/g, '')
    // Replace escaped colons
    string = string.replace('\\:', ':')

    return string
  }

  displayString(string: string) {
    if (string === undefined) {
      return string
    }

    // Remove any newlines
    string = string.replace('\\n', '')
    // Remove any leading/trailing whitespace
    string = string.trim()
    return string
  }
}

export default RrdGraphVisitor
