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

import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { cloneState } from '@/lib/utils'
import API from '@/services'
import type { ValidationResult } from '@/types/validation'
import { createFailureResult, isPreconditionFailed, RELOADED_AFTER_PRECONDITION_FAILED } from '@/types/validation'
import type {
  XmlCollection,
  XmlCollectionMetadata,
  XmlCollectionSummary,
  XmlGroup,
  XmlGroupFile,
  XmlGroupFileSummary,
  XmlObject,
  XmlRequest,
  XmlSource
} from '@/types/xmlCollectionConfig'

/** The archives of the shipped xml-elasticsearch-cluster-stats collection, the usual starting point. */
export const DEFAULT_RRAS = [
  'RRA:AVERAGE:0.5:1:2016',
  'RRA:AVERAGE:0.5:12:1488',
  'RRA:AVERAGE:0.5:288:366',
  'RRA:MAX:0.5:288:366',
  'RRA:MIN:0.5:288:366'
]

export const getDefaultCollection = (): XmlCollection => ({
  name: '',
  rrd: { step: 300, rras: [...DEFAULT_RRAS] },
  sources: []
})

export const getDefaultRequest = (): XmlRequest => ({
  method: 'GET',
  parameters: [],
  headers: [],
  contentType: '',
  content: ''
})

export const getDefaultSource = (): XmlSource => ({
  url: '',
  request: null,
  importGroups: [],
  groups: []
})

export const getDefaultObject = (): XmlObject => ({
  name: '',
  type: 'gauge',
  xpath: '',
  mappings: []
})

export const getDefaultGroup = (): XmlGroup => ({
  name: '',
  resourceType: 'node',
  resourceXpath: '/',
  keyXpath: '',
  timestampXpath: '',
  timestampFormat: '',
  resourceKeys: [],
  objects: [getDefaultObject()]
})

export const getDefaultGroupFile = (): XmlGroupFile => ({
  id: '',
  groups: []
})

const emptyMetadata = (): XmlCollectionMetadata => ({ attributeTypes: [], resourceTypes: [], groupFiles: [] })

/**
 * State for the XML collector configuration (formerly xml-datacollection-config.xml and xml-datacollection/*.xml).
 *
 * A collection is the transactional unit, like a threshold group: adding, editing or deleting a source writes the
 * whole collection back, changed on a copy, so the loaded collection only moves on once the save has succeeded.
 */
