export interface SnmpDataCollectionStoreState {
  sources: SnmpCollectionSource[]
  selectedSource: SnmpCollectionSource | null
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
  isLoading: boolean
  uploadedSourceNames: SnmpDataCollectionSourceNamesAndIds[]
}

export interface SnmpCollectionSource {
  id: number
  name: string
  vendor: string
  description: string
  enabled: boolean
  createdTime: Date
  lastModified: Date
  uploadedBy: string
}

export interface SnmpDataCollectionSourceUploadResponse {
  errors: [
    {
      file: string
      error: string
    }
  ]
  success: [
    {
      file: string
    }
  ]
}

export interface UploadSnmpDataCollectionFileType {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export interface SnmpCollectionDetailState {
  selectedCollectionSource: SnmpCollectionSource | null
}

export interface SnmpDataCollectionSourceResponse {
  sources: SnmpCollectionSource[]
  totalRecords: number
}

export interface SnmpDataCollectionSourceNamesAndIds {
  id: number
  name: string
}

