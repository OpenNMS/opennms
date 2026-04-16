import { MibCompileResponse, UploadMibFileType } from '@/types/mibCompiler'
import axios from 'axios';

export const mibFilesValidator = async (file: File): Promise<{ isValid: boolean; errors: string[] }> => {
  const errors: string[] = []
  if (!file.name.toLowerCase().endsWith('.txt')) {
    errors.push('Invalid file type. Only .txt files are allowed.')
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
  return existingFiles.some((file) => file.file.name === fileName)
}

export enum FOLDER_LOCATIONS {
  PENDING = 'PENDING',
  COMPILED = 'COMPILED'
}

export const MAX_FILE_SIZE = 5 * 1024 * 1024 // 5MB
export const VALID_FILE_EXTENSION = '.txt,.mib'

export const getCompileErrorMessage = (error: unknown) => {
  if (!axios.isAxiosError<MibCompileResponse>(error)) {
    return 'Failed to compile MIB file.'
  }

  const response = error.response?.data
  if (!response || typeof response !== 'object') {
    return 'Failed to compile MIB file.'
  }

  const message = response.message || 'Failed to compile MIB file.'
  if (Array.isArray(response.missingDependencies) && response.missingDependencies.length > 0) {
    return `${message} Missing dependencies: ${response.missingDependencies.join(', ')}`
  }

  if (response.errors) {
    return `${message} ${response.errors}`
  }

  return message
}

