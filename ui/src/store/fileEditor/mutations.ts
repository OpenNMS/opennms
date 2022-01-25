import { FileEditorResponseLog } from '@/types'
import { IFile, State } from './state'
import { filesToFolders } from '@/components/FileEditor/utils'
import { uniq } from 'lodash'

const SAVE_FILE_NAMES_TO_STATE = (state: State, fileNames: string[]) => {
  state.fileNames = fileNames
}

const SAVE_FILE_EXTENSIONS_TO_STATE = (state: State, extensions: string[]) => {
  state.allowedFileExtensions = extensions
}

const SAVE_FOLDER_FILE_STRUCTURE = (state: State, fileNames: string[]) => {
  const savedAndUnsavedFiles = uniq([...fileNames, ...state.unsavedFiles])
  const filteredFileNames = savedAndUnsavedFiles.filter((fileName) => {
    const searchValue = state.searchValue.toLowerCase()
    const filename = fileName.toLowerCase()
    const selectedFileName = state.selectedFileName.toLowerCase()
    return !searchValue || filename === selectedFileName || (searchValue && filename.includes(searchValue))
  })

  state.filesInFolders = filesToFolders(filteredFileNames)
}

const SAVE_NEW_FILE_TO_STATE = (state: State, newFilePath: string) => {
  state.fileNames = [...state.fileNames, newFilePath]
  SAVE_FOLDER_FILE_STRUCTURE(state, state.fileNames)
}

const REMOVE_UNSAVED_FILE_FROM_STATE = (state: State, newFilePath: string) => {
  state.fileNames = state.fileNames.filter((path) => path !== newFilePath)
  SAVE_FOLDER_FILE_STRUCTURE(state, state.fileNames)
}

const SAVE_FILE_TO_STATE = (state: State, file: string) => {
  state.file = file
}

const SAVE_SNIPPETS_TO_STATE = (state: State, snippets: string) => {
  state.snippets = snippets
}

const SAVE_SEARCH_VALUE_TO_STATE = (state: State, searchValue: string) => {
  state.searchValue = searchValue
  SAVE_FOLDER_FILE_STRUCTURE(state, state.fileNames)
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

const SAVE_CHANGED_FILES_ONLY = (state: State, changedFilesOnly: boolean) => {
  state.changedFilesOnly = changedFilesOnly
}

const SET_FILE_TO_DELETE = (state: State, fileToDelete: IFile | null) => {
  state.fileToDelete = fileToDelete
}

const ADD_TO_UNSAVED_FILES_LIST = (state: State, unsavedFile: string) => {
  state.unsavedFiles = [...state.unsavedFiles, unsavedFile]
}

const REMOVE_FROM_UNSAVED_FILES_LIST = (state: State, unsavedFile: string) => {
  state.unsavedFiles = state.unsavedFiles.filter((path) => path !== unsavedFile)
}

export default {
  CLEAR_LOGS,
  CLEAR_EDITOR,
  ADD_LOG_TO_STATE,
  SET_IS_HELP_OPEN,
  TRIGGER_FILE_RESET,
  SAVE_FILE_TO_STATE,
  SET_FILE_TO_DELETE,
  SET_IS_CONSOLE_OPEN,
  SAVE_NEW_FILE_TO_STATE,
  SAVE_SNIPPETS_TO_STATE,
  SAVE_CHANGED_FILES_ONLY,
  SAVE_FILE_NAMES_TO_STATE,
  ADD_TO_UNSAVED_FILES_LIST,
  SAVE_MODIFIED_FILE_STRING,
  SAVE_FOLDER_FILE_STRUCTURE,
  SAVE_SEARCH_VALUE_TO_STATE,
  SAVE_FILE_EXTENSIONS_TO_STATE,
  REMOVE_UNSAVED_FILE_FROM_STATE,
  REMOVE_FROM_UNSAVED_FILES_LIST,
  SAVE_SELECTED_FILE_NAME_TO_STATE,
  SAVE_IS_CONTENT_MODIFIED_TO_STATE
}
