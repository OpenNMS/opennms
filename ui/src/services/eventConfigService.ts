import { v2 } from './axiosInstances'

export const uploadEventConfigFiles = async (files: File[]): Promise<void> => {
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
    console.log('Event config files uploaded successfully:', response.data)

  } catch (error) {
    console.error('Error uploading event config files:', error)
    throw error
  }
}
