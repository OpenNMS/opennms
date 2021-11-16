import { IFile } from '@/store/fileEditor/state'

export const sortFilesAndFolders = (files: IFile[]): IFile[] => {
  // move folders to top, alphabetical
  for (const file of files) {
    if (file.children) {
      file.children = sortFilesAndFolders(file.children)
    }
  }
  const folders = files.filter(x => x.children).sort((a, b) => a.name.localeCompare(b.name))
  const noFolders = files.filter(x => !x.children)
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

export const filesToFolders = (fileNames: string[]): IFile => {
  const files: IFile[] = []

  const createFolder = (fileArray: IFile[], file: string, fullPath: string) => {
    const fileNamePieces = file.split('/')
    const folder = fileNamePieces[0]
    fileNamePieces.shift()
    const remaining = fileNamePieces.join('/')
    const existingFolder = fileArray.filter(x => x.name === folder)[0] as Required<IFile>

    if (existingFolder) addFileOrCreateFolder(existingFolder.children, remaining, fullPath)
    else fileArray.push({ name: folder, children: addFileOrCreateFolder([], remaining, fullPath), fullPath: getFolderPath(folder, fullPath) })
  }

  const addFileOrCreateFolder = (fileArray: IFile[], file: string, fullPath: string): IFile[] => {
    if (file.includes('/')) createFolder(fileArray, file, fullPath)
    else fileArray.push({ name: file, fullPath })
    return fileArray
  }

  for (const file of fileNames) addFileOrCreateFolder(files, file, file)
  return { name: 'Files', children: sortFilesAndFolders(files) }
}
