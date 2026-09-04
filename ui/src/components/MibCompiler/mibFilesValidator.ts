import { UploadMibFileType } from '@/types/mibCompiler'
import axios from 'axios'

export const isValidMibExtension = (fileName: string): boolean => {
  return VALID_FILE_EXTENSION.some(ext => fileName.toLowerCase().endsWith(ext))
}

export const mibFilesValidator = async (file: File): Promise<{ isValid: boolean; errors: string[] }> => {
  const errors: string[] = []
  if (!isValidMibExtension(file.name)) {
    errors.push(`Invalid file type. Only ${VALID_FILE_EXTENSION.join(', ')} files are allowed.`)
  }
  if (file.size > MAX_FILE_SIZE) {
    errors.push(`File size exceeds the maximum limit of ${MAX_FILE_SIZE / (1024 * 1024)}MB.`)
  }
  return {
    isValid: errors.length === 0,
    errors
  }
}

export const isDuplicateFile = (fileName: string, existingFiles: UploadMibFileType[]): boolean => {
  return existingFiles.some(file => file.file.name === fileName)
}

export const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
// the parser resolves dependencies against files with these suffixes (plus no suffix)
export const VALID_FILE_EXTENSION = ['.txt', '.mib', '.my']

export const isWellFormedXml = (xml: string): boolean => {
  const parsed = new DOMParser().parseFromString(xml, 'application/xml')
  return parsed.querySelector('parsererror') === null
}

export const getGeneralErrorMessage = (error: unknown, fallbackMessage: string) => {
  if (!axios.isAxiosError(error)) {
    return fallbackMessage
  }
  const response = error.response?.data as { message?: string; error?: string } | undefined
  return response?.error || response?.message || fallbackMessage
}
