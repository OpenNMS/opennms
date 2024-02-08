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

import { IFile } from '@/stores/fileEditorStore'

export const sortFilesAndFolders = (files: IFile[]): IFile[] => {
  // move folders to top, alphabetically
  for (const file of files) {
    if (file.children) {
      file.children = sortFilesAndFolders(file.children)
    }
  }

  const folders = files.filter((x) => x.children).sort((a, b) => a.name.localeCompare(b.name))
  const noFolders = files.filter((x) => !x.children).sort((a, b) => a.name.localeCompare(b.name))

  return [...folders, ...noFolders]
}

const getFolderPath = (folder: string, fullPath: string) => {
  const path = []
  const fullPathSplit = fullPath.split('/')

  // folder path should only reach the folder name
  for (const part of fullPathSplit) {
    path.push(part)
    if (part === folder) {
      break
    }
  }

  return path.join('/')
}

/**
 * Recursive function to create folders from file paths
 *
 * @param {IFile[]} folder - Array of files that represents a folder.
 * @param {string} file - name of the current file.
 * @param {string} fullPath - full path of file used to send file to BE.
 */
const addFileOrCreateFolder = (folder: IFile[], file: string, fullPath: string): IFile[] => {
  if (file.includes('/')) {
    // create a folder
    const fileNamePieces = file.split('/')
    const folderName = fileNamePieces[0]
    fileNamePieces.shift()
    const remaining = fileNamePieces.join('/')
    const existingFolder = folder.filter((x) => x.name === folderName)[0] as Required<IFile>

    if (existingFolder) {
      addFileOrCreateFolder(existingFolder.children, remaining, fullPath)
    } else {
      folder.push({
        name: folderName,
        children: addFileOrCreateFolder([], remaining, fullPath),
        fullPath: getFolderPath(folderName, fullPath)
      })
    }
  } else {
    // add file to the folder
    folder.push({ name: file, fullPath })
  }

  return folder
}

export const filesToFolders = (fileNames: string[]): IFile => {
  const files: IFile[] = []

  for (const file of fileNames) {
    addFileOrCreateFolder(files, file, file)
  }

  return { name: 'etc', children: sortFilesAndFolders(files) }
}

export const getExtensionFromFilenameSafely = (filename: string) => {
  return filename.slice((Math.max(0, filename.lastIndexOf('.')) || Infinity) + 1)
}
