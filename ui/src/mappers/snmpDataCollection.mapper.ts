import { CreateEditMode } from '@/types'
import {
  SnmpCollectionMibGroup,
  SnmpCollectionMibGroupPayload,
  SnmpCollectionMibGroupResponse,
  SnmpCollectionResourceType,
  SnmpCollectionResourceTypeResponse,
  SnmpCollectionSource,
  SnmpCollectionSystemDef,
  SnmpCollectionSystemDefPayload,
  SnmpCollectionSystemDefResponse,
  SnmpDataCollectionSourceNamesAndIds,
  SnmpDataCollectionSourceResponse,
  SnmpDataCollectionSourceUploadResponse
} from '@/types/snmpDataCollection'

export const mapUploadedDataCollectionFilesResponseFromServer = (
  response: any
): SnmpDataCollectionSourceUploadResponse => {
  return {
    errors: response.errors.map((err: any) => ({
      file: err.file,
      error: err.error
    })),
    success: response.success.map((success: any) => ({
      file: success.file
    }))
  }
}

export const mapDataCollectionSourceFromServer = (source: any): SnmpCollectionSource => {
  return {
    id: source.id,
    name: source.name,
    vendor: source.vendor,
    description: source.description,
    enabled: source.enabled,
    createdTime: new Date(source.createdTime),
    lastModified: new Date(source.lastModified),
    uploadedBy: source.uploadedBy
  }
}

export const mapSnmpDataCollectionSourceResponseFromServer = (response: any): SnmpDataCollectionSourceResponse => {
  return {
    sources: response.snmpCollectionSourceList.map(mapDataCollectionSourceFromServer),
    totalRecords: response.totalRecords
  }
}

export const mapSnmpDataCollectionSourceNamesAndIdsResponseFromServer = (
  response: any
): SnmpDataCollectionSourceNamesAndIds[] => {
  return response.map((source: any) => ({
    id: source.id,
    name: source.name
  }))
}

export const mapSnmpCollectionSystemDefFromServer = (defs: any): SnmpCollectionSystemDef => {
  return {
    id: defs.id,
    name: defs.name,
    sysoid: defs.sysoid,
    sysoidMask: defs.sysoidMask,
    ipAddresses: defs.ipAddresses,
    ipAddressMasks: defs.ipAddressMasks,
    mibGroupNames: JSON.parse(defs.mibGroupNames),
    enabled: defs.enabled,
    collectionSourceId: defs.collectionSourceId,
    collectionSourceName: defs.collectionSourceName
  }
}

export const mapSnmpCollectionSystemDefResponseFromServer = (defs: any): SnmpCollectionSystemDefResponse => {
  return {
    systemDefinitions: defs.dataCollectionSystemDefsList.map(mapSnmpCollectionSystemDefFromServer),
    totalRecords: defs.totalRecords
  }
}

export const mapSnmpCollectionMibGroupFromServer = (group: any): SnmpCollectionMibGroup => {
  return {
    id: group.id,
    name: group.name,
    ifType: group.ifType,
    mibGroupNames: JSON.parse(group.mibGroupNames),
    mibObjects: group.mibObjects,
    mibObjProperties: group.mibObjProperties,
    enabled: group.enabled,
    collectionSourceId: group.collectionSourceId,
    collectionSourceName: group.collectionSourceName
  }
}

export const mapSnmpCollectionMibGroupResponseFromServer = (groups: any): SnmpCollectionMibGroupResponse => {
  return {
    mibGroups: groups.dataCollectionMibGroupList.map(mapSnmpCollectionMibGroupFromServer),
    totalRecords: groups.totalRecords
  }
}

export const mapSnmpCollectionResourceTypeFromServer = (resourceType: any): SnmpCollectionResourceType => {
  return {
    id: resourceType.id,
    name: resourceType.name,
    label: resourceType.label,
    resourceLabel: resourceType.resourceLabel,
    persistenceSelectorStrategy: resourceType.persistenceSelectorStrategy,
    persistenceSelectorParams: resourceType.persistenceSelectorParams,
    storageStrategy: resourceType.storageStrategy,
    storageStrategyParams: resourceType.storageStrategyParams,
    enabled: resourceType.enabled,
    collectionSourceId: resourceType.collectionSourceId,
    collectionSourceName: resourceType.collectionSourceName
  }
}

export const mapSnmpCollectionResourceTypeResponseFromServer = (
  resourceTypes: any
): SnmpCollectionResourceTypeResponse => {
  return {
    resourceTypes: resourceTypes.dataCollectionResourceTypeList.map(mapSnmpCollectionResourceTypeFromServer),
    totalRecords: resourceTypes.totalRecords
  }
}

export const mapSnmpDataCollectionSystemDefPayloadToServer = (
  name: string,
  sysoid: string,
  sysoidMask: string,
  ipAddresses: string,
  ipAddressMasks: string,
  mibGroupNames: string[],
  enabled: boolean,
  selectedSystemDefId: number,
  isEditMode: CreateEditMode
): SnmpCollectionSystemDefPayload => {
  const payload = {
    name: name,
    sysoid: sysoid,
    sysoidMask: sysoidMask,
    ipAddresses: ipAddresses,
    ipAddressMasks: ipAddressMasks,
    mibGroupNames: JSON.stringify(mibGroupNames),
    enabled: enabled
  } as SnmpCollectionSystemDefPayload

  if (isEditMode === CreateEditMode.Edit) {
    payload.id = selectedSystemDefId
  }

  return payload
}

export const mapSnmpDataCollectionMibGroupPayloadToServer = (
  name: string,
  ifType: string,
  mibGroupNames: string[],
  mibObjects: any[],
  enabled: boolean,
  selectedMibGroupId: number,
  isEditMode: CreateEditMode
): SnmpCollectionMibGroupPayload => {
  const names = isEditMode === CreateEditMode.Edit ? JSON.stringify(mibGroupNames) : JSON.stringify([name])
  const payload = {
    name: name,
    ifType: ifType,
    mibGroupNames: names,
    mibObjects: JSON.stringify(mibObjects),
    enabled: enabled,
    mibObjProperties: ''
  } as SnmpCollectionMibGroupPayload

  if (isEditMode === CreateEditMode.Edit) {
    payload.id = selectedMibGroupId
  }

  return payload
}

