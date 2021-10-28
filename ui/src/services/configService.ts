import { rest, restFile } from './axiosInstances'

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
    return resp.data
  } catch (err) {
    return ''
  }
}

const postFile = async (fileName: string, formData: FormData): Promise<File | null> => {
  try {
    const resp = await restFile.post(`${endpoint}/contents?f=${fileName}`, formData)
    return resp.data
  } catch (err) {
    return null
  }
}

export {
  getFileNames,
  getFile,
  postFile,
  getSnippets
}