export const useXmlCollectionConfigStore = defineStore('xmlCollectionConfigStore', () => {
  const collections = ref<XmlCollectionSummary[]>([])
  const currentCollection = ref<XmlCollection | null>(null)
  const groupFiles = ref<XmlGroupFileSummary[]>([])
  const currentGroupFile = ref<XmlGroupFile | null>(null)
  const rrdRepository = ref('')
  const metadata = ref<XmlCollectionMetadata>(emptyMetadata())
  const isLoading = ref(false)

  const collectionNames = computed(() => collections.value.map(collection => collection.name))
  const groupFileIds = computed(() => groupFiles.value.map(groupFile => groupFile.id))

  // ------------------------------------------------------------------ reads

  const fetchCollections = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getXmlCollections()
    isLoading.value = false

    if (result.success) {
      collections.value = result.payload ?? []
    }
    return result
  }

  const fetchCollection = async (name: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getXmlCollection(name)
    isLoading.value = false

    currentCollection.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchGroupFiles = async (): Promise<ValidationResult> => {
    const result = await API.getXmlGroupFiles()

    if (result.success) {
      groupFiles.value = result.payload ?? []
    }
    return result
  }

  const fetchGroupFile = async (id: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getXmlGroupFile(id)
    isLoading.value = false

    currentGroupFile.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchSettings = async (): Promise<ValidationResult> => {
    const result = await API.getXmlCollectionSettings()

    if (result.success) {
      rrdRepository.value = result.payload?.rrdRepository ?? ''
    }
    return result
  }

  const fetchMetadata = async (): Promise<ValidationResult> => {
    const result = await API.getXmlCollectionMetadata()

    if (result.success && result.payload) {
      metadata.value = { ...emptyMetadata(), ...result.payload }
    }
    return result
  }

  // ---------------------------------------------------------- collections

  const createCollection = async (collection: XmlCollection): Promise<ValidationResult> => {
    const result = await API.createXmlCollection(collection)

    if (result.success) {
      await Promise.all([fetchCollections(), fetchGroupFiles()])
    }
    return result
  }

  /**
   * A 412 means the loaded copy is stale, and keeping it would make every later save fail the same way.
   * Load the stored collection instead, so the user sees what changed and the next save carries its version.
   */
  const reloadCollectionAfterPreconditionFailed = async (name: string, result: ValidationResult): Promise<ValidationResult> => {
    if (!isPreconditionFailed(result)) {
      return result
    }

    await fetchCollection(name)
    return { ...result, message: `${result.message} ${RELOADED_AFTER_PRECONDITION_FAILED}` }
  }

  /** Writes a collection, renaming it when its name differs from oldName; refetches for the new entity tag. */
  const updateCollection = async (oldName: string, collection: XmlCollection): Promise<ValidationResult> => {
    const result = await API.updateXmlCollection(oldName, collection)

    if (result.success) {
      await Promise.all([fetchCollection(collection.name), fetchCollections(), fetchGroupFiles()])
      return result
    }
    return reloadCollectionAfterPreconditionFailed(oldName, result)
  }

  const deleteCollection = async (name: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteXmlCollection(name, version)

    if (result.success) {
      if (currentCollection.value?.name === name) {
        currentCollection.value = null
      }
      await Promise.all([fetchCollections(), fetchGroupFiles()])
    }
    return result
  }

  /** Adds (index null) or replaces a source of the loaded collection and saves it. */
  const saveSource = async (index: number | null, source: XmlSource): Promise<ValidationResult> => {
    if (!currentCollection.value) {
      return createFailureResult('No XML data collection is loaded.')
    }

    const collection = cloneState(currentCollection.value)

    if (index === null) {
      collection.sources.push(source)
    } else if (index >= 0 && index < collection.sources.length) {
      collection.sources.splice(index, 1, source)
    } else {
      return createFailureResult('No such source.')
    }
    return updateCollection(collection.name, collection)
  }

  /** Removes a source of the loaded collection and saves it. */
  const deleteSource = async (index: number): Promise<ValidationResult> => {
    if (!currentCollection.value || index < 0 || index >= currentCollection.value.sources.length) {
      return createFailureResult('No such source.')
    }

    const collection = cloneState(currentCollection.value)
    collection.sources.splice(index, 1)
    return updateCollection(collection.name, collection)
  }

  /** A deep clone of the source at index, so Cancel really discards; a new source for null; null when there is none. */
  const sourceAt = (index: number | null): XmlSource | null => {
    if (index === null) {
      return getDefaultSource()
    }

    const sources = currentCollection.value?.sources

    return sources && index >= 0 && index < sources.length ? cloneState(sources[index]) : null
  }

  // ---------------------------------------------------------- group files

  const createGroupFile = async (groupFile: XmlGroupFile): Promise<ValidationResult> => {
    const result = await API.createXmlGroupFile(groupFile)

    if (result.success) {
      await Promise.all([fetchGroupFile(groupFile.id), fetchGroupFiles(), fetchMetadata()])
    }
    return result
  }

  const updateGroupFile = async (groupFile: XmlGroupFile): Promise<ValidationResult> => {
    const result = await API.updateXmlGroupFile(groupFile)

    if (result.success) {
      await Promise.all([fetchGroupFile(groupFile.id), fetchGroupFiles()])
      return result
    }
    if (isPreconditionFailed(result)) {
      await fetchGroupFile(groupFile.id)
      return { ...result, message: `${result.message} ${RELOADED_AFTER_PRECONDITION_FAILED}` }
    }
    return result
  }

  const deleteGroupFile = async (id: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteXmlGroupFile(id, version)

    if (result.success) {
      if (currentGroupFile.value?.id === id) {
        currentGroupFile.value = null
      }
      await Promise.all([fetchGroupFiles(), fetchMetadata()])
    }
    return result
  }

  // ------------------------------------------------------------- settings

  const saveSettings = async (repository: string): Promise<ValidationResult> => {
    const result = await API.updateXmlCollectionSettings({ rrdRepository: repository })

    if (result.success) {
      await fetchSettings()
    }
    return result
  }

  /** Reloads everything the overview shows, e.g. after an upload. */
  const fetchAll = async (): Promise<ValidationResult> => {
    const results = await Promise.all([fetchCollections(), fetchGroupFiles(), fetchSettings(), fetchMetadata()])
    return results.find(result => !result.success) ?? results[0]
  }

  const resetState = (): void => {
    collections.value = []
    currentCollection.value = null
    groupFiles.value = []
    currentGroupFile.value = null
    rrdRepository.value = ''
    metadata.value = emptyMetadata()
    isLoading.value = false
  }

  return {
    collections,
    currentCollection,
    groupFiles,
    currentGroupFile,
    rrdRepository,
    metadata,
    isLoading,
    collectionNames,
    groupFileIds,
    fetchCollections,
    fetchCollection,
    fetchGroupFiles,
    fetchGroupFile,
    fetchSettings,
    fetchMetadata,
    fetchAll,
    createCollection,
    updateCollection,
    deleteCollection,
    saveSource,
    deleteSource,
    sourceAt,
    createGroupFile,
    updateGroupFile,
    deleteGroupFile,
    saveSettings,
    resetState
  }
})

export default useXmlCollectionConfigStore
