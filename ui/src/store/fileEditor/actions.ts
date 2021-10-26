import API from "@/services"
import { VuexContext } from '@/types'

const getFileNames = async (context: VuexContext) => {
  const fileNames = await API.getFileNames()
  context.commit('SAVE_FILE_NAMES_TO_STATE', fileNames)
}

const getFile = async (context: VuexContext, fileName: string) => {
  const file = await API.getFile(fileName)
  context.commit('SAVE_FILE_TO_STATE', file)
}

const postFile = async (context: VuexContext, payload: { formData: File, fileName: string }) => {
  await API.postFile(payload)
}

export default {
  getFileNames,
  getFile,
  postFile
}
