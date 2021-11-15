import { FileEditorResponseLog } from '@/types'
import { State, IFile } from './state'

const filesToFolders = (fileNames: string[]): IFile => {
  const files: IFile[] = []

  const createFolder = (fileArray: IFile[], file: string) => {
    const fileNamePieces = file.split('/')
    const folder = fileNamePieces[0]
    fileNamePieces.shift()
    const remaining = fileNamePieces.join('/')
    const existingFolder = fileArray.filter(x => x.name === folder)[0] as Required<IFile>

    if (existingFolder) addFileOrCreateFolder(existingFolder.children, remaining)
    else fileArray.push({ name: folder, children: addFileOrCreateFolder([], remaining) })
  }

  const addFileOrCreateFolder = (fileArray: IFile[], file: string): IFile[] => {
    if (file.includes('/')) createFolder(fileArray, file)
    else fileArray.push({ name: file })
    return fileArray
  }

  for (const file of fileNames) addFileOrCreateFolder(files, file)
  return { name: 'Files', children: files }
}

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
}

const SAVE_IS_CONTENT_MODIFIED_TO_STATE = (state: State, contentModified: boolean) => {
  state.contentModified = contentModified
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
  SAVE_FOLDER_FILE_STRUCTURE
}
