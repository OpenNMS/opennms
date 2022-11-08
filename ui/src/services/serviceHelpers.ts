import { QueryParameters } from '@/types'

const queryParametersHandler = (queryParameters: QueryParameters, endpoint: string): string => {
  let modifiedEndpoint = endpoint + '?'
  let queryString = ''

  for (const key in queryParameters) {
    queryString = `${queryString}${key}=${(queryParameters as any)[key]}&`
  }

  modifiedEndpoint += queryString

  // remove last useless ampersand
  return modifiedEndpoint.slice(0, -1)
}

export { queryParametersHandler }
