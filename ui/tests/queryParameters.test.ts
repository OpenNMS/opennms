import { queryParametersHandler } from '../src/services/serviceHelpers'
import { QueryParameters } from '../src/types'
import { test } from 'uvu'
import * as assert from 'uvu/assert'

const testEndpoint = 'my-endpoint'

const testQueryParams: QueryParameters = {
  search: 'val1',
  order: 'asc' as any,
  orderBy: 'property'
}

const expectedResult = 'my-endpoint?search=val1&order=asc&orderBy=property'

test('The query parameter handler', () => {
  assert.type(queryParametersHandler, 'function')
  assert.equal(queryParametersHandler(testQueryParams, testEndpoint), expectedResult)
})

test.run()
