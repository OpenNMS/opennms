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
  const fileToDelete = ref<IFile>(null as unknown as IFile)
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

  const getFileExtensions = async () => {
    const extensions = await API.getFileExtensions()
    allowedFileExtensions.value = extensions
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
    getFileNames,
    getFileExtensions
  }
})
