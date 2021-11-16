import API from "@/services"
import { VuexContext } from '@/types'

const getSummary = async (context: VuexContext) => {
  const summary = await API.getSummary()
  context.commit('SAVE_SUMMARY_TO_STATE', summary)
}

export default {
  getSummary
}
