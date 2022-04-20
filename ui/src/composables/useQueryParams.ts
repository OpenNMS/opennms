import { FeatherSortObject, QueryParameters } from '@/types'
import { useStore } from 'vuex'

const useQueryParameters = (
  initialParameters: QueryParameters,
  call: string,
  optionalPayload?: { [key: string]: any }
) => {
  const store = useStore()

  const queryParameters = ref(initialParameters)
  const payload = ref({ queryParameters: queryParameters.value, ...optionalPayload })

  const updateQueryParameters = (updatedParams: QueryParameters) => (queryParameters.value = updatedParams)

  const sort = (sortProps: FeatherSortObject) => {
    const updatedQueryParams = {
      ...queryParameters.value,
      orderBy: sortProps.property,
      order: sortProps.value
    }
    queryParameters.value = updatedQueryParams
    store.dispatch(
      call,
      optionalPayload ? { ...payload.value, queryParameters: updatedQueryParams } : updatedQueryParams
    )
  }

  return { queryParameters, sort, updateQueryParameters, payload }
}

export default useQueryParameters
