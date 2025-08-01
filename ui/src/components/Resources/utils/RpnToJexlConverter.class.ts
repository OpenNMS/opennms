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

/**
 * References:
 *   https://github.com/OpenNMS/backshift
 *   http://oss.oetiker.ch/rrdtool/doc/rrdgraph_rpn.en.html
 *   http://commons.apache.org/proper/commons-jexl/reference/syntax.html
 *
 * @author jesse
 */

class RpnToJexlConverter {
  operators: Record<string, any> = {}

  constructor() {
    const simpleOp = (op: string) => {
      return (stack: string[]) => {
        const b = stack.pop()
        const a = stack.pop()
        return '(' + a + ' ' + op + ' ' + b + ')'
      }
    }

    const funcOp = (op: string, numArgs: number) => {
      return (stack: string[]) => {
        let ret = op + '('
        for (let i = 0; i < numArgs; i++) {
          ret += stack.pop() + ','
        }
        return ret.substring(0, ret.length - 1) + ')'
      }
    }

    const ifOp = (stack: string[]) => {
      const c = stack.pop()
      const b = stack.pop()
      const a = stack.pop()
      return '(' + a + ' != 0 ? ' + b + ' : ' + c + ')'
    }

    const unOp = (stack: string[]) => {
      const a = stack.pop()
      return '( (' + a + ' == NaN) ? 1 : 0)'
    }

    const infOp = (stack: string[]) => {
      const a = stack.pop()
      return '( (' + a + ' == __inf) || (' + a + ' == __neg_inf) ? 1 : 0)'
    }

    const booleanOp = (op: string) => {
      return (stack: string[]) => {
        const b = stack.pop()
        const a = stack.pop()
        return '(' + a + ' ' + op + ' ' + b + ' ? 1 : 0)'
      }
    }

    const limitOp = (stack: string[]) => {
      const max = stack.pop()
      const min = stack.pop()
      const val = stack.pop()
      return (
        '( ' +
        '( ' +
        '(' +
        min +
        ' == __inf) || (' +
        min +
        ' == __neg_inf) ' +
        '|| (' +
        max +
        ' == __inf) || (' +
        max +
        ' == __neg_inf) ' +
        '|| (' +
        val +
        ' == __inf) || (' +
        val +
        ' == __neg_inf) ' +
        '|| (' +
        val +
        ' < ' +
        min +
        ') ' +
        '|| (' +
        val +
        ' > ' +
        max +
        ') ' +
        ') ? NaN : ' +
        val +
        ' ' +
        ')'
      )
    }

    const minMaxNanOp = (op: string) => {
      return (stack: string[]) => {
        const b = stack.pop()
        const a = stack.pop()
        return (
          '( ' +
          '( ' +
          a +
          ' == NaN ) ? ' +
          b +
          ' : ( ' +
          '( ' +
          b +
          ' == NaN ) ? ' +
          a +
          ' : ( ' +
          op +
          '(' +
          b +
          ',' +
          a +
          ') ' +
          ') ' +
          ') ' +
          ')'
        )
      }
    }

    const addNanOp = (stack: string[]) => {
      const b = stack.pop()
      const a = stack.pop()
      return (
        '( ' +
        '( ' +
        '( ' +
        a +
        ' == NaN ) && ' +
        '( ' +
        b +
        ' == NaN ) ' +
        ') ? NaN : ( ' +
        '( ' +
        a +
        ' == NaN ) ? ' +
        b +
        ' : ( ' +
        '( ' +
        b +
        ' == NaN ) ? ' +
        a +
        ' : ( ' +
        a +
        ' + ' +
        b +
        ' ) ' +
        ') ' +
        ') ' +
        ')'
      )
    }

    const atan2Op = (stack: string[]) => {
      const x = stack.pop()
      const y = stack.pop()
      return 'math:atan2(' + y + ',' + x + ')'
    }

    this.operators['+'] = simpleOp('+')
    this.operators['-'] = simpleOp('-')
    this.operators['*'] = simpleOp('*')
    this.operators['/'] = simpleOp('/')
    this.operators['%'] = simpleOp('%')
    this.operators['IF'] = ifOp
    this.operators['UN'] = unOp
    this.operators['LT'] = booleanOp('<')
    this.operators['LE'] = booleanOp('<=')
    this.operators['GT'] = booleanOp('>')
    this.operators['GE'] = booleanOp('>=')
    this.operators['EQ'] = booleanOp('==')
    this.operators['NE'] = booleanOp('!=')
    this.operators['MIN'] = funcOp('math:min', 2)
    this.operators['MAX'] = funcOp('math:max', 2)
    this.operators['MINNAN'] = minMaxNanOp('math:min')
    this.operators['MAXNAN'] = minMaxNanOp('math:max')
    this.operators['ISINF'] = infOp
    this.operators['LIMIT'] = limitOp
    this.operators['ADDNAN'] = addNanOp
    this.operators['SIN'] = funcOp('math:sin', 1)
    this.operators['COS'] = funcOp('math:cos', 1)
    this.operators['LOG'] = funcOp('math:log', 1)
    this.operators['EXP'] = funcOp('math:exp', 1)
    this.operators['SQRT'] = funcOp('math:sqrt', 1)
    this.operators['ATAN'] = funcOp('math:atan', 1)
    this.operators['ATAN2'] = atan2Op
    this.operators['FLOOR'] = funcOp('math:floor', 1)
    this.operators['CEIL'] = funcOp('math:ceil', 1)
    this.operators['RAD2DEG'] = funcOp('math:toDegrees', 1)
    this.operators['DEG2RAD'] = funcOp('math:toRadians', 1)
    this.operators['ABS'] = funcOp('math:abs', 1)
    this.operators['UNKN'] = () => 'NaN'
    this.operators['INF'] = () => '__inf'
    this.operators['NEGINF'] = () => '__neg_inf'
    this.operators['{diffTime}'] = () => '(__diff_time / 1000)'
  }

  convert(rpn: string): string {
    const stack: string[] = []
    const tokens = rpn.split(',')
    const n = tokens.length
    for (let i = 0; i < n; i++) {
      const token = tokens[i]
      if (this._isOperator(token)) {
        stack.push(this._toExpression(token, stack))
      } else {
        stack.push(token)
      }
    }

    if (stack.length === 1) {
      return stack.pop() as string
    } else {
      const msg = 'Too many input values in RPN express. RPN: ' + rpn + ' Stack: ' + JSON.stringify(stack)
      console.log('Error: ' + msg)
      throw 'Error: ' + msg
    }
  }

  _isOperator(token: string) {
    return token in this.operators
  }

  _toExpression(token: string, stack: string[]) {
    return this.operators[token](stack)
  }
}

export default RpnToJexlConverter
