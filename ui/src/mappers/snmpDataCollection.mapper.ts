import {
  SnmpCollectionSource,
  SnmpCollectionSystemDef,
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
    sources: response.snmpCollectionSourceList.map((source: any) => mapDataCollectionSourceFromServer(source)),
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
    mibGroupNames: defs.mibGroupNames,
    enabled: defs.enabled,
    collectionSourceId: defs.collectionSourceId,
    collectionSourceName: defs.collectionSourceName
  }
}

export const mapSnmpCollectionSystemDefResponseFromServer = (defs: any): SnmpCollectionSystemDefResponse => {
  return {
    systemDefinitions: defs.dataCollectionSystemDefsList.map((def: any) => mapSnmpCollectionSystemDefFromServer(def)),
    totalRecords: defs.totalRecords
  }
}
