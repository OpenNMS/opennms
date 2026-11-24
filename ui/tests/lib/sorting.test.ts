import { describe, it, expect } from 'vitest'
import { compareBy, compareValues, sortPredicate } from '@/lib/sorting'
import { SORT } from '@/types'
import { ISortObject } from '@/types'

describe('sorting', () => {
  describe('sortPredicate', () => {
    const createSortObject = (property: string, value: string): ISortObject => ({
      property,
      value
    })

    describe('ascending sort', () => {
      const testCases = [
        {
          description: 'should sort strings in ascending order',
          property: 'name',
          a: { name: 'apple' },
          b: { name: 'banana' },
          expected: 'lessThan'
        },
        {
          description: 'should return 0 for equal strings',
          property: 'name',
          a: { name: 'apple' },
          b: { name: 'apple' },
          expected: 'equal'
        },
        {
          description: 'should handle numeric values as strings',
          property: 'value',
          a: { value: 10 },
          b: { value: 20 },
          expected: 'lessThan'
        },
        {
          description: 'should handle case-insensitive sorting',
          property: 'name',
          a: { name: 'Apple' },
          b: { name: 'banana' },
          expected: 'lessThan'
        },
        {
          description: 'should handle special characters',
          property: 'name',
          a: { name: 'test-1' },
          b: { name: 'test-2' },
          expected: 'lessThan'
        },
        {
          description: 'should handle empty strings',
          property: 'name',
          a: { name: '' },
          b: { name: 'something' },
          expected: 'lessThan'
        }
      ]

      testCases.forEach(({ description, property, a, b, expected }) => {
        it(description, () => {
          const sortObj = createSortObject(property, SORT.ASCENDING)
          const result = sortPredicate(a, b, sortObj)

          if (expected === 'lessThan') {
            expect(result).toBeLessThan(0)
          } else if (expected === 'equal') {
            expect(result).toBe(0)
          } else if (expected === 'greaterThan') {
            expect(result).toBeGreaterThan(0)
          }
        })
      })
    })

    describe('descending sort', () => {
      const testCases = [
        {
          description: 'should sort strings in descending order',
          property: 'name',
          a: { name: 'apple' },
          b: { name: 'banana' },
          expected: 'greaterThan'
        },
        {
          description: 'should return 0 for equal strings in descending order',
          property: 'name',
          a: { name: 'apple' },
          b: { name: 'apple' },
          expected: 'equal'
        },
        {
          description: 'should reverse comparison in descending order',
          property: 'name',
          a: { name: 'zebra' },
          b: { name: 'apple' },
          expected: 'lessThan'
        },
        {
          description: 'should handle numeric values in descending order',
          property: 'value',
          a: { value: 30 },
          b: { value: 10 },
          expected: 'lessThan'
        },
        {
          description: 'should handle empty strings',
          property: 'name',
          a: { name: '' },
          b: { name: 'something' },
          expected: 'greaterThan'
        }
      ]

      testCases.forEach(({ description, property, a, b, expected }) => {
        it(description, () => {
          const sortObj = createSortObject(property, SORT.DESCENDING)
          const result = sortPredicate(a, b, sortObj)

          if (expected === 'lessThan') {
            expect(result).toBeLessThan(0)
          } else if (expected === 'equal') {
            expect(result).toBe(0)
          } else if (expected === 'greaterThan') {
            expect(result).toBeGreaterThan(0)
          }
        })
      })
    })

    describe('edge cases', () => {
      const testCases = [
        {
          description: 'should handle null values',
          property: 'name',
          a: { name: null },
          b: { name: 'something' },
          assertion: (result: number) => expect(result).toBeLessThan(0)
        },
        {
          description: 'should handle undefined values',
          property: 'name',
          a: { name: undefined },
          b: { name: 'something' },
          assertion: (result: number) => expect(result).toBeLessThan(0)
        },
        {
          description: 'should handle boolean values',
          property: 'active',
          a: { active: false },
          b: { active: true },
          assertion: (result: number) => expect(result).toBeLessThan(0)
        },
        {
          description: 'should handle object values',
          property: 'data',
          a: { data: { value: 1 }},
          b: { data: { value: 2 }},
          assertion: (result: number) => expect(typeof result).toBe('number')
        }
      ]

      testCases.forEach(({ description, property, a, b, assertion }) => {
        it(description, () => {
          const sortObj = createSortObject(property, SORT.ASCENDING)
          const result = sortPredicate(a, b, sortObj)
          assertion(result)
        })
      })
    })

    describe('sorting with different properties', () => {
      const testCases = [
        {
          description: 'should sort based on the specified property',
          property: 'age',
          a: { name: 'John', age: 25 },
          b: { name: 'Jane', age: 30 },
          expected: 'lessThan'
        },
        {
          description: 'should ignore other properties not specified in sort object',
          property: 'city',
          a: { name: 'John', city: 'New York' },
          b: { name: 'Jane', city: 'Boston' },
          expected: 'greaterThan'
        }
      ]

      testCases.forEach(({ description, property, a, b, expected }) => {
        it(description, () => {
          const sortObj = createSortObject(property, SORT.ASCENDING)
          const result = sortPredicate(a, b, sortObj)

          if (expected === 'lessThan') {
            expect(result).toBeLessThan(0)
          } else if (expected === 'greaterThan') {
            expect(result).toBeGreaterThan(0)
          }
        })
      })
    })

    describe('array sorting', () => {
      const testCases = [
        {
          description: 'should correctly sort an array of objects in ascending order',
          sortOrder: SORT.ASCENDING,
          expectedOrder: ['Alice', 'Bob', 'Charlie']
        },
        {
          description: 'should correctly sort an array of objects in descending order',
          sortOrder: SORT.DESCENDING,
          expectedOrder: ['Charlie', 'Bob', 'Alice']
        }
      ]

      testCases.forEach(({ description, sortOrder, expectedOrder }) => {
        it(description, () => {
          const sortObj = createSortObject('name', sortOrder)
          const array = [
            { name: 'Charlie' },
            { name: 'Alice' },
            { name: 'Bob' }
          ]

          const sorted = array.sort((a, b) => sortPredicate(a, b, sortObj))
          expectedOrder.forEach((name, index) => {
            expect(sorted[index].name).toBe(name)
          })
        })
      })
    })
  })
})

