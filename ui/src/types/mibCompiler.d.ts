import { SORT } from '@featherds/table'

export type MibCompilerFileLocation = 'PENDING' | 'COMPILED'

export interface MibCompilerFileInfo {
  fileName: string
  location: MibCompilerFileLocation
}

export type MibFileListResponse = MibCompilerFileInfo[]

export type MibCompilerFileInfoWithContent = MibCompilerFileContentResponse

export interface MibCompilerGenerateEventsRequest {
  name: string
  ueiBase?: string
}

export interface MibCompilerStoreState {
  files: MibCompilerFileInfo[]
  isLoading: boolean
  compiledMibFilesSearchTerm: string
  pendingMibFilesSearchTerm: string
  selectedMibFile: MibCompilerFileInfoWithContent | null
  compiledMibFilesSort: {
    property: keyof MibCompilerFileInfo
    value: SORT
  }
  pendingMibFilesSort: {
    property: keyof MibCompilerFileInfo
    value: SORT
  }
  compiledMibFilesPagination: {
    page: number
    pageSize: number
    total: number
  }
  pendingMibFilesPagination: {
    page: number
    pageSize: number
    total: number
  }
}

export type UploadMibFileType = {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export interface MibUploadResponse {
  success: Array<{
    filename: string
    savedAs: string
    success: boolean
  }>
  errors: Array<{
    filename: string
    basename: string
    error: string
    exception?: string
  }>
}

export interface MibCompileResponse {
  success: boolean
  message: string
  mibName: string
  compiledFile?: string
  missingDependencies?: string[]
  errors?: string
}

export interface MibCompilerFileContentResponse {
  contents: string
  location: string
  name: string
}


