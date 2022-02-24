import API from '@/services'
import { VuexContext } from '@/types'

const search = async (context: VuexContext, searchStr: string) => {
  const responses = await API.search(searchStr)

  if (responses) {
    // add label and filter actions for dropdown display
    const results = responses.filter((resp) => {
      resp.label = resp.context.name
      if (resp.label !== 'Action') return resp
    })
    context.commit('SAVE_SEARCH_RESULTS', results)
    return results
  }

  return
}

export default {
  search
}
