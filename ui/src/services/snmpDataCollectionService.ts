import { mapUploadedDataCollectionFilesResponseFromServer } from '@/mappers/snmpDataCollection.mapper'
import { SnmpDataCollectionSourceUploadResponse } from '@/types/snmpDataCollection'
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

