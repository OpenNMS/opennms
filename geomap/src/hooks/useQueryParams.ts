//this file is copied from vue-ui. 
import { QueryParameters, SortProps } from '@/types'
import { ref } from 'vue'
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

  const sort = (sortProps: SortProps) => {
    const updatedQueryParams = {
      ...queryParameters.value,
      orderBy: sortProps.sortField,
      order: sortProps.sortOrder === 1 ? ('asc' as 'asc') : ('desc' as 'desc')
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