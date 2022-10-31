import API from '@/services'
import { VuexContext } from '@/types'

const search = async (context: VuexContext, searchStr: string) => {
  const responses = await API.search(searchStr)

  if (responses) {
    // add label for dropdown display
    const results = responses.filter((resp) => {
      resp.label = resp.context.name
      return resp
    })
    context.commit('SAVE_SEARCH_RESULTS', results)
  }

  return
}

export default {
  search
}
