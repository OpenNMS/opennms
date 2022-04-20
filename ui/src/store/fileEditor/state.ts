import { FileEditorResponseLog } from '@/types'

export interface IFile {
  name: string
  children?: IFile[]
  fullPath?: string
  isEditing?: boolean
  isHidden?: boolean
}

export interface State {
  fileNames: string[]
  file: string
  snippets: string
  searchValue: string
  contentModified: boolean
  selectedFileName: string
  modifiedFileString: string
  logs: FileEditorResponseLog[]
  isConsoleOpen: boolean
  isHelpOpen: boolean
  filesInFolders: IFile
  allowedFileExtensions: string[]
  changedFilesOnly: boolean
  fileToDelete: IFile | null
  unsavedFiles: string[]
}

const state: State = {
  fileNames: [],
  file: '',
  snippets: '',
  searchValue: '',
  contentModified: false,
  selectedFileName: '',
  modifiedFileString: '',
  logs: [],
  isConsoleOpen: false,
  isHelpOpen: false,
  filesInFolders: {} as IFile,
  allowedFileExtensions: [],
  changedFilesOnly: false,
  fileToDelete: null,
  unsavedFiles: []
}

export default state
