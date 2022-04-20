import { numericSeverityLevel } from '@/components/Map/utils'
import { assert, test } from 'vitest'

const isGreater = (a: number, b: number) => {
  assert.equal(a > b, true)
}

test('Ensure precedence', () => {
  assert.equal(numericSeverityLevel(undefined), 0)
  isGreater(numericSeverityLevel('CRITICAL'), numericSeverityLevel('MAJOR'))
  isGreater(numericSeverityLevel('MAJOR'), numericSeverityLevel('MINOR'))
  isGreater(numericSeverityLevel('MINOR'), numericSeverityLevel('WARNING'))
  isGreater(numericSeverityLevel('WARNING'), numericSeverityLevel('NORMAL'))
})
