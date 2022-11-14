import API from '@/services'
import { SearchResultResponse, VuexContext } from '@/types'

export interface FullStoreState extends VuexContext {
  state:{loading: boolean}
}

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

/**
 * 
 * @param context Since this is a dispatch based action, it will receive the full VueX context as the first result
 * @param searchStr The string we want to search OpenNMS for
 */
const search = async (context: FullStoreState, searchStr: string) => {
  context.commit('SET_LOADING',true)
  const responses = await API.search(searchStr)
  const responsesByLabel = buildResponsesByLabel(responses)
  context.commit('SAVE_SEARCH_RESULTS', {responses,responsesByLabel})
  context.commit('SET_LOADING',false)
}

const setLoading = async (context: FullStoreState, loading: boolean) => {
  context.commit('SET_LOADING',loading)
}

export default {
  search,
  setLoading
}
