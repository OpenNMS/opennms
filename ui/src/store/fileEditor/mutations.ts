import { FileEditorResponseLog } from '@/types'
import { State } from './state'
import { filesToFolders } from '@/components/FileEditor/utils'

const SAVE_FILE_NAMES_TO_STATE = (state: State, fileNames: string[]) => {
  state.fileNames = fileNames
}

const SAVE_FOLDER_FILE_STRUCTURE = (state: State, fileNames: string[]) => {
  state.filesInFolders = filesToFolders(fileNames)
}

const SAVE_FILE_TO_STATE = (state: State, file: string) => {
  state.file = file
}

const SAVE_SNIPPETS_TO_STATE = (state: State, snippets: string) => {
  state.snippets = snippets
}

const SAVE_SEARCH_VALUE_TO_STATE = (state: State, searchValue: string) => {
  state.searchValue = searchValue
  const filteredFileNames = state.fileNames.filter((fileName) => !searchValue || (searchValue && fileName.includes(searchValue)))
  state.filesInFolders = filesToFolders(filteredFileNames)
}

const SAVE_IS_CONTENT_MODIFIED_TO_STATE = (state: State, contentModified: boolean) => {
  state.contentModified = contentModified
}

const CLEAR_EDITOR = (state: State) => {
  state.file = 'clear'
  state.modifiedFileString = 'clear' // trigger reaction
  state.file = ' '
  state.modifiedFileString = ''
  state.contentModified = false
}

const TRIGGER_FILE_RESET = (state: State) => {
  const savedFile = state.file
  state.file = '' // trigger reaction
  state.file = savedFile
  state.contentModified = false
}

const SAVE_SELECTED_FILE_NAME_TO_STATE = (state: State, selectedFileName: string) => {
  state.selectedFileName = selectedFileName
}

const SAVE_MODIFIED_FILE_STRING = (state: State, modifiedFileString: string) => {
  state.modifiedFileString = modifiedFileString
}

const ADD_LOG_TO_STATE = (state: State, log: FileEditorResponseLog) => {
  state.logs = [...state.logs, log]
}

const CLEAR_LOGS = (state: State) => {
  state.logs = []
}

const SET_IS_CONSOLE_OPEN = (state: State, isOpen: boolean) => {
  state.isConsoleOpen = isOpen
}

const SET_IS_HELP_OPEN = (state: State, isOpen: boolean) => {
  state.isHelpOpen = isOpen
}

export default {
  SAVE_FILE_NAMES_TO_STATE,
  SAVE_FILE_TO_STATE,
  SAVE_SNIPPETS_TO_STATE,
  SAVE_SEARCH_VALUE_TO_STATE,
  SAVE_IS_CONTENT_MODIFIED_TO_STATE,
  TRIGGER_FILE_RESET,
  SAVE_SELECTED_FILE_NAME_TO_STATE,
  SAVE_MODIFIED_FILE_STRING,
  ADD_LOG_TO_STATE,
  CLEAR_LOGS,
  SET_IS_CONSOLE_OPEN,
  SET_IS_HELP_OPEN,
  SAVE_FOLDER_FILE_STRUCTURE,
  CLEAR_EDITOR
}
