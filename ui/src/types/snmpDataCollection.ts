import { IAutocompleteItemType } from '@featherds/autocomplete';
import { CreateEditMode, Pagination, Sorting } from '.'
import type { RRA } from './timeSeries'

export type EditableRRA = RRA & { _id: number }

export interface ProfileFormErrors {
  name?: string
  maxVarsPerPdu?: string
  rrdStep?: string
  storageFlag?: string
  rrdRras?: string
}

export interface ConfigDetailsModel {
  name: string
  enabled: boolean
  maxVarsPerPdu: string
  storageFlag: string
}

export interface RrdSettingsModel {
  rrdStep: string
  rras: EditableRRA[]
}

export interface SnmpDataCollectionStoreState {
  sources: SnmpCollectionSource[]
  selectedSource: SnmpCollectionSource | null
  sourcesPagination: Pagination
  sourcesSearchTerm: string
  sourcesSorting: Sorting
  profiles: SnmpCollectionProfile[]
  selectedProfile: SnmpCollectionProfile | null
  profilesPagination: Pagination
  profilesSearchTerm: string
  profilesSorting: Sorting
  isLoading: boolean
  uploadedSourceNames: SnmpDataCollectionSourceNamesAndIds[]
  activeTab: number
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

export enum SnmpProfileStorageFlagType {
  SELECT = 'select',
  PRIMARY = 'primary',
  ALL = 'all'
}

export interface SnmpCollectionProfile {
  id: number
  name: string
  rrdStep: number
  rrdRras: string[]
  storageFlag: string // SnmpProfileStorageFlagType
  sourceNames: string[]
  maxVarsPerPdu?: number
  enabled: boolean
  createdTime?: number
  lastModified?: number
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
  // Root element kind. 'group' for <datacollection-group> source files,
  // 'config' for <datacollection-config> profile-driver files.
  kind?: 'group' | 'config'
  // Group name parsed from <datacollection-group name="...">; used for
  // duplicate detection because the DB stores sources by group name, not
  // by filename basename. Only set when kind === 'group'.
  groupName?: string
  // <snmp-collection> names parsed from a <datacollection-config>. Only set
  // when kind === 'config'.
  profileNames?: string[]
}

export interface SnmpCollectionDetailStoreState {
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
  mibGroupNames: string[]
  selectedSystemDef: SnmpCollectionSystemDef | null
  selectedMibGroup: SnmpCollectionMibGroup | null
  selectedResourceType: SnmpCollectionResourceType | null
  activeTab: number
  systemDefDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
  }
  resourceTypeDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
  }
  mibGroupDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
  }
}

export interface SnmpDataCollectionCreationStoreState {
  // selectedCollectionSource: IAutocompleteItemType
  uploadedSourceNames: SnmpDataCollectionSourceNamesAndIds[]
  resourceTypeNames: string[]
  mibGroupNames: string[]
  isLoading: boolean
  configForm: {
    systemDef: SnmpCollectionSystemDefPayload[]
    mibGroup: SnmpCollectionMibGroupPayload[]
    resourceType: SnmpCollectionResourceTypePayload[]
  }
  systemDefDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode,
    systemDefIndex: number
  }
  resourceTypeDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
    resourceTypeIndex: number
  }
  mibGroupDrawerState: {
    visible: boolean
    isEditMode: CreateEditMode
    mibGroupIndex: number
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
  ipAddresses: string[]
  ipAddressMasks: string[]
  mibGroupNames: string[]
  enabled: boolean
  collectionSourceId: number
  collectionSourceName: string
}

export interface SnmpCollectionSystemDefPayload {
  id: number
  name: string
  sysoid: string
  sysoidMask: string
  ipAddresses: string[]
  ipAddressMasks: string[]
  mibGroupNames: string
  enabled: boolean
}

export interface SnmpCollectionSystemDefResponse {
  systemDefinitions: SnmpCollectionSystemDef[]
  totalRecords: number
}

export interface SnmpCollectionMibGroup {
  id: number
  name: string
  ifType: string
  mibGroupNames: string[]
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

export interface SystemDefErrors {
  name?: string
  oidType?: string
  oidValue?: string
  status?: string
  ipAddresses?: string
  ipAddressMasks?: string
  mibGroupNames?: string
}

export interface SnmpCollectionMibGroupPayload {
  id: number
  name: string
  ifType: string
  mibGroupNames: string
  mibObjects: string
  mibObjProperties: string
  enabled: boolean
}

export interface MibGroupObjectForm {
  oid: string
  alias: string
  instance: string
  maxval: string | null
  minval: string | null
  type: string
}

export interface PersistSelectorStrategyForm {
  key: string
  value: string
}

export interface StorageStrategyForm {
  key: string
  value: string
}

export interface MibGroupObjectFormErrors {
  oid?: string
  alias?: string
  instance?: string
  maxval?: string
  minval?: string
  type?: string
}

export interface MibGroupErrors {
  name?: string
  ifType?: string
  status?: string
  mibGroupNames?: string
  mibObjects?: string
  mibObjProperties?: string
}

export interface ResourceTypeErrors {
  name?: string
  label?: string
  status?: string
  resourceLabel?: string
  persistenceSelectorStrategy?: string
  storageStrategy?: string
}

export interface SnmpCollectionResourceTypePayload {
  id: number
  name: string
  label: string
  resourceLabel: string
  persistenceSelectorStrategy: string
  persistenceSelectorParams: string
  storageStrategy: string
  storageStrategyParams: string
  enabled: boolean
}
