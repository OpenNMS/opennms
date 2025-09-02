import { EventConfigFilesUploadReponse } from '@/types/eventConfig'

export const mapUploadedEventConfigFilesResponseFromServer = (response: any): EventConfigFilesUploadReponse => {
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
