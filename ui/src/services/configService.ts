import { rest, restFile } from './axiosInstances'
import marked from 'marked'
import { AxiosError } from 'axios'

const endpoint = '/filesystem'

const getFileNames = async (): Promise<string[]> => {
  try {
    const resp = await rest.get(endpoint)
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

const getSnippets = async (fileName: string): Promise<string> => {
  try {
    const resp = await rest.get(`${endpoint}/help?f=${fileName}`)
    return marked(resp.data, { breaks: true })
  } catch (err) {
    return ''
  }
}

const postFile = async (fileName: string, formData: FormData): Promise<{ success: boolean, data: string }> => {
  try {
    const resp = await restFile.post(`${endpoint}/contents?f=${fileName}`, formData)
    return { success: true, data: resp.data }
  } catch (err) {
    return { success: false, data: err as string}
  }
}

export {
  getFileNames,
  getFile,
  postFile,
  getSnippets
}
