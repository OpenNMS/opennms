///
/// Licensed to The OpenNMS Group, Inc (TOG) under one or more
/// contributor license agreements.  See the LICENSE.md file
/// distributed with this work for additional information
/// regarding copyright ownership.
///
/// TOG licenses this file to You under the GNU Affero General
/// Public License Version 3 (the "License") or (at your option)
/// any later version.  You may not use this file except in
/// compliance with the License.  You may obtain a copy of the
/// License at:
///
///      https://www.gnu.org/licenses/agpl-3.0.txt
///
/// Unless required by applicable law or agreed to in writing,
/// software distributed under the License is distributed on an
/// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
/// either express or implied.  See the License for the specific
/// language governing permissions and limitations under the
/// License.
///

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
