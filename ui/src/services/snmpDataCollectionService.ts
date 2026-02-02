import {
  mapDataCollectionSourceFromServer,
  mapSnmpCollectionMibGroupResponseFromServer,
  mapSnmpCollectionResourceTypeResponseFromServer,
  mapSnmpCollectionSystemDefResponseFromServer,
  mapSnmpDataCollectionSourceNamesAndIdsResponseFromServer,
  mapSnmpDataCollectionSourceResponseFromServer,
  mapUploadedDataCollectionFilesResponseFromServer
} from '@/mappers/snmpDataCollection.mapper'
import {
  SnmpCollectionMibGroupResponse,
  SnmpCollectionResourceTypeResponse,
  SnmpCollectionSource,
  SnmpCollectionSystemDef,
  SnmpCollectionSystemDefResponse,
  SnmpDataCollectionSourceNamesAndIds,
  SnmpDataCollectionSourceResponse,
  SnmpDataCollectionSourceUploadResponse
} from '@/types/snmpDataCollection'
import { v2 } from './axiosInstances'

export const uploadDataCollectionFiles = async (files: File[]): Promise<SnmpDataCollectionSourceUploadResponse> => {
  const formData = new FormData()
  const endpoint = '/datacollectionconf/upload'
  files.forEach((file) => {
    formData.append('upload', file)
  })

  try {
    const response = await v2.post(endpoint, formData)
    if (response.status !== 200) {
      throw new Error(`Failed to upload files: ${response.statusText}`)
    }
    return mapUploadedDataCollectionFilesResponseFromServer(response.data)
  } catch (error) {
    console.error('Error uploading SNMP data collection files:', error)
    throw error
  }
}

export const filterSnmpCollectionSources = async (
  offset: number,
  limit: number,
  filter: string,
  sortBy: string,
  order: string
): Promise<SnmpDataCollectionSourceResponse> => {
  const endpoint = '/datacollectionconf/filter/collectsources'
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        filter,
        sortBy,
        order
      }
    })

    if (response.status === 200) {
      return mapSnmpDataCollectionSourceResponseFromServer(response.data)
    } else if (response.status === 204) {
      return { sources: [], totalRecords: 0 }
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection sources:', error)
    throw error
  }
}

export const getAllSnmpCollectionSourcesNamesAndIds = async (): Promise<SnmpDataCollectionSourceNamesAndIds[]> => {
  const endpoint = '/datacollectionconf/collectsources/names-and-ids'

  try {
    const response = await v2.get(endpoint)

    if (response.status === 200) {
      return mapSnmpDataCollectionSourceNamesAndIdsResponseFromServer(response.data)
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection source names and IDs:', error)
    throw error
  }
}

export const getSnmpDataCollectionSourceById = async (id: number): Promise<SnmpCollectionSource> => {
  const endpoint = `/datacollectionconf/collectsources/${id}`

  try {
    const response = await v2.get(endpoint)

    if (response.status === 200) {
      return mapDataCollectionSourceFromServer(response.data)
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error(`Error fetching SNMP data collection source with ID ${id}:`, error)
    throw error
  }
}

export const getSnmpDataCollectionSystemDefinitions = async (
  collectionSourceId: number,
  offset: number,
  limit: number,
  systemDefsFilter: string,
  sortBy: string,
  order: string
): Promise<SnmpCollectionSystemDefResponse> => {
  const endpoint = `/datacollectionconf/filter/${collectionSourceId}/systemdefs`
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        systemDefsFilter,
        sortBy,
        order
      }
    })

    if (response.status === 200) {
      return mapSnmpCollectionSystemDefResponseFromServer(response.data)
    } else if (response.status === 204) {
      return { systemDefinitions: [], totalRecords: 0 }
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection system definitions:', error)
    throw error
  }
}

export const getSnmpDataCollectionMibGroups = async (
  collectionSourceId: number,
  offset: number,
  limit: number,
  mibGroupsFilter: string,
  sortBy: string,
  order: string
): Promise<SnmpCollectionMibGroupResponse> => {
  const endpoint = `/datacollectionconf/filter/${collectionSourceId}/mibgroups`
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        mibGroupsFilter,
        sortBy,
        order
      }
    })

    if (response.status === 200) {
      return mapSnmpCollectionMibGroupResponseFromServer(response.data)
    } else if (response.status === 204) {
      return { mibGroups: [], totalRecords: 0 }
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection MIB groups:', error)
    throw error
  }
}

export const getSnmpDataCollectionResourceTypes = async (
  dataCollectionGroupId: number,
  offset: number,
  limit: number,
  resourceTypeFilter: string,
  sortBy: string,
  order: string
): Promise<SnmpCollectionResourceTypeResponse> => {
  const endpoint = `/datacollectionconf/filter/${dataCollectionGroupId}/resourcetypes`
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        resourceTypeFilter,
        sortBy,
        order
      }
    })

    if (response.status === 200) {
      return mapSnmpCollectionResourceTypeResponseFromServer(response.data)
    } else if (response.status === 204) {
      return { resourceTypes: [], totalRecords: 0 }
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection resource types:', error)
    throw error
  }
}

export const getAllResourceTypeNames = async (): Promise<string[]> => {
  const endpoint = '/datacollectionconf/resourcetypes/names'

  try {
    const response = await v2.get(endpoint)

    if (response.status === 200) {
      return response.data as string[]
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection resource type names:', error)
    throw error
  }
}

export const getAllMibGroupNames = async (): Promise<string[]> => {
  const endpoint = '/datacollectionconf/mibgroups/names'

  try {
    const response = await v2.get(endpoint)

    if (response.status === 200) {
      return response.data as string[]
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching SNMP data collection MIB group names:', error)
    throw error
  }
}

export const createSystemDefinition = async (
  payload: SnmpCollectionSystemDef,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/collectsources/${sourceId}/systemdefs`

  try {
    const response = await v2.post(endpoint, payload)

    if (response.status === 201) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error creating SNMP data collection system definition:', error)
    throw error
  }
}

export const updateSystemDefinition = async (
  payload: SnmpCollectionSystemDef,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/collectsources/${sourceId}/systemdefs/${payload.id}`

  try {
    const response = await v2.put(endpoint, payload)

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error updating SNMP data collection system definition:', error)
    throw error
  }
}

