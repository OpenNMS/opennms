export type MibDirectory = 'pending' | 'compiled'

export interface MibFileInfo {
  name: string
  size: number
  lastModified: number
}

export interface MibFilesResponse {
  pending: MibFileInfo[]
  compiled: MibFileInfo[]
}

export interface MibUploadFileResult {
  file: string
  error?: string
}

export interface MibUploadResponse {
  success: MibUploadFileResult[]
  errors: MibUploadFileResult[]
}

export interface MibParseResult {
  success: boolean
  mibName?: string
  errors?: string
  missingDependencies?: string[]
}

export interface MibCompileResult extends MibParseResult {
  targetFile?: string
}

export interface MibCompileConflict {
  mibName: string
  targetFile: string
  error?: string
}

export interface MibEventsPreview extends MibParseResult {
  ueiBase?: string
  eventCount?: number
  suggestedFileName?: string
  eventsXml?: string
}

export interface MibDataCollectionPreview extends MibParseResult {
  groupCount?: number
  suggestedFileName?: string
  dataCollectionXml?: string
}

export interface MibGraphTemplatesResult extends MibParseResult {
  graphCount?: number
  fileName?: string
  content?: string
  written?: boolean
}

export type UploadMibFileType = {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export interface MibCompilerStoreState {
  pendingFiles: MibFileInfo[]
  compiledFiles: MibFileInfo[]
  isLoading: boolean
}
