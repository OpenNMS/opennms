import { CreateEditMode } from '@/types'
import {
  PersistSelectorStrategyForm,
  SnmpCollectionMibGroup,
  SnmpCollectionMibGroupPayload,
  SnmpCollectionMibGroupResponse,
  SnmpCollectionResourceType,
  SnmpCollectionResourceTypePayload,
  SnmpCollectionResourceTypeResponse,
  SnmpCollectionSource,
  SnmpCollectionSystemDef,
  SnmpCollectionSystemDefPayload,
  SnmpCollectionSystemDefResponse,
  SnmpDataCollectionSourceNamesAndIds,
  SnmpDataCollectionSourceResponse,
  SnmpDataCollectionSourceUploadResponse,
  StorageStrategyForm
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
  ipAddresses: string[],
  ipAddressMasks: string[],
  mibGroupNames: string[],
  enabled: boolean,
  selectedSystemDefId: number,
  isEditMode: CreateEditMode
): SnmpCollectionSystemDefPayload => {
  // ipAddresses / ipAddressMasks are now structured arrays on the wire — the
  // server normalises them into the canonical IpList JSON internally so every
  // write path produces the same on-disk shape the runtime loader expects.
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
  // New MIB groups have no nested include-groups; writing [name] would make the
  // group self-include via mib_group_names → Group.includeGroups, causing infinite
  // recursion in processGroupName once a SystemDef references it.
  const names = isEditMode === CreateEditMode.Edit ? JSON.stringify(mibGroupNames) : JSON.stringify([])
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

export const mapSnmpDataCollectionResourceTypePayloadToServer = (
  name: string,
  label: string,
  resourceLabel: string,
  persistenceSelectorStrategy: string,
  persistenceSelectorParams: PersistSelectorStrategyForm[],
  storageStrategy: string,
  storageStrategyParams: StorageStrategyForm[],
  enabled: boolean,
  selectedResourceTypeId: number,
  isEditMode: CreateEditMode
): SnmpCollectionResourceTypePayload => {
  const payload = {
    name: name,
    label: label,
    resourceLabel: resourceLabel,
    persistenceSelectorStrategy: persistenceSelectorStrategy,
    persistenceSelectorParams: JSON.stringify(persistenceSelectorParams),
    storageStrategy: storageStrategy,
    storageStrategyParams: JSON.stringify(storageStrategyParams),
    enabled: enabled
  } as SnmpCollectionResourceTypePayload

  if (isEditMode === CreateEditMode.Edit) {
    payload.id = selectedResourceTypeId
  }

  return payload
}
