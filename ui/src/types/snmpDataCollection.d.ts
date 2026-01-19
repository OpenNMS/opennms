export type SnmpDataCollectionStoreState = {
  sources: SnmpCollectionSource[]
  selectedSource: SnmpCollectionSource | null
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
}

export type SnmpCollectionSource = {
  id: number
  name: string
  vendor: string
  description: string
  enabled: boolean
  createdTime: Date
  lastModified: Date
  uploadedBy: string
}

export type SnmpDataCollectionSourceUploadResponse = {
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

export type UploadSnmpDataCollectionFileType = {
  file: File
  isValid: boolean
  errors: string[]
  isDuplicate: boolean
}

export type SnmpCollectionDetailState = {
  selectedCollectionSource: SnmpCollectionSource | null
}

