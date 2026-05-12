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
  SnmpCollectionProfile,
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
 * Uploads one or more data collection config files and associates each created
 * source with the given profile names.
 *
 * @param files The files to upload.
 * @param profileNames Profile names that newly-created sources should be added to.
 *                     The server no longer auto-attaches to "default", so an empty
 *                     list means the source is created but not used by any profile.
 */
export const uploadDataCollectionFiles = async (
  files: File[],
  profileNames: string[] = []
): Promise<SnmpDataCollectionSourceUploadResponse> => {
  const formData = new FormData()
  const endpoint = '/datacollectionconf/upload'
  files.forEach((file) => {
    formData.append('upload', file)
  })
  profileNames.forEach((name) => {
    formData.append('profileNames', name)
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
 * Fetches all SNMP collection profiles from the server.
 */
export const getAllSnmpCollectionProfiles = async (): Promise<SnmpCollectionProfile[]> => {
  const endpoint = '/datacollectionconf/profiles'
  try {
    const response = await v2.get<SnmpCollectionProfile[]>(endpoint)
    return Array.isArray(response.data) ? response.data : []
  } catch (error) {
    console.error('Error fetching SNMP collection profiles:', error)
    return []
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
 * @param {string} mibGroupFilter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpCollectionMibGroupResponse>} A promise that resolves to an object containing the filtered SNMP data collection MIB groups.
 */
export const getSnmpDataCollectionMibGroups = async (
  collectionSourceId: number,
  offset: number,
  limit: number,
  mibGroupFilter: string,
  sortBy: string,
  order: string
): Promise<SnmpCollectionMibGroupResponse> => {
  const endpoint = `/datacollectionconf/filter/${collectionSourceId}/mibgroups`
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        mibGroupFilter,
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
 * @param {number} collectionSourceId The ID of the SNMP data collection to filter resource types for.
 * @param {number} offset The offset of the page of results to return.
 * @param {number} limit The maximum number of results to return in a page.
 * @param {string} resourceTypeFilter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param {string} sortBy The field to sort the results by.
 * @param {string} order The order in which to sort the results (either "asc" or "desc").
 * @returns {Promise<SnmpCollectionResourceTypeResponse>} A promise that resolves to an object containing the filtered SNMP data collection resource types.
 */
export const getSnmpDataCollectionResourceTypes = async (
  collectionSourceId: number,
  offset: number,
  limit: number,
  resourceTypeFilter: string,
  sortBy: string,
  order: string
): Promise<SnmpCollectionResourceTypeResponse> => {
  const endpoint = `/datacollectionconf/filter/${collectionSourceId}/resourcetypes`
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
      return false
    }
  } catch (error) {
    console.error('Error creating SNMP data collection system definition:', error)
    return false
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing System Definition in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionSystemDefPayload} payload The payload to send with the request, containing the details of the System Definition to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the System Definition in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
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
      return false
    }
  } catch (error) {
    console.error('Error updating SNMP data collection system definition:', error)
    return false
  }
}

/**
 * Makes a POST request to the REST endpoint to create a new MIB group in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionMibGroupPayload} payload The payload to send with the request, containing the details of the MIB group to create.
 * @param {number} sourceId The ID of the SnmpCollectionSources to create the MIB group in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const createMibGroup = async (payload: SnmpCollectionMibGroupPayload, sourceId: number): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mibgroups`

  try {
    const response = await v2.post(endpoint, payload)

    if (response.status === 201) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error creating SNMP data collection MIB group:', error)
    return false
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing MIB group in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionMibGroupPayload} payload The payload to send with the request, containing the details of the MIB group to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the MIB group in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const updateMibGroup = async (payload: SnmpCollectionMibGroupPayload, sourceId: number): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mibgroups/${payload.id}`

  try {
    const response = await v2.put(endpoint, payload)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error updating SNMP data collection MIB group:', error)
    return false
  }
}

/**
 * Makes a POST request to the REST endpoint to create a new resource type in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionResourceTypePayload} payload The payload to send with the request, containing the details of the resource type to create.
 * @param {number} sourceId The ID of the SnmpCollectionSources to create the resource type in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
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
      return false
    }
  } catch (error) {
    console.error('Error creating SNMP data collection resource type:', error)
    return false
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing resource type in an SnmpCollectionSources by its ID.
 * @param {SnmpCollectionResourceTypePayload} payload The payload to send with the request, containing the details of the resource type to update.
 * @param {number} sourceId The ID of the SnmpCollectionSources to update the resource type in.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
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
      return false
    }
  } catch (error) {
    console.error('Error updating SNMP data collection resource type:', error)
    return false
  }
}

/**
 * Makes a POST request to the REST endpoint to create a new SNMP data collection source.
 * @param {string} name The name of the source to create.
 * @param {string[]} profiles The profile names to associate with the new source.
 * @returns {Promise<number | null>} A promise that resolves to the new source ID on success, or null on failure.
 */
export const createSnmpCollectionSource = async (name: string, profiles: string[]): Promise<number | null> => {
  const endpoint = '/datacollectionconf/collectsources'

  try {
    const response = await v2.post(endpoint, { name, profiles })

    if (response.status === 201) {
      return response.data as number
    }

    return null
  } catch (error) {
    console.error('Error creating SNMP data collection source:', error)
    return null
  }
}

/**
 * Makes a POST request to the REST endpoint to create a new SNMP collection profile.
 * @param {Omit<SnmpCollectionProfile, 'id'>} profile The profile data to create.
 * @returns {Promise<boolean>} A promise that resolves to true if the creation was successful.
 */
export const createSnmpCollectionProfile = async (profile: Omit<SnmpCollectionProfile, 'id' | 'createdTime' | 'lastModified'>): Promise<boolean> => {
  const endpoint = '/datacollectionconf/profiles'

  try {
    const response = await v2.post(endpoint, profile)

    if (response.status === 201) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error creating SNMP collection profile:', error)
    return false
  }
}

/**
 * Makes a PUT request to the REST endpoint to update an existing SNMP collection profile.
 * @param {SnmpCollectionProfile} profile The updated profile to send.
 * @returns {Promise<boolean>} A promise that resolves to true if the update was successful.
 */
export const updateDataCollectionProfile = async (profile: SnmpCollectionProfile): Promise<boolean> => {
  const endpoint = `/datacollectionconf/profiles/${profile.id}`

  try {
    const response = await v2.put(endpoint, profile)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error(`Error updating SNMP collection profile with ID ${profile.id}:`, error)
    return false
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more SNMP data collection sources.
 * @param {number[]} sourceIds The IDs of the SNMP data collection sources to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const deleteSnmpCollectionSources = async (sourceIds: number[]): Promise<boolean> => {
  const endpoint = '/datacollectionconf/collectsources'
  try {
    const params = new URLSearchParams()
    sourceIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.delete(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection sources:', error)
    return false
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more SNMP data collection profiles.
 * @param {number[]} ids The IDs of the profiles to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const deleteSnmpDataCollectionProfiles = async (ids: number[]): Promise<boolean> => {
  const endpoint = '/datacollectionconf/profiles'
  try {
    const response = await v2.delete(endpoint, { data: ids })
    return response.status === 200
  } catch (error) {
    console.error('Error deleting SNMP data collection profiles:', error)
    return false
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more MIB groups for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the MIB groups.
 * @param {number[]} mibGroupIds The IDs of the MIB groups to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const deleteMibGroups = async (sourceId: number, mibGroupIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/mib-groups`
  try {
    const params = new URLSearchParams()
    mibGroupIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.delete(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection MIB groups:', error)
    return false
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more resource types for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the resource types.
 * @param {number[]} resourceTypeIds The IDs of the resource types to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const deleteResourceTypes = async (sourceId: number, resourceTypeIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/resource-types`
  try {
    const params = new URLSearchParams()
    resourceTypeIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.delete(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection resource types:', error)
    return false
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete one or more system definitions for a specific SNMP data collection source.
 * @param {number} sourceId The ID of the SNMP data collection source containing the system definitions.
 * @param {number[]} systemDefIds The IDs of the system definitions to delete.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const deleteSystemDefinitions = async (sourceId: number, systemDefIds: number[]): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${sourceId}/system-defs`
  try {
    const params = new URLSearchParams()
    systemDefIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.delete(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error deleting SNMP data collection system definitions:', error)
    return false
  }
}

/**
 * Makes a GET request to download all Resource types, MIB groups and System defs
 * associated with the specified SNMP data collection source ID.
 *
 * @param {number} collectionSourceId The ID of the SNMP data collection source to download.
 * @param {string} format The format of the download (e.g., 'xml' or 'json').
 * @returns {Promise<Blob | false>} A promise that resolves to a Blob containing the downloaded file content, or false if the request was unsuccessful.
 */
export const downloadSnmpDataCollectionById = async (collectionSourceId: number, format: string): Promise<Blob> => {
  const endpoint = `/datacollectionconf/collectsources/${collectionSourceId}/download`

  try {
    const response = await v2.get(endpoint, {
      params: { format },
      responseType: 'blob'
    })

    if (response.status === 200) {
      return new Blob([response.data], { type: response.headers['content-type'] })
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error(`Error downloading SNMP data collection for source ID ${collectionSourceId}:`, error)
    throw error
  }
}

/**
 * Download the top-level <datacollection-config> assembled from current
 * profile rows. Pair with downloadSnmpDataCollectionById per source for a
 * full round-trippable export.
 * @param {string} format 'xml' or 'json'.
 * @returns {Promise<Blob>} The serialized config as a Blob.
 */
export const downloadDatacollectionConfig = async (format: string): Promise<Blob> => {
  const endpoint = '/datacollectionconf/config/download'
  try {
    const response = await v2.get(endpoint, {
      params: { format },
      responseType: 'blob'
    })
    if (response.status === 200) {
      return new Blob([response.data], { type: response.headers['content-type'] })
    }
    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    console.error('Error downloading datacollection-config:', error)
    throw error
  }
}

/**
 * Makes a PATCH request to the REST endpoint to enable or disable one or more SNMP data collection sources.
 * @param {boolean} enabled Whether to enable (true) or disable (false) the SNMP data collection sources.
 * @param {number[]} sourceIds The IDs of the SNMP data collection sources to enable or disable.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const enableDisableSnmpDataCollectionSources = async (
  enabled: boolean,
  sourceIds: number[]
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/status/${enabled}`
  try {
    const params = new URLSearchParams()
    sourceIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.patch(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error enabling/disabling SNMP data collection sources:', error)
    return false
  }
}

/**
 * Makes a PATCH request to the REST endpoint to enable or disable one or more SNMP MIB groups.
 * @param {number} snmpDataCollectionSourceId The ID of the SNMP data collection source containing the MIB groups.
 * @param {boolean} enabled Whether to enable (true) or disable (false) the MIB groups.
 * @param {number[]} mibGroupIds The IDs of the MIB groups to enable or disable.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const enableDisableSnmpMibGroups = async (
  snmpDataCollectionSourceId: number,
  enabled: boolean,
  mibGroupIds: number[]
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${snmpDataCollectionSourceId}/mib-groups/status/${enabled}`
  try {
    const params = new URLSearchParams()
    mibGroupIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.patch(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error enabling/disabling SNMP MIB groups:', error)
    return false
  }
}

/**
 * Makes a PATCH request to the REST endpoint to enable or disable one or more SNMP resource types.
 * @param {number} snmpDataCollectionSourceId The ID of the SNMP data collection source containing the resource types.
 * @param {boolean} enabled Whether to enable (true) or disable (false) the resource types.
 * @param {number[]} resourceTypeIds The IDs of the resource types to enable or disable.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const enableDisableSnmpResourceTypes = async (
  snmpDataCollectionSourceId: number,
  enabled: boolean,
  resourceTypeIds: number[]
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${snmpDataCollectionSourceId}/resource-types/status/${enabled}`
  try {
    const params = new URLSearchParams()
    resourceTypeIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.patch(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error enabling/disabling SNMP resource types:', error)
    return false
  }
}

/**
 * Makes a PATCH request to the REST endpoint to enable or disable one or more SNMP system definitions.
 * @param {number} snmpDataCollectionSourceId The ID of the SNMP data collection source containing the system definitions.
 * @param {boolean} enabled Whether to enable (true) or disable (false) the system definitions.
 * @param {number[]} systemDefIds The IDs of the system definitions to enable or disable.
 * @returns {Promise<boolean>} A promise that resolves to a boolean indicating whether the request was successful or not.
 */
export const enableDisableSnmpSystemDefs = async (
  snmpDataCollectionSourceId: number,
  enabled: boolean,
  systemDefIds: number[]
): Promise<boolean> => {
  const endpoint = `/datacollectionconf/collectsources/${snmpDataCollectionSourceId}/system-defs/status/${enabled}`
  try {
    const params = new URLSearchParams()
    systemDefIds.forEach((id) => params.append('id', id.toString()))
    const response = await v2.patch(`${endpoint}?${params.toString()}`)

    if (response.status === 200) {
      return true
    } else {
      return false
    }
  } catch (error) {
    console.error('Error enabling/disabling SNMP system definitions:', error)
    return false
  }
}
