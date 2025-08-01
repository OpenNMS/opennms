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

import { defineStore } from 'pinia'
import { uniq } from 'lodash'
import { filesToFolders } from '@/components/FileEditor/utils'
import API from '@/services'
import { FileEditorResponseLog } from '@/types'

export interface IFile {
  name: string
  children?: IFile[]
  fullPath?: string
  isEditing?: boolean
  isHidden?: boolean
}

export const useFileEditorStore = defineStore('fileEditorStore', () => {
  const fileNames = ref([] as string[])
  const file = ref('')
  const snippets = ref('')
  const searchValue = ref('')
  const contentModified = ref(false)
  const selectedFileName = ref('')
  const modifiedFileString = ref('')
  const logs = ref([] as FileEditorResponseLog[])
  const isConsoleOpen = ref(false)
  const isHelpOpen = ref(false)
  const filesInFolders = ref({} as IFile)
  const allowedFileExtensions = ref([] as string[])
  const changedFilesOnly = ref(false)
  const fileToDelete = ref<IFile | null>(null as unknown as IFile)
  const unsavedFiles = ref([] as string[])

  const getFileNames = async () => {
    const resp = await API.getFileNames(changedFilesOnly.value)
    fileNames.value = resp

    // save folder file structure
    const savedAndUnsavedFiles = uniq([...resp, ...unsavedFiles.value])

    const filteredFileNames = savedAndUnsavedFiles.filter(fileName => {
      const searchValueLower = searchValue.value.toLowerCase()
      const filename = fileName.toLowerCase()
      const selectedFileNameLower = selectedFileName.value.toLowerCase()

      return !searchValueLower || filename === selectedFileNameLower || (searchValueLower && filename.includes(searchValueLower))
    })

    filesInFolders.value = filesToFolders(filteredFileNames)
  }

  const clearLogs = () => {
    logs.value = []
  }

  const getFileExtensions = async () => {
    const extensions = await API.getFileExtensions()
    allowedFileExtensions.value = extensions
  }

  const getFile = async (fileName: string) => {
    // reset modified file string, snippets
    modifiedFileString.value = ''
    snippets.value = ''
    contentModified.value = false
    selectedFileName.value = fileName

    const newFile = await API.getFile(fileName)
    const newSnippets = await API.getSnippets(fileName)

    file.value = newFile
    snippets.value = newSnippets
  }

  const saveFolderFileStructure = (names: string[]) => {
    const savedAndUnsavedFiles = uniq([...names, ...unsavedFiles.value])

    const filteredFileNames = savedAndUnsavedFiles.filter(fileName => {
      const searchVal = searchValue.value.toLowerCase()
      const filename = fileName.toLowerCase()
      const selectedFile = selectedFileName.value.toLowerCase()

      return !searchVal || filename === selectedFile || (searchVal && filename.includes(searchVal))
    })

    filesInFolders.value = filesToFolders(filteredFileNames)
  }

  const setSearchValue = (value: string) => {
    searchValue.value = value
    saveFolderFileStructure(fileNames.value)
  }

  const removeFromUnsavedFilesList = (unsavedFile: string) => {
    unsavedFiles.value = unsavedFiles.value.filter(path => path !== unsavedFile)
  }

  const removeUnsavedFileFromState = (newFilePath: string) => {
    fileNames.value = fileNames.value.filter(path => path !== newFilePath)

    saveFolderFileStructure(fileNames.value)
  }

  const clearEditor = () => {
    file.value = 'clear'
    modifiedFileString.value = 'clear' // trigger reaction
    file.value = ' '
    modifiedFileString.value = ''
    contentModified.value = false
  }

  const addLog = (log: FileEditorResponseLog) => {
    logs.value = [...logs.value, log]
  }

  const deleteFile = async (fileName: string) => {
    // runs after delete
    const clearSearch = () => {
      // clear the search if it matches the deleted file
      const searchValueLower = searchValue.value.toLowerCase()
      const actualFileName = fileName.split('/').pop()?.toLowerCase()

      if (searchValueLower == actualFileName) {
        setSearchValue('')
      }
    }

    if (unsavedFiles.value.includes(fileName)) {
      // file not in DB, just delete from state
      removeFromUnsavedFilesList(fileName)
      removeUnsavedFileFromState(fileName)
      clearEditor()
      selectedFileName.value = ''
      fileToDelete.value = null  // closes confirmation modal

      clearSearch()
      return
    }

    const response = await API.deleteFile(fileName)

    addLog(response)

    if (response.success) {
      getFileNames()
      clearEditor()
      selectedFileName.value = ''
      clearSearch()
    }

    if (!response.success && response.msg) {
      isConsoleOpen.value = true
    }

    fileToDelete.value = null  // closes confirmation modal
  }

  const saveModifiedFile = async () => {
    const xml = 'xml'
    const plain = 'plain'
    const filename = selectedFileName.value

    if (!filename) {
      return
    }

    const fileString = modifiedFileString.value
    const splitFilename = filename.split('.')
    const filetype = splitFilename[splitFilename.length - 1]
    const mimetype = filetype === xml ? xml : plain
    const doc = new File([fileString], filename, { type: `text/${mimetype}` })

    const formData = new FormData()
    formData.append('upload', doc)
    const response = await API.postFile(filename, formData)

    addLog(response)

    if (response.success) {
      removeFromUnsavedFilesList(filename)
      file.value = fileString
      contentModified.value = false
    } else {
      if (response.msg) {
        isConsoleOpen.value = true
      }
    }
  }

  const triggerFileReset = () => {
    const savedFile = file.value

    file.value = '' // trigger reaction
    file.value = savedFile
    contentModified.value = false
  }

  const addFileToUnsavedFilesList = (unsavedFile: string) => {
    unsavedFiles.value = [...unsavedFiles.value, unsavedFile]
  }

  const saveNewFileToState = (newFilePath: string) => {
    fileNames.value = [...fileNames.value, newFilePath]

    saveFolderFileStructure(fileNames.value)
  }

  const setChangedFilesOnly = (val: boolean) => {
    changedFilesOnly.value = val
  }

  const setFileToDelete = (f: IFile | null) => {
    fileToDelete.value = f
  }

  const setIsConsoleOpen = (isOpen: boolean) => {
    isConsoleOpen.value = isOpen
  }

  const setIsFileContentModified = (val: boolean) => {
    contentModified.value = val
  }

  const setIsHelpOpen = (val: boolean) => {
    isHelpOpen.value = val
  }

  const setModifiedFileString = (s: string) => {
    modifiedFileString.value = s
  }

  const setSelectedFileName = (name: string) => {
    selectedFileName.value = name
  }


  return {
    fileNames,
    file,
    snippets,
    searchValue,
    contentModified,
    selectedFileName,
    modifiedFileString,
    logs,
    isConsoleOpen,
    isHelpOpen,
    filesInFolders,
    allowedFileExtensions,
    changedFilesOnly,
    fileToDelete,
    unsavedFiles,
    addFileToUnsavedFilesList,
    addLog,
    clearEditor,
    clearLogs,
    deleteFile,
    getFile,
    getFileExtensions,
    getFileNames,
    setChangedFilesOnly,
    setFileToDelete,
    setIsConsoleOpen,
    setIsFileContentModified,
    setIsHelpOpen,
    saveModifiedFile,
    setModifiedFileString,
    saveNewFileToState,
    setSearchValue,
    setSelectedFileName,
    triggerFileReset
  }
})
