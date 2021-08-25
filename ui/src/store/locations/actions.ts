import API from "@/services"
import { VuexContext } from '@/types'

const getLocations = async (context: VuexContext) => {
  const resp = await API.getLocations()
  if (resp) {
    context.commit('SAVE_LOCATIONS_TO_STATE', resp.location)
  }
}

export default {
  getLocations
}
