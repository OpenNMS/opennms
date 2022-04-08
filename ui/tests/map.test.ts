import { numericSeverityLevel } from '../src/components/Map/utils'
import { test } from 'uvu'
import * as assert from 'uvu/assert'

const isGreater = (a: number, b: number) => {
  assert.equal(a > b, true)
}

test('Ensure precedence', () => {
  assert.type(numericSeverityLevel, 'function')
  assert.equal(numericSeverityLevel(undefined), 0)
  isGreater(numericSeverityLevel('CRITICAL'), numericSeverityLevel('MAJOR'))
  isGreater(numericSeverityLevel('MAJOR'), numericSeverityLevel('MINOR'))
  isGreater(numericSeverityLevel('MINOR'), numericSeverityLevel('WARNING'))
  isGreater(numericSeverityLevel('WARNING'), numericSeverityLevel('NORMAL'))
})

test.run()
