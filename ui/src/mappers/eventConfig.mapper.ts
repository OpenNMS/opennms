import { EventConfigFilesUploadReponse } from '@/types/eventConfig'

export const mapUploadedEventConfigFilesResponseFromServer = (response: any): EventConfigFilesUploadReponse => {
  console.log('Mapping uploaded event config files response from server:', response)
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
