import { rest, restFile } from './axiosInstances'
import { marked } from 'marked'
import { FileEditorResponseLog } from '@/types'

const endpoint = '/filesystem'

const getFileNames = async (changedFilesOnly: boolean): Promise<string[]> => {
  try {
    const resp = await rest.get(`${endpoint}?changedFilesOnly=${changedFilesOnly}`)
    return resp.data
  } catch (err) {
    return []
  }
}

const getFile = async (fileName: string): Promise<string> => {
  try {
    const resp = await rest.get(`${endpoint}/contents?f=${fileName}`)
    return resp.data
  } catch (err) {
    return ''
  }
}

const deleteFile = async (fileName: string): Promise<FileEditorResponseLog> => {
  try {
    const resp = await rest.delete(`${endpoint}/contents?f=${fileName}`)
    return { success: true, msg: resp.data }
  } catch (err: any) {
    return { success: false, msg: err.response.data as string }
  }
}

const getSnippets = async (fileName: string): Promise<string> => {
  try {
    const resp = await rest.get(`${endpoint}/help?f=${fileName}`)
    return marked(resp.data, { breaks: true })
  } catch (err) {
    return ''
  }
}

const getFileExtensions = async (): Promise<string[]> => {
  try {
    const resp = await rest.get(`${endpoint}/extensions`)
    return resp.data
  } catch (err) {
    return []
  }
}

const postFile = async (fileName: string, formData: FormData): Promise<FileEditorResponseLog> => {
  try {
    const resp = await restFile.post(`${endpoint}/contents?f=${fileName}`, formData)
    return { success: true, msg: resp.data }
  } catch (err: any) {
    return { success: false, msg: err.response.data as string }
  }
}

export { getFile, postFile, deleteFile, getSnippets, getFileNames, getFileExtensions }
