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
  SnmpCollectionMibGroupPayload,
  SnmpCollectionMibGroupResponse,
  SnmpCollectionResourceTypePayload,
  SnmpCollectionResourceTypeResponse,
  SnmpCollectionSource,
  SnmpCollectionSystemDefPayload,
  SnmpCollectionSystemDefResponse,
  SnmpDataCollectionSourceNamesAndIds,
  SnmpDataCollectionSourceResponse,
  SnmpDataCollectionSourceUploadResponse
} from '@/types/snmpDataCollection'
import { v2 } from './axiosInstances'

/**
 * Uploads one or more data collection config files.
 * @param {File[]} files The files to upload.
 * @returns {Promise<SnmpDataCollectionSourceUploadResponse>} A promise that resolves to an object containing the list of data collection config files and any errors encountered during the upload process.
 */
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

/**
 * Makes a GET request to the REST endpoint to filter SNMP data collection sources.
 *
 * @param {number} offset The offset of the page of results to return.
 * @param {number} limit The maximum number of results to return in a page.
 * @param {string} filter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpDataCollectionSourceResponse>} A promise that resolves to an object containing the filtered SNMP data collection sources.
 */
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

/**
 * Makes a GET request to the REST endpoint to retrieve a list of all SNMP data collection source names and IDs.
 *
 * @returns {Promise<SnmpDataCollectionSourceNamesAndIds[]>} A promise that resolves to an array of objects containing the names and IDs of all SNMP data collection sources.
 */
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

/**
 * Makes a GET request to the REST endpoint to retrieve an SNMP data collection source by its ID.
 * @param {number} id The ID of the SNMP data collection source to retrieve.
 * @returns {Promise<SnmpCollectionSource>} A promise that resolves to an object representing the requested SNMP data collection source.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
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

/**
 * Makes a GET request to the REST endpoint to filter SNMP data collection system definitions.
 * @param {number} collectionSourceId The ID of the SNMP data collection source to filter system definitions for.
 * @param {number} offset The offset of the page of results to return.
 * @param {number} limit The maximum number of results to return in a page.
 * @param {string} systemDefsFilter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpCollectionSystemDefResponse>} A promise that resolves to an object containing the filtered SNMP data collection system definitions.
 */
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

/**
 * Makes a GET request to the REST endpoint to filter SNMP data collection MIB groups.
 *
 * @param {number} collectionSourceId The ID of the collection source to filter MIB groups for.
 * @param {number} offset The offset of the page of results to return.
 * @param {number} limit The maximum number of results to return in a page.
 * @param {string} mibGroupsFilter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpCollectionMibGroupResponse>} A promise that resolves to an object containing the filtered SNMP data collection MIB groups.
 */
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

/**
 * Makes a GET request to the REST endpoint to filter SNMP data collection resource types.
 *
 * @param {number} dataCollectionGroupId The ID of the SNMP data collection to filter resource types for.
 * @param {number} offset The offset of the page of results to return.
 * @param {number} limit The maximum number of results to return in a page.
 * @param {string} resourceTypeFilter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpCollectionResourceTypeResponse>} A promise that resolves to an object containing the filtered SNMP data collection resource types.
 */
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

/**
 * Makes a GET request to the REST endpoint to retrieve a list of all SNMP data collection resource type names.
 *
 * @returns {Promise<string[]>} A promise that resolves to an array of strings containing the names of all SNMP data collection resource types.
 */
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

/**
 * Makes a GET request to the REST endpoint to retrieve a list of all SNMP data collection MIB group names.
 *
 * @returns {Promise<string[]>} A promise that resolves to an array of strings containing the names of all SNMP data collection MIB groups.
 */
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

/**
 * Makes a POST request to the REST endpoint to create a new System Definition in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionSystemDefPayload} payload The payload to send with the request, containing the details of the System Definition to create.
 * @param {number} sourceId The ID of the SnmpCollectionSources to create the System Definition in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const createSystemDefinition = async (
  payload: SnmpCollectionSystemDefPayload,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/systemdefs`

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

/**
 * Makes a PUT request to the REST endpoint to update an existing System Definition in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionSystemDefPayload} payload The payload to send with the request, containing the details of the System Definition to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the System Definition in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const updateSystemDefinition = async (
  payload: SnmpCollectionSystemDefPayload,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/systemdefs/${payload.id}`

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

/**
 * Makes a POST request to the REST endpoint to create a new MIB group in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionMibGroupPayload} payload The payload to send with the request, containing the details of the MIB group to create.
 * @param {number} sourceId The ID of the SnmpCollectionSources to create the MIB group in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const createMibGroup = async (payload: SnmpCollectionMibGroupPayload, sourceId: number): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mibgroups`

  try {
    const response = await v2.post(endpoint, payload)

    if (response.status === 201) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error creating SNMP data collection MIB group:', error)
    throw error
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing MIB group in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionMibGroupPayload} payload The payload to send with the request, containing the details of the MIB group to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the MIB group in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const updateMibGroup = async (payload: SnmpCollectionMibGroupPayload, sourceId: number): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mibgroups/${payload.id}`

  try {
    const response = await v2.put(endpoint, payload)

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error updating SNMP data collection MIB group:', error)
    throw error
  }
}

/**
 * Makes a POST request to the REST endpoint to create a new resource type in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionResourceTypePayload} payload The payload to send with the request, containing the details of the resource type to create.
 * @param {number} sourceId The ID of the SnmpCollectionSources to create the resource type in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const createResourceType = async (
  payload: SnmpCollectionResourceTypePayload,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/resourcetypes`

  try {
    const response = await v2.post(endpoint, payload)

    if (response.status === 201) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error creating SNMP data collection resource type:', error)
    throw error
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing resource type in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionResourceTypePayload} payload The payload to send with the request, containing the details of the resource type to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the resource type in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const updateResourceType = async (
  payload: SnmpCollectionResourceTypePayload,
  sourceId: number
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/resourcetypes/${payload.id}`

  try {
    const response = await v2.put(endpoint, payload)

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error updating SNMP data collection resource type:', error)
    throw error
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more SNMP data collection sources.
 * @param {number[]} sourceIds The IDs of the SNMP data collection sources to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const deleteSnmpCollectionSources = async (sourceIds: number[]): Promise<boolean> => {
  const endpoint = '/datacollectionconf/collectsources'
  try {
    const response = await v2.delete(endpoint, { data: { ids: sourceIds } })

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection sources:', error)
    throw error
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more MIB groups for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the MIB groups.
 * @param {number[]} mibGroupIds The IDs of the MIB groups to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const deleteMibGroups = async (sourceId: number, mibGroupIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mib-groups`
  try {
    const response = await v2.delete(endpoint, { data: { ids: mibGroupIds } })

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection MIB groups:', error)
    throw error
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more resource types for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the resource types.
 * @param {number[]} resourceTypeIds The IDs of the resource types to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const deleteResourceTypes = async (sourceId: number, resourceTypeIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/resource-types`
  try {
    const response = await v2.delete(endpoint, { data: { ids: resourceTypeIds } })

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection resource types:', error)
    throw error
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more system definitions for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the system definitions.
 * @param {number[]} systemDefIds The IDs of the system definitions to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 * @throws {Error} If the request was unsuccessful, an error is thrown with a message indicating the reason for the failure.
 */
export const deleteSystemDefinitions = async (sourceId: number, systemDefIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/system-defs`
  try {
    const response = await v2.delete(endpoint, { data: { ids: systemDefIds } })

    if (response.status === 200) {
      return true
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection system definitions:', error)
    throw error
  }
}

