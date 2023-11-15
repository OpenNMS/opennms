import { describe, expect, test } from 'vitest'
import {
  ellipsify,
  hasNonEmptyProperty,
  isConvertibleToInteger,
  isNumber,
  isString
} from '@/lib/utils'

describe('lib/utils test', () => {
  test('test isNumber', async () => {
    const numberValues = [0, 1, 999, -1, -999, NaN]

    const nonNumberValues = [null, undefined, '', '0', '1', '999', '-1', '-999', 'abc', [0], { a: 1 }]

    for (const value of numberValues) {
      expect(isNumber(value)).toBe(true)
    }

    for (const value of nonNumberValues) {
      expect(isNumber(value)).toBe(false)
    }
  })

  test('test isConvertibleToInteger', async () => {
    const integerValues = [0, 1, 999, -1, -999, '0', '1', '999', '-1', '-999', [0], [1]]

    const nonIntegerValues = [null, undefined, '', 'abc', [0, 1], { a: 1 }, NaN, 1.1, -9.9]

    for (const value of integerValues) {
      expect(isConvertibleToInteger(value)).toBe(true)
    }

    for (const value of nonIntegerValues) {
      expect(isConvertibleToInteger(value), `value failed: ${value}`).toBe(false)
    }
  })

  test('test isString', async () => {
    const stringValues = ['', 'a', 'abc', 'DEFghi', 'MNO123!!!', new String(), new String('hello')]

    const nonStringValues = [null, 1, -1, 999, [0, 1], { a: 1 }, NaN, 1.1, -9.9]

    for (const value of stringValues) {
      expect(isString(value)).toBe(true)
    }

    for (const value of nonStringValues) {
      expect(isString(value)).toBe(false)
    }
  })

  test('test ellipsify', async () => {
    expect(ellipsify('', 1)).toBe('')
    expect(ellipsify('', 4)).toBe('')
    expect(ellipsify('abc', 4)).toBe('abc')
    expect(ellipsify('abcdef', 4)).toBe('abcd...')
    expect(ellipsify('Lorem ipsum dolor sit amet', 20)).toBe('Lorem ipsum dolor si...')
  })

  test('test hasNonEmptyProperty', async () => {
    const emptyProperties = [null, undefined, {}, { a: 1 }, { a: [1, 2, 3] }]

    const nonEmptyProperties = [{ a: 'a' }, { a: 1, b: 'two' }]

    for (const value of emptyProperties) {
      expect(hasNonEmptyProperty(value), `value: ${value}`).toBe(false)
    }

    for (const value of nonEmptyProperties) {
      expect(hasNonEmptyProperty(value)).toBe(true)
    }
  })
})
