import axios from 'axios'

import type { SnmpV3User, TrapConfig } from '@/types/trapConfig'

import { v2 } from './axiosInstances'

const endpoint = '/trapd'

export type TrapdConfigurationUpdatePayload = Partial<Omit<TrapConfig, 'snmpv3User'>>

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

export const uploadTrapdConfiguration = async (file: File): Promise<TrapConfig | null> => {
  const formData = new FormData()
  formData.append('upload', file)

  try {
    const response = await v2.post(`${endpoint}/upload`, formData)

    if (response.status === 204) {
      return null
    }

    if (response.status === 200) {
      return response.data as TrapConfig
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to upload trapd configuration.')
  }
}

export const getTrapdConfiguration = async (): Promise<TrapConfig> => {
  try {
    const response = await v2.get(`${endpoint}/get-config`)

    if (response.status === 200) {
      return response.data as TrapConfig
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to retrieve trapd configuration.')
  }
}

export const updateTrapdConfiguration = async (
  payload: TrapdConfigurationUpdatePayload
): Promise<TrapConfig> => {
  try {
    const response = await v2.put(`${endpoint}/update-config`, payload)

    if (response.status === 200) {
      return response.data as TrapConfig
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to update trapd configuration.')
  }
}

export const saveTrapdUser = async (user: SnmpV3User): Promise<SnmpV3User> => {
  try {
    const response = await v2.post(`${endpoint}/save-user`, user)

    if (response.status === 200) {
      return response.data as SnmpV3User
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to save trapd user.')
  }
}

export const updateTrapdUser = async (index: number, user: SnmpV3User): Promise<SnmpV3User> => {
  try {
    const response = await v2.put(`${endpoint}/update-user`, user, {
      params: {
        index
      }
    })

    if (response.status === 200) {
      return response.data as SnmpV3User
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to update trapd user.')
  }
}

export const deleteTrapdUser = async (index: number): Promise<SnmpV3User> => {
  try {
    const response = await v2.delete(`${endpoint}/delete-user`, {
      params: {
        index
      }
    })

    if (response.status === 200) {
      return response.data as SnmpV3User
    }

    throw new Error(`Unexpected response status: ${response.status}`)
  } catch (error) {
    return throwTrapdServiceError(error, 'Failed to delete trapd user.')
  }
}