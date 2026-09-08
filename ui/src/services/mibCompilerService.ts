import {
  MibCompileResult,
  MibDataCollectionPreview,
  MibDirectory,
  MibEventsPreview,
  MibFilesResponse,
  MibGraphTemplatesResult,
  MibUploadResponse
} from '@/types/mibCompiler'
import { v2 } from './axiosInstances'

const endpoint = '/mibs'

export const listMibFiles = async (): Promise<MibFilesResponse> => {
  const response = await v2.get<MibFilesResponse>(endpoint)
  return response.data
}

export const uploadMibFiles = async (files: File[]): Promise<MibUploadResponse> => {
  const formData = new FormData()
  files.forEach((file) => {
    formData.append('upload', file)
  })
  const response = await v2.post<MibUploadResponse>(`${endpoint}/upload`, formData)
  return response.data
}

export const getMibFileContent = async (dir: MibDirectory, name: string): Promise<string> => {
  const response = await v2.get<string>(
    `${endpoint}/${dir}/${encodeURIComponent(name)}`,
    { responseType: 'text', transformResponse: [data => data] }
  )
  return response.data
}

export const updatePendingMibFile = async (name: string, content: string): Promise<void> => {
  await v2.put(`${endpoint}/pending/${encodeURIComponent(name)}`, content, {
    headers: { 'Content-Type': 'text/plain' }
  })
}

export const deleteMibFile = async (dir: MibDirectory, name: string): Promise<void> => {
  await v2.delete(`${endpoint}/${dir}/${encodeURIComponent(name)}`)
}

export const compileMibFile = async (name: string, overwrite = false): Promise<MibCompileResult> => {
  const response = await v2.post<MibCompileResult>(
    `${endpoint}/pending/${encodeURIComponent(name)}/compile`,
    null,
    { params: { overwrite }}
  )
  return response.data
}

export const generateEvents = async (name: string, ueiBase?: string): Promise<MibEventsPreview> => {
  const response = await v2.post<MibEventsPreview>(
    `${endpoint}/compiled/${encodeURIComponent(name)}/events`,
    null,
    { params: ueiBase ? { ueiBase } : {}}
  )
  return response.data
}

export const generateDataCollection = async (name: string): Promise<MibDataCollectionPreview> => {
  const response = await v2.post<MibDataCollectionPreview>(
    `${endpoint}/compiled/${encodeURIComponent(name)}/datacollection`
  )
  return response.data
}

export const generateGraphTemplates = async (name: string, dryRun = true): Promise<MibGraphTemplatesResult> => {
  const response = await v2.post<MibGraphTemplatesResult>(
    `${endpoint}/compiled/${encodeURIComponent(name)}/graph-templates`,
    null,
    { params: { dryRun }}
  )
  return response.data
}
