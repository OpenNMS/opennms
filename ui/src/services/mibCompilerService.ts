import {
  MibCompileResponse,
  MibCompilerFileInfoWithContent,
  MibCompilerFileLocation,
  MibCompilerGenerateEventsRequest,
  MibFileListResponse,
  MibUploadResponse,
  MibGenerateEventsResponse,
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

export const compileMib = async (name: string): Promise<MibCompileResponse> => {
  const response = await rest.post<MibCompileResponse>(`${endpoint}/compile`, null, { params: { name } })
  return response.data
}

export const listPendingAndCompiledFiles = async (): Promise<MibFileListResponse> => {
  const response = await rest.get<MibFileListResponse>(`${endpoint}/files`)
  return response.data
}

export const deleteFile = async (location: MibCompilerFileLocation, fileName: string): Promise<void> => {
  await rest.delete(`${endpoint}/files/${encodeURIComponent(location)}/${encodeURIComponent(fileName)}`)
}

export const getFileText = async (location: MibCompilerFileLocation, fileName: string): Promise<MibCompilerFileInfoWithContent> => {
  const response = await rest.get<MibCompilerFileInfoWithContent>(
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

export const generateEvents = async (request: MibCompilerGenerateEventsRequest): Promise<MibGenerateEventsResponse> => {
  const response = await rest.post<MibGenerateEventsResponse>(`${endpoint}/generate-events`, request)
  return response.data
}

