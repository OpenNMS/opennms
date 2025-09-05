import { mapEventConfSourceResponseFromServer, mapUploadedEventConfigFilesResponseFromServer } from '@/mappers/eventConfig.mapper'
import { EventConfigFilesUploadReponse, EventConfSourcesResponse } from '@/types/eventConfig'
import { v2 } from './axiosInstances'

/**
 * Makes a POST request to the REST endpoint to upload event configuration files.
 *
 * @param files A list of File objects to upload.
 * @returns A promise that resolves to an object containing the list of event
 * configuration files and any errors encountered during the upload process.
 */
export const uploadEventConfigFiles = async (files: File[]): Promise<EventConfigFilesUploadReponse> => {
  const formData = new FormData()
  const endpoint = '/eventconf/upload'
  files.forEach((file) => {
    formData.append('upload', file)
  })

  try {
    const response = await v2.post(endpoint, formData)
    if (response.status !== 200) {
      throw new Error(`Failed to upload files: ${response.statusText}`)
    }
    return mapUploadedEventConfigFilesResponseFromServer(response.data)
  } catch (error) {
    console.error('Error uploading event config files:', error)
    throw error
  }
}

/**
 * Makes a DELETE request to the REST endpoint to delete an event configuration source.
 *
 * @param id The ID of the event configuration source to delete.
 * @returns A promise that resolves to a boolean indicating whether the source was deleted successfully.
 */
export const deleteEventConfigSourceById = async (id: number): Promise<boolean> => {
  const endpoint = '/eventconf/sources'
  const payload = {
    sourceIds: [id]
  }
  try {
    const response = await v2.delete(endpoint, { data: payload })
    return response.status === 200
  } catch (error) {
    console.error('Error deleting event config source:', error)
    return false
  }
}

/**
 * Makes a PATCH request to the REST endpoint to change the status of an event configuration event.
 *
 * @param eventId The ID of the event configuration event to change the status of.
 * @param sourceId The ID of the event configuration source that the event configuration event belongs to.
 * @param enable Whether to enable or disable the event configuration event.
 * @returns A promise that resolves to a boolean indicating whether the event configuration event status was changed successfully.
 */
export const changeEventConfigEventStatus = async (
  eventId: number,
  sourceId: number,
  enable: boolean
): Promise<boolean> => {
  const endpoint = `/eventconf/sources/${sourceId}/events/status`
  const payload = {
    enable,
    eventsIds: [eventId]
  }
  try {
    const response = await v2.patch(endpoint, payload)
    return response.status === 200
  } catch (error) {
    console.error('Error changing event config event status:', error)
    return false
  }
}

/**
 * Makes a PATCH request to the REST endpoint to change the status of an event configuration source.
 *
 * @param sourceId The ID of the event configuration source to change the status of.
 * @param enabled Whether to enable or disable the event configuration source.
 * @returns A promise that resolves to a boolean indicating whether the event configuration source status was changed successfully.
 */
export const changeEventConfigSourceStatus = async (sourceId: number, enabled: boolean): Promise<boolean> => {
  const endpoint = '/eventconf/sources/status'
  const payload = {
    enabled,
    cascadeToEvents: true,
    sourceIds: [sourceId]
  }
  try {
    const response = await v2.patch(endpoint, payload)
    return response.status === 200
  } catch (error) {
    console.error('Error changing event config source status:', error)
    return false
  }
}

// (params: {
//   name?: string
//   vendor?: string
//   desc?: string
//   fileOrder?: number
//   eventCount?: number
//   totalRecords?: number
//   offset?: number
//   limit?: number
// })

/**
 * Makes a GET request to the REST endpoint to filter event configuration sources.
 *
 * @param offset The offset of the first record to return.
 * @param limit The maximum number of records to return.
 * @param totalRecords The total number of records in the result set.
 * @param searchTerm The search term to filter by.
 * @returns A promise that resolves to the filtered event configuration sources.
 */
export const filterEventConfigSources = async (offset: number, limit: number, totalRecords: number, filter: string, sortBy: string, order: string): Promise<EventConfSourcesResponse> => {
  const endpoint = '/eventconf/filter/sources'
  try {
    const response = await v2.get(endpoint, {
      params: {
        offset,
        limit,
        totalRecords,
        filter,
        sortBy,
        order
      }
    })
    if (response.status === 200) {
      return mapEventConfSourceResponseFromServer(response.data)
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error filtering event config sources:', error)
    throw error
  }
}

