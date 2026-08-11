import { describe, expect, it } from 'vitest'

import { jexlToRpn, JexlToRpnSuccess } from '@/components/AdhocGraphs/utils/jexlToRpn'
import RpnToJexlConverter from '@/components/Resources/utils/RpnToJexlConverter.class'

const rpnOf = (expression: string): string => {
  const result = jexlToRpn(expression)
  expect(result.ok, `expected ${expression} to convert`).toBe(true)
  return (result as JexlToRpnSuccess).rpn
}

const reasonOf = (expression: string): string => {
  const result = jexlToRpn(expression)
  expect(result.ok, `expected ${expression} to be refused`).toBe(false)
  return (result as { ok: false, reason: string }).reason
}

/** Evaluate RRD RPN over a scope, so a conversion can be checked by its result. */
const evaluateRpn = (rpn: string, scope: Record<string, number>): number => {
  const stack: number[] = []

  for (const token of rpn.split(',')) {
    if (['+', '-', '*', '/', '%'].includes(token)) {
      const b = stack.pop() as number
      const a = stack.pop() as number
      stack.push(
        token === '+' ? a + b :
          token === '-' ? a - b :
            token === '*' ? a * b :
              token === '/' ? a / b : a % b
      )
      continue
    }

    stack.push(token in scope ? scope[token] : Number(token))
  }

  expect(stack).toHaveLength(1)
  return stack[0]
}

describe('jexlToRpn', () => {
  it('converts the canonical octets-to-bits expression', () => {
    expect(rpnOf('ifHCInOctets * 8')).toBe('ifHCInOctets,8,*')
  })

  it('collects the series it references, in order, without duplicates', () => {
    const result = jexlToRpn('(a + b) / a') as JexlToRpnSuccess
    expect(result.identifiers).toEqual(['a', 'b'])
  })

  it('honours operator precedence', () => {
    expect(rpnOf('a + b * c')).toBe('a,b,c,*,+')
    expect(rpnOf('(a + b) * c')).toBe('a,b,+,c,*')
  })

  it('is left-associative for equal precedence', () => {
    expect(rpnOf('a - b - c')).toBe('a,b,-,c,-')
    expect(rpnOf('a / b / c')).toBe('a,b,/,c,/')
  })

  it('accepts decimal and scientific literals', () => {
    expect(rpnOf('a * 1.5')).toBe('a,1.5,*')
    expect(rpnOf('a / 1e6')).toBe('a,1e6,/')
  })

  // RRD RPN has no negation operator, so -x has to become 0,x,- — and crucially
  // the 0 belongs before the whole operand, not just its last token.
  it('expands unary minus, including over a parenthesised operand', () => {
    expect(rpnOf('-a')).toBe('0,a,-')
    expect(rpnOf('-(a + b)')).toBe('0,a,b,+,-')
    expect(rpnOf('-a * b')).toBe('0,a,-,b,*')
    expect(rpnOf('a - -b')).toBe('a,0,b,-,-')
  })

  it('produces RPN that evaluates to the same number as the infix form', () => {
    const scope = { a: 12, b: 5, c: 2 }
    const cases: [string, number][] = [
      ['a + b * c', 12 + 5 * 2],
      ['(a + b) * c', (12 + 5) * 2],
      ['a - b - c', 12 - 5 - 2],
      ['-a * b', -12 * 5],
      ['-(a + b)', -(12 + 5)],
      ['a % b', 12 % 5],
      ['a * 8 / 1000', 12 * 8 / 1000]
    ]

    for (const [expression, expected] of cases) {
      expect(evaluateRpn(rpnOf(expression), scope), expression).toBeCloseTo(expected, 9)
    }
  })

  // The SPA already ships the opposite converter for reading prefab graphs, so a
  // conversion can be checked by feeding it back and comparing to the input.
  it('round-trips through the existing RpnToJexlConverter', () => {
    const converter = new RpnToJexlConverter()

    expect(converter.convert(rpnOf('ifInOctets * 8'))).toBe('(ifInOctets * 8)')
    expect(converter.convert(rpnOf('(a + b) * c'))).toBe('((a + b) * c)')
    expect(converter.convert(rpnOf('a + b * c'))).toBe('(a + (b * c))')
  })

  describe('refuses what RRD RPN cannot express', () => {
    it('function calls, naming the function', () => {
      expect(reasonOf('math:abs(a)')).toContain('Function calls are not supported')
    })

    it('conditionals', () => {
      expect(reasonOf('a > 0 ? a : 0')).toMatch(/Comparison|Conditional/)
    })

    it('comparison and logical operators', () => {
      expect(reasonOf('a > b')).toContain('Comparison and logical operators')
      expect(reasonOf('a && b')).toContain('Comparison and logical operators')
    })

    it('text values', () => {
      expect(reasonOf('a + "x"')).toContain('Text values are not supported')
    })
  })

  describe('refuses malformed input', () => {
    it('an empty expression', () => {
      expect(reasonOf('   ')).toBe('The expression is empty.')
    })

    it('an expression with no series in it', () => {
      expect(reasonOf('8 * 2')).toBe('The expression does not reference any series.')
    })

    it('unbalanced parentheses', () => {
      expect(reasonOf('(a + b')).toContain('Unbalanced parentheses')
      expect(reasonOf('a + b)')).toContain('Unbalanced parentheses')
    })

    it('a trailing operator', () => {
      expect(reasonOf('a *')).toBe('The expression ends with an operator.')
    })

    it('two values in a row', () => {
      expect(reasonOf('a b')).toContain('Missing an operator')
    })
  })
})
