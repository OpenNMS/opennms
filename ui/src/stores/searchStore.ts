import { defineStore } from 'pinia'
import API from '@/services'
import { SearchResultResponse, SearchResultsByContext } from '@/types'

/**
 * Could be moved to a Services/Helper area.
 * @param anyArray A string based array
 * @returns A string based array that is unique (duplicate entries removed)
 */
const makeArrayUnique = (anyArray: Array<string>) => {
  return [...new Set(anyArray)]
}

/**
 * 
 * @param responses A list of search results provided by OpenNMS API.Search()
 * @returns The same search results, but organized by Context Label (Alarm, Node)
 */
const buildResponsesByLabel = (responses: SearchResultResponse[] | false) => {
  const responsesByLabel: Array<{results:SearchResultResponse[],label:string}> = []

  if (responses){
    const listOfAllContexts = responses.map((i) => i.context.name)
    const contextList = makeArrayUnique(listOfAllContexts)

    for (const label of contextList){
      responsesByLabel.push({label,results:responses.filter((res) => res.context.name === label)})
    }
  }
  return responsesByLabel
}

export const useSearchStore = defineStore('searchStore', () => {
  const searchResults = ref([] as SearchResultResponse[])
  const searchResultsByContext = ref([] as SearchResultsByContext)
  const loading = ref(false)

  /**
   * @param searchStr The string we want to search OpenNMS for
   */
  const search = async (searchStr: string) => {
    loading.value = true

    const resp = await API.search(searchStr)

    if (resp !== false) {
      const responses = resp as SearchResultResponse[]
      const responsesByLabel = buildResponsesByLabel(responses)

      searchResults.value = responses
      searchResultsByContext.value = [...responsesByLabel]
    }

    loading.value = false
  }

  const setLoading = async (value: boolean) => {
    loading.value = value
  }

  return {
    searchResults,
    searchResultsByContext,
    loading,
    search,
    setLoading
  }
})
