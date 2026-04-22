import { mapTrapdConfigFromServer } from '@/mappers/trapdConfig.mapper'
import type { TrapConfig } from '@/types/trapConfig'
import axios from 'axios'
import { v2 } from './axiosInstances'

const endpoint = '/trapd'

const getTrapdServiceErrorMessage = (error: unknown, fallbackMessage: string): string => {
  if (axios.isAxiosError(error)) {
    const responseData = error.response?.data

    if (typeof responseData === 'string' && responseData.trim().length > 0) {
      return responseData
    }

    if (
      responseData &&
      typeof responseData === 'object' &&
      'message' in responseData &&
      typeof responseData.message === 'string' &&
      responseData.message.trim().length > 0
    ) {
      return responseData.message
    }

    if (typeof error.message === 'string' && error.message.trim().length > 0) {
      return error.message
    }
  }

  if (error instanceof Error && error.message.trim().length > 0) {
    return error.message
  }

  return fallbackMessage
}

const throwTrapdServiceError = (error: unknown, fallbackMessage: string): never => {
  console.error(fallbackMessage, error)
  throw new Error(getTrapdServiceErrorMessage(error, fallbackMessage))
}

export const uploadTrapdConfiguration = async (file: File): Promise<void> => {
  const formData = new FormData()
  formData.append('upload', file)

  try {
    const response = await v2.post(`${endpoint}/upload`, formData)

    if (response.status === 200) {
      return
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to upload trapd configuration.')
  }
}

export const getTrapdConfiguration = async (): Promise<TrapConfig> => {
  try {
    const response = await v2.get(`${endpoint}/config`)

    if (response.status === 200) {
      return mapTrapdConfigFromServer(response.data) as TrapConfig
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to retrieve trapd configuration.')
  }
}

export const updateTrapdConfiguration = async (payload: TrapConfig): Promise<void> => {
  try {
    const response = await v2.put(`${endpoint}/config`, payload)

    if (response.status === 200) {
      return
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to update trapd configuration.')
  }
}

