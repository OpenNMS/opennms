import { describe, it, expect } from 'vitest'
import { sortPredicate } from '@/lib/sorting'
import { SORT } from '@featherds/table'
import { FeatherSortObject } from '@/types'

describe('sorting', () => {
  describe('sortPredicate', () => {
    const createSortObject = (property: string, value: string): FeatherSortObject => ({
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
          a: { data: { value: 1 } },
          b: { data: { value: 2 } },
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
