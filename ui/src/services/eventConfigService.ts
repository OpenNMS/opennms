import {
  mapEventConfigEventsResponseFromServer,
  mapEventConfSourceResponseFromServer,
  mapUploadedEventConfigFilesResponseFromServer
} from '@/mappers/eventConfig.mapper'
import {
  EventConfigEventsResponse,
  EventConfigFilesUploadResponse,
  EventConfigSourcesResponse
} from '@/types/eventConfig'
import { v2 } from './axiosInstances'

/**
 * Makes a POST request to the REST endpoint to upload event configuration files.
 *
 * @param files A list of File objects to upload.
 * @returns A promise that resolves to an object containing the list of event
 * configuration files and any errors encountered during the upload process.
 */
export const uploadEventConfigFiles = async (files: File[]): Promise<EventConfigFilesUploadResponse> => {
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

export const updateEventConfigById = async (id: number, eventLabel:string, description:string, enabled:boolean): Promise<boolean> => {
  const endpoint = `eventconf/sources/events/${id}`
  const payload = {
    eventLabel:eventLabel,
    description:description,
    enabled:enabled
  }
  try {
    const response = await v2.put(endpoint, payload )
    return response.status === 200
  } catch (error) {
    console.error('Error Updating event config source:', error)
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

/**
 * Makes a GET request to the REST endpoint to filter event configuration sources.
 *
 * @param offset The offset of the page of results to return.
 * @param limit The maximum number of results to return in a page.
 * @param totalRecords The total number of records across all pages.
 * @param filter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param sortBy The field to sort the results by.
 * @param order The order in which to sort the results (either "asc" or "desc").
 * @returns A promise that resolves to an `EventConfSourcesResponse` containing the filtered event configuration sources.
 */
export const filterEventConfigSources = async (
  offset: number,
  limit: number,
  filter: string,
  sortBy: string,
  order: string
): Promise<EventConfigSourcesResponse> => {
  const endpoint = '/eventconf/filter/sources'
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
      return mapEventConfSourceResponseFromServer(response.data)
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error filtering event config sources:', error)
    throw error
  }
}

/**
 * Makes a GET request to the REST endpoint to filter event configuration events.
 *
 * @param sourceId The ID of the event configuration source to filter events from.
 * @param offset The offset of the page of results to return.
 * @param limit The maximum number of results to return in a page.
 * @param filter The filter to apply to the results, expressed as a comma-separated list of key-value pairs.
 * @param sortBy The field to sort the results by.
 * @param order The order in which to sort the results (either "asc" or "desc").
 * @returns A promise that resolves to an `EventConfigEventsResponse` containing the filtered event configuration events.
 */
export const filterEventConfigEvents = async (
  sourceId: number,
  offset: number,
  limit: number,
  filter: string,
  sortBy: string,
  order: string
): Promise<EventConfigEventsResponse> => {
  const endpoint = `/eventconf/filter/${sourceId}/events`
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
      return mapEventConfigEventsResponseFromServer(response.data)
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error filtering event config events:', error)
    throw error
  }
}

/**
 * Makes a GET request to the REST endpoint to fetch all source names.
 *
 * @returns A promise that resolves to an array of strings containing all source names.
 */
export const getAllSourceNames = async (): Promise<string[]> => {
  const endpoint = '/eventconf/sources/names'
  try {
    const response = await v2.get(endpoint)
    if (response.status === 200) {
      return response.data as string[]
    } else {
      throw new Error(`Unexpected response status: ${response.status}`)
    }
  } catch (error) {
    console.error('Error fetching all source names:', error)
    throw error
  }
}

