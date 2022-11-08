import { IFile } from '@/store/fileEditor/state'

export const sortFilesAndFolders = (files: IFile[]): IFile[] => {
  // move folders to top, alphabetical
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
    if (part === folder) break
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

    if (existingFolder) addFileOrCreateFolder(existingFolder.children, remaining, fullPath)
    else
      folder.push({
        name: folderName,
        children: addFileOrCreateFolder([], remaining, fullPath),
        fullPath: getFolderPath(folderName, fullPath)
      })
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
