import { FeatherSortObject, QueryParameters } from '@/types'

const useQueryParameters = (
  initialParameters: QueryParameters,
  call: (params: QueryParameters) => Promise<void>,
  optionalPayload?: { [key: string]: any }
) => {
  const queryParameters = ref(initialParameters)
  const payload = ref({ queryParameters: queryParameters.value, ...optionalPayload })

  const updateQueryParameters = (updatedParams: QueryParameters) => { queryParameters.value = updatedParams }

  const sort = (sortProps: FeatherSortObject) => {
    const updatedQueryParams = {
      ...queryParameters.value,
      orderBy: sortProps.property,
      order: sortProps.value
    }
    queryParameters.value = updatedQueryParams

    call(optionalPayload ? { ...payload.value, queryParameters: updatedQueryParams } : updatedQueryParams)
  }

  return { queryParameters, sort, updateQueryParameters, payload }
}

export default useQueryParameters
