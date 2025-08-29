import { mapUploadedEventConfigFilesResponseFromServer } from '@/mappers/eventConfig.mapper'
import { EventConfigFilesUploadReponse } from '@/types/eventConfig'
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

export const deleteEventConfigSourceById = async (id: number): Promise<boolean> => {
  const endpoint = '/eventconf/sources'
  const payload = {
    sourceIds: [id]
  }
  try {
    const response = await v2.delete(endpoint, { data: payload })
    return response.status === 200 ? true : false
  } catch (error) {
    console.error('Error deleting event config source:', error)
    return false
  }
}
