import API from '@/services'
import { FileEditorResponseLog, VuexContext } from '@/types'
import { IFile, State } from './state'

interface ContextWithState extends VuexContext {
  state: State
}

const getFileNames = async (context: ContextWithState) => {
  const fileNames = await API.getFileNames(context.state.changedFilesOnly)
  context.commit('SAVE_FILE_NAMES_TO_STATE', fileNames)
  context.commit('SAVE_FOLDER_FILE_STRUCTURE', fileNames)
}

const getFileExtensions = async (context: VuexContext) => {
  const extensions = await API.getFileExtensions()
  context.commit('SAVE_FILE_EXTENSIONS_TO_STATE', extensions)
}

const getFile = async (context: VuexContext, fileName: string) => {
  // reset modified file string, snippets
  context.commit('SAVE_MODIFIED_FILE_STRING', '')
  context.commit('SAVE_SNIPPETS_TO_STATE', '')
  context.commit('SAVE_IS_CONTENT_MODIFIED_TO_STATE', false)
  context.commit('SAVE_SELECTED_FILE_NAME_TO_STATE', fileName)

  const file = await API.getFile(fileName)
  const snippets = await API.getSnippets(fileName)

  context.commit('SAVE_FILE_TO_STATE', file)
  context.commit('SAVE_SNIPPETS_TO_STATE', snippets)
}

const deleteFile = async (context: ContextWithState, fileName: string) => {
  // runs after delete
  const clearSearch = () => {
    // clear the search if it matches the deleted file
    const searchValue = context.state.searchValue.toLowerCase()
    const actualFileName = fileName.split('/').pop()?.toLowerCase()
    if (searchValue == actualFileName) {
      context.dispatch('setSearchValue', '')
    }
  }

  if (context.state.unsavedFiles.includes(fileName)) {
    // file not in DB, just delete from state
    context.commit('REMOVE_FROM_UNSAVED_FILES_LIST', fileName)
    context.commit('REMOVE_UNSAVED_FILE_FROM_STATE', fileName)
    context.dispatch('clearEditor')
    context.dispatch('setSelectedFileName', '')
    context.dispatch('setFileToDelete', null) // closes confirmation modal

    clearSearch()
    return
  }

  const response = await API.deleteFile(fileName)
  context.commit('ADD_LOG_TO_STATE', response)

  if (response.success) {
    context.dispatch('getFileNames')
    context.dispatch('clearEditor')
    context.dispatch('setSelectedFileName', '')
    clearSearch()
  }

  if (!response.success && response.msg) {
    context.commit('SET_IS_CONSOLE_OPEN', true)
  }

  context.dispatch('setFileToDelete', null) // closes confirmation modal
}

const saveModifiedFile = async (context: ContextWithState) => {
  const xml = 'xml',
    plain = 'plain'
  const filename = context.state.selectedFileName
  if (!filename) return

  const fileString = context.state.modifiedFileString
  const splitFilename = filename.split('.')
  const filetype = splitFilename[splitFilename.length - 1]
  const mimetype = filetype === xml ? xml : plain
  const doc = new File([fileString], filename, { type: `text/${mimetype}` })

  const formData = new FormData()
  formData.append('upload', doc)
  const response = await API.postFile(filename, formData)

  context.commit('ADD_LOG_TO_STATE', response)

  if (response.success) {
    context.commit('REMOVE_FROM_UNSAVED_FILES_LIST', filename)
    context.commit('SAVE_FILE_TO_STATE', fileString)
    context.commit('SAVE_IS_CONTENT_MODIFIED_TO_STATE', false)
  } else {
    if (response.msg) context.commit('SET_IS_CONSOLE_OPEN', true)
  }
}

const addNewFileToState = async (context: VuexContext, newFilePath: string) => {
  context.commit('SAVE_NEW_FILE_TO_STATE', newFilePath)
}

const setSelectedFileName = async (context: VuexContext, fileName: string) => {
  context.commit('SAVE_SELECTED_FILE_NAME_TO_STATE', fileName)
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

const setChangedFilesOnly = async (context: VuexContext, changedOnly: boolean) => {
  context.commit('SAVE_CHANGED_FILES_ONLY', changedOnly)
}

const clearEditor = async (context: VuexContext) => {
  context.commit('CLEAR_EDITOR')
}

const triggerFileReset = async (context: VuexContext) => {
  context.commit('TRIGGER_FILE_RESET')
}

const addLog = (context: VuexContext, log: FileEditorResponseLog) => {
  context.commit('ADD_LOG_TO_STATE', log)
}

const clearLogs = (context: VuexContext) => {
  context.commit('CLEAR_LOGS')
}

const setIsConsoleOpen = (context: VuexContext, isOpen: boolean) => {
  context.commit('SET_IS_CONSOLE_OPEN', isOpen)
}

const setIsHelpOpen = (context: VuexContext, isOpen: boolean) => {
  context.commit('SET_IS_HELP_OPEN', isOpen)
}

const setFileToDelete = (context: VuexContext, file: IFile | null) => {
  context.commit('SET_FILE_TO_DELETE', file)
}

const addFileToUnsavedFilesList = (context: VuexContext, unsavedFile: string) => {
  context.commit('ADD_TO_UNSAVED_FILES_LIST', unsavedFile)
}

export default {
  addLog,
  getFile,
  clearLogs,
  deleteFile,
  clearEditor,
  getFileNames,
  setIsHelpOpen,
  setSearchValue,
  setFileToDelete,
  triggerFileReset,
  setIsConsoleOpen,
  saveModifiedFile,
  addNewFileToState,
  getFileExtensions,
  setChangedFilesOnly,
  setSelectedFileName,
  setModifiedFileString,
  setIsFileContentModified,
  addFileToUnsavedFilesList
}
