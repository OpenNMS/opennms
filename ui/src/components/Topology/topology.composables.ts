import { SearchResultResponse } from '@/types'
import { Node } from 'v-network-graph'
import store from '@/store'

export const useFocus = () => {
  // set single node as focus
  const setContextNodeAsFocus = async (node: Node) => {
    const results: SearchResultResponse[] = await store.dispatch('searchModule/search', node.name)

    if (results) {
      store.dispatch('topologyModule/setFocusedSearchBarNodes', [results[0].results[0]])
      store.dispatch('topologyModule/addFocusedNodeIds', [node.id])
    }
  }

  return { setContextNodeAsFocus }
}
