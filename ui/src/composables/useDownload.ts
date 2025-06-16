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

import { AxiosResponse, AxiosResponseHeaders } from 'axios'

const useDownload = () => {
  /**
   * Download a file from a binary response
   *
   * @param   {AxiosResponse}  file  response from the server
   */
  const downloadFile = (file: AxiosResponse): void => {
    const name = getNameFromHeaders(file.headers)
    const extension = name.split('.').pop() || ''
    const blob = generateBlob(file, extension)
    generateDownload(blob, name)
  }

  return { downloadFile }
}

export default useDownload

/**
 * Construct the filename from the headers, removing superfluous characters
 *
 * @param   {Headers}  headers  headers from the server
 * @return  {string}            filename
 */
const getNameFromHeaders = (headers: AxiosResponseHeaders): string => {
  let name = ''
  const disposition = headers['content-disposition']

  if (disposition && disposition.indexOf('attachment') !== -1) {
    const filenameRegex = /filename[^;=\n]*=((['"]).*?\2|[^;\n]*)/
    const matches = filenameRegex.exec(disposition)
    if (matches != null && matches[1]) {
      name = matches[1].replace(/['"]/g, '')
    }
  }

  return name
}

/**
 * Generate a blob for the file object
 *
 * @param   {Headers}  headers   response from the server
 * @param   {string}   extension filename extension
 * @return  {Blob}               file object
 */
const generateBlob = (file: AxiosResponse, extension: string): Blob => {
  const contentType = (<AxiosResponseHeaders>file.headers)['content-type']

  // stringify if it's a JSON file
  if (extension.toLowerCase() === 'json') {
    return new Blob([JSON.stringify(file.data)], { type: contentType })
  }

  return new Blob([file.data], { type: contentType })
}

/**
 * Create and call the target <a/> element
 *
 * @param   {Blob}    blob      file object
 * @param   {string}  name  name of file to save
 */
const generateDownload = (blob: Blob, name: string): void => {
  const url = window.URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = name
  a.click()
}
