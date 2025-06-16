///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

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
