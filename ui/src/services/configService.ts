import { rest, restFile } from './axiosInstances'
import * as xml from 'xml2js'

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
    let parsed = ''
    const resp = await rest.get(`${endpoint}/contents?f=${fileName}`)

    xml.parseString(resp.data, (err, result) => {
      if (!err) parsed = result
    })

    return parsed
  } catch (err) {
    return ''
  }
}

const postFile = async ({ formData, fileName }: { formData: File, fileName: string }): Promise<File | null> => {
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
  postFile
}
