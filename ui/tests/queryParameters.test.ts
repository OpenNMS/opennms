import { queryParametersHandler } from '@/services/serviceHelpers'
import { QueryParameters } from '@/types'
import { assert, test } from 'vitest'

const testEndpoint = 'my-endpoint'

const testQueryParams: QueryParameters = {
  search: 'val1',
  order: 'asc' as any,
  orderBy: 'property'
}

const expectedResult = 'my-endpoint?search=val1&order=asc&orderBy=property'

test('The query parameter handler', () => {
  assert.equal(queryParametersHandler(testQueryParams, testEndpoint), expectedResult)
})
