import { CreateEditMode, Pagination } from '.'

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
  isLoading: boolean
  selectedCollectionSource: SnmpCollectionSource | null
  systemDefinitions: SnmpCollectionSystemDef[]
  systemDefsPagination: Pagination
  systemDefsSorting: Sorting
  systemDefsSearchTerm: string
  mibGroups: SnmpCollectionMibGroup[]
  mibGroupsPagination: Pagination
  mibGroupsSorting: Sorting
  mibGroupsSearchTerm: string
  resourceTypes: SnmpCollectionResourceType[]
  resourceTypesPagination: Pagination
  resourceTypesSorting: Sorting
  resourceTypesSearchTerm: string
  resourceTypeNames: string[]
  selectedSystemDef: SnmpCollectionSystemDef | null
  systemDefDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
  }
}

export interface SnmpDataCollectionSourceResponse {
  sources: SnmpCollectionSource[]
  totalRecords: number
}

export interface SnmpDataCollectionSourceNamesAndIds {
  id: number
  name: string
}

export interface SnmpCollectionSystemDef {
  id: number
  name: string
  sysoid: string
  sysoidMask: string
  ipAddresses: string
  ipAddressMasks: string
  mibGroupNames: string
  enabled: boolean
  collectionSourceId: number
  collectionSourceName: string
}

export interface SnmpCollectionSystemDefResponse {
  systemDefinitions: SnmpCollectionSystemDef[]
  totalRecords: number
}

export interface SnmpCollectionMibGroup {
  id: number
  name: string
  ifType: string
  mibGroupNames: string
  mibObjects: string
  mibObjProperties: string
  enabled: boolean
  collectionSourceId: number
  collectionSourceName: string
}

export interface SnmpCollectionMibGroupResponse {
  mibGroups: SnmpCollectionMibGroup[]
  totalRecords: number
}

export interface SnmpCollectionResourceType {
  id: number
  name: string
  label: string
  resourceLabel: string
  persistenceSelectorStrategy: string
  persistenceSelectorParams: string
  storageStrategy: string
  storageStrategyParams: string
  enabled: boolean
  collectionSourceId: number
  collectionSourceName: string
}

export interface SnmpCollectionResourceTypeResponse {
  resourceTypes: SnmpCollectionResourceType[]
  totalRecords: number
}

