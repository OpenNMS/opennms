import {
  MibCompilerFileLocation,
  MibCompilerGenerateEventsRequest,
  MibFileListResponse,
  MibUploadResponse
} from '@/types/mibCompiler'
import { rest } from './axiosInstances'

const endpoint = '/mib-compiler'

export const uploadMib = async (file: File, filename: string): Promise<MibUploadResponse> => {
  const buffer = await file.arrayBuffer()
  const response = await rest.post<MibUploadResponse>(`${endpoint}/upload`, buffer, {
    params: { filename },
    headers: { 'Content-Type': 'application/octet-stream' }
  })
  return response.data
}

export const compileMib = async (name: string): Promise<void> => {
  await rest.post(`${endpoint}/compile`, null, { params: { name } })
}

export const listPendingAndCompiledFiles = async (): Promise<MibFileListResponse> => {
  const response = await rest.get<MibFileListResponse>(`${endpoint}/files`)
  return response.data
}

export const deleteFile = async (location: MibCompilerFileLocation, fileName: string): Promise<void> => {
  await rest.delete(`${endpoint}/files/${encodeURIComponent(location)}/${encodeURIComponent(fileName)}`)
}

export const getFileText = async (location: MibCompilerFileLocation, fileName: string): Promise<string> => {
  const response = await rest.get<string>(
    `${endpoint}/files/${encodeURIComponent(location)}/${encodeURIComponent(fileName)}/text`
  )
  return response.data
}

export const setFileText = async (fileName: string, content: ArrayBuffer): Promise<void> => {
  await rest.post(`${endpoint}/files/pending/text`, content, {
    params: { fileName },
    headers: { 'Content-Type': 'application/octet-stream' }
  })
}

export const generateEvents = async (request: MibCompilerGenerateEventsRequest): Promise<void> => {
  await rest.post(`${endpoint}/generate-events`, request)
}

