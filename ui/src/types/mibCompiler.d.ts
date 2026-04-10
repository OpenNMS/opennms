import { SORT } from '@featherds/table'

export type MibCompilerFileLocation = 'PENDING' | 'COMPILED'

export interface MibCompilerFileInfo {
  fileName: string
  location: MibCompilerFileLocation
}

export type MibFileListResponse = MibCompilerFileInfo[]

export interface MibCompilerGenerateEventsRequest {
  name: string
  ueiBase?: string
}

export interface MibCompilerStoreState {
  files: MibCompilerFileInfo[]
  isLoading: boolean
  compiledMibFilesSearchTerm: string
  pendingMibFilesSearchTerm: string
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