describe('compareValues', () => {
  it('sorts numbers numerically', () => {
    expect([10, 9, 100, 1].sort(compareValues)).toEqual([1, 9, 10, 100])
  })

  it('sorts strings by code unit, uppercase before lowercase', () => {
    expect(['banana', 'apple', 'Cherry'].sort(compareValues)).toEqual(['Cherry', 'apple', 'banana'])
  })

  it('puts null, undefined and NaN after everything else', () => {
    expect([undefined, 2, null, NaN, 1].sort(compareValues).slice(0, 2)).toEqual([1, 2])
    expect(compareValues(null, undefined)).toBe(0)
    expect(compareValues(undefined, 1)).toBe(1)
    expect(compareValues(1, null)).toBe(-1)
  })

  it('returns 0 for equal values', () => {
    expect(compareValues('a', 'a')).toBe(0)
    expect(compareValues(3, 3)).toBe(0)
  })
})

describe('compareBy', () => {
  const items = [{ id: 10, name: 'b' }, { id: 9, name: 'a' }, { id: undefined, name: 'c' }, { id: 100, name: 'b' }]

  it('sorts ascending by default, missing values last', () => {
    expect([...items].sort(compareBy('id')).map(item => item.id)).toEqual([9, 10, 100, undefined])
  })

  it('sorts descending with SORT.DESCENDING, missing values first', () => {
    expect([...items].sort(compareBy('id', SORT.DESCENDING)).map(item => item.id)).toEqual([undefined, 100, 10, 9])
  })

  it('treats any other direction as ascending', () => {
    expect([...items].sort(compareBy('id', SORT.NONE)).map(item => item.id)).toEqual([9, 10, 100, undefined])
  })

  it('keeps equal items in their original order', () => {
    expect([...items].sort(compareBy('name')).map(item => item.id)).toEqual([9, 10, 100, undefined])
    expect([...items].sort(compareBy('name', SORT.DESCENDING)).map(item => item.id)).toEqual([undefined, 10, 100, 9])
  })
})
