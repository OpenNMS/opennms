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
  compiledMibFiles: MibCompilerFileInfo[]
  pendingMibFiles: MibCompilerFileInfo[]
  files: MibCompilerFileInfo[]
  isLoading: boolean
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
