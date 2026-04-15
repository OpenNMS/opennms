import { UploadMibFileType } from '@/types/mibCompiler'

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

