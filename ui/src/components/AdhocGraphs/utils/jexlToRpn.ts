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
 * Convert the infix JEXL an ad-hoc expression is written in into the RPN that an
 * RRDtool CDEF requires.
 *
 * This is the mirror of `Resources/utils/RpnToJexlConverter.class.ts`, which the
 * SPA uses to read prefab graphs. That direction is total — every RRD operator
 * has a JEXL rendering — but this one is not: JEXL is a general expression
 * language and RRD RPN is a small stack machine, so most JEXL programs have no
 * translation at all.
 *
 * The supported subset is therefore deliberately narrow — numbers, series names,
 * `+ - * / %`, unary minus and parentheses — and anything outside it is REFUSED
 * with a reason rather than approximated. A CDEF that silently computes something
 * other than what the user saw on screen is far worse than no CDEF.
 */

export interface JexlToRpnSuccess {
  ok: true
  rpn: string
  /** Series names the expression reads, in order of first appearance. */
  identifiers: string[]
}

export interface JexlToRpnFailure {
  ok: false
  reason: string
}

export type JexlToRpnResult = JexlToRpnSuccess | JexlToRpnFailure

interface Token {
  type: 'number' | 'identifier' | 'operator' | 'lparen' | 'rparen'
  value: string
}

/** Infix operator → RRD RPN operator, with precedence and associativity. */
const OPERATORS: Record<string, { rpn: string, precedence: number }> = {
  '+': { rpn: '+', precedence: 1 },
  '-': { rpn: '-', precedence: 1 },
  '*': { rpn: '*', precedence: 2 },
  '/': { rpn: '/', precedence: 2 },
  '%': { rpn: '%', precedence: 2 }
}

/**
 * Unary minus, kept distinct from binary '-' so precedence works out. RRD RPN has
 * no negation operator, so `-x` is emitted as `0,x,-`: a literal 0 goes to the
 * output the moment the unary is seen, and this marker behaves as a high-precedence
 * binary subtraction from that 0. Doing it here rather than as a post-pass matters
 * — `-(a + b)` must become `0,a,b,+,-`, which a fix-up over the flat token list
 * cannot produce.
 */
const UNARY_MINUS = 'u-'

const NUMBER = /^\d+(\.\d+)?([eE][+-]?\d+)?/
const IDENTIFIER = /^[A-Za-z_][A-Za-z0-9_]*/

const failure = (reason: string): JexlToRpnFailure => ({ ok: false, reason })

const tokenize = (expression: string): Token[] | JexlToRpnFailure => {
  const tokens: Token[] = []
  let rest = expression.trim()

  while (rest.length) {
    const char = rest[0]

    if (/\s/.test(char)) {
      rest = rest.slice(1)
      continue
    }

    if (char === '(') {
      tokens.push({ type: 'lparen', value: char })
      rest = rest.slice(1)
      continue
    }

    if (char === ')') {
      tokens.push({ type: 'rparen', value: char })
      rest = rest.slice(1)
      continue
    }

    if (OPERATORS[char]) {
      tokens.push({ type: 'operator', value: char })
      rest = rest.slice(1)
      continue
    }

    const number = NUMBER.exec(rest)
    if (number) {
      tokens.push({ type: 'number', value: number[0] })
      rest = rest.slice(number[0].length)
      continue
    }

    const identifier = IDENTIFIER.exec(rest)
    if (identifier) {
      const after = rest.slice(identifier[0].length)

      // An identifier followed by '(' is a function call, and one followed by
      // ':name' is a JEXL namespace prefix (math:abs(...)). Neither has a general
      // RRD equivalent, so both are refused by name — and the namespace case has
      // to be caught here, or the ':' falls through to the ternary check below and
      // reports the wrong reason.
      if (/^\s*\(/.test(after)) {
        return failure(`Function calls are not supported in a graph definition: ${identifier[0]}(...)`)
      }

      const namespaced = /^\s*:\s*([A-Za-z_][A-Za-z0-9_]*)/.exec(after)
      if (namespaced) {
        return failure(
          `Function calls are not supported in a graph definition: ${identifier[0]}:${namespaced[1]}(...)`
        )
      }

      tokens.push({ type: 'identifier', value: identifier[0] })
      rest = after
      continue
    }

    if (char === '?' || char === ':') {
      return failure('Conditional (? :) expressions are not supported in a graph definition.')
    }

    if (char === '"' || char === '\'') {
      return failure('Text values are not supported in a graph definition.')
    }

    if ('<>=!&|'.includes(char)) {
      return failure(`Comparison and logical operators are not supported in a graph definition: ${char}`)
    }

    return failure(`Unsupported character in expression: ${char}`)
  }

  return tokens
}

/**
 * Shunting-yard. Emits RRD RPN, which is comma-separated: `a,8,*`.
 */
export const jexlToRpn = (expression: string): JexlToRpnResult => {
  const tokenized = tokenize(expression)

  if (!Array.isArray(tokenized)) {
    return tokenized
  }

  if (!tokenized.length) {
    return failure('The expression is empty.')
  }

  const output: string[] = []
  const stack: string[] = []
  const identifiers: string[] = []
  // Distinguishes unary from binary minus, and catches two values in a row.
  let expectValue = true

  const precedenceOf = (operator: string) =>
    (operator === UNARY_MINUS ? 3 : OPERATORS[operator].precedence)

  const rpnFor = (operator: string) =>
    (operator === UNARY_MINUS ? '-' : OPERATORS[operator].rpn)

  for (const token of tokenized) {
    if (token.type === 'number' || token.type === 'identifier') {
      if (!expectValue) {
        return failure(`Missing an operator before '${token.value}'.`)
      }

      output.push(token.value)

      if (token.type === 'identifier' && !identifiers.includes(token.value)) {
        identifiers.push(token.value)
      }

      expectValue = false
      continue
    }

    if (token.type === 'operator') {
      if (expectValue) {
        if (token.value !== '-') {
          return failure(`Missing a value before '${token.value}'.`)
        }
        // 0 goes out now; the marker subtracts the operand from it later.
        output.push('0')
        stack.push(UNARY_MINUS)
        continue
      }

      while (stack.length) {
        const top = stack[stack.length - 1]

        if (top === '(' || precedenceOf(top) < precedenceOf(token.value)) {
          break
        }

        output.push(rpnFor(top))
        stack.pop()
      }

      stack.push(token.value)
      expectValue = true
      continue
    }

    if (token.type === 'lparen') {
      if (!expectValue) {
        return failure('Missing an operator before \'(\'.')
      }
      stack.push('(')
      continue
    }

    // rparen
    if (expectValue) {
      return failure('Missing a value before \')\'.')
    }

    let matched = false

    while (stack.length) {
      const top = stack.pop() as string

      if (top === '(') {
        matched = true
        break
      }

      output.push(rpnFor(top))
    }

    if (!matched) {
      return failure('Unbalanced parentheses in the expression.')
    }

    expectValue = false
  }

  if (expectValue) {
    return failure('The expression ends with an operator.')
  }

  while (stack.length) {
    const top = stack.pop() as string

    if (top === '(') {
      return failure('Unbalanced parentheses in the expression.')
    }

    output.push(rpnFor(top))
  }

  if (!identifiers.length) {
    return failure('The expression does not reference any series.')
  }

  return { ok: true, rpn: output.join(','), identifiers }
}
