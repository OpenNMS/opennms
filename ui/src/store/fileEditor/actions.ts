import API from "@/services"
import { VuexContext } from '@/types'
import { State } from './state'

interface ContextWithState extends VuexContext {
  state: State
}

const getFileNames = async (context: VuexContext) => {
  const fileNames = await API.getFileNames()
  context.commit('SAVE_FILE_NAMES_TO_STATE', fileNames)
}

const getFile = async (context: VuexContext, fileName: string) => {
  const file = await API.getFile(fileName)
  const snippets = await API.getSnippets(fileName)
  context.commit('SAVE_FILE_TO_STATE', file)
  context.commit('SAVE_SNIPPETS_TO_STATE', snippets)
  context.commit('SAVE_SELECTED_FILE_NAME_TO_STATE', fileName)
}

const saveModifiedFile = async (context: ContextWithState) => {
  const filename = context.state.selectedFileName
  const fileString = context.state.modifiedFileString
  const filetype = filename.split('.')[1]
  const doc = new File([fileString], filename, { type: `text/${filetype}` })

  const formData = new FormData()
  formData.append('upload', doc)
  await API.postFile(filename, formData)
}

const setModifiedFileString = async (context: VuexContext, modifiedFileString: string) => {
  context.commit('SAVE_MODIFIED_FILE_STRING', modifiedFileString)
}

const setSearchValue = async (context: VuexContext, searchValue: string) => {
  context.commit('SAVE_SEARCH_VALUE_TO_STATE', searchValue)
}

const setIsFileContentModified = async (context: VuexContext, contentModified: boolean) => {
  context.commit('SAVE_IS_CONTENT_MODIFIED_TO_STATE', contentModified)
}

const triggerFileReset = async (context: VuexContext) => {
  context.commit('TRIGGER_FILE_RESET')
}

export default {
  getFileNames,
  getFile,
  saveModifiedFile,
  setSearchValue,
  setIsFileContentModified,
  setModifiedFileString,
  triggerFileReset
}
