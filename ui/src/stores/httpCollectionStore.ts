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
import { cloneDeep } from 'lodash'
import API from '@/services'
import type { ValidationResult } from '@/types/validation'
import { createFailureResult } from '@/types/validation'
import type {
  HttpAttrib,
  HttpCollection,
  HttpCollectionMetadata,
  HttpCollectionSettings,
  HttpCollectionSummary,
  HttpRrd,
  HttpUri
} from '@/types/httpCollectionConfig'

export const DEFAULT_RRD: HttpRrd = {
  step: 300,
  rras: [
    'RRA:AVERAGE:0.5:1:2016',
    'RRA:AVERAGE:0.5:12:1488',
    'RRA:AVERAGE:0.5:288:366',
    'RRA:MAX:0.5:288:366',
    'RRA:MIN:0.5:288:366'
  ]
}

export const getDefaultCollection = (rrd: HttpRrd = DEFAULT_RRD): HttpCollection => ({
  name: '',
  rrd: cloneDeep(rrd),
  uris: []
})

/** The schema defaults, shown in the form rather than left implicit so the user sees what will be requested. */
export const getDefaultUri = (): HttpUri => ({
  name: '',
  url: {
    method: 'GET',
    httpVersion: '1.1',
    scheme: 'http',
    host: '${ipaddr}',
    port: 80,
    path: '/',
    userAgent: null,
    virtualHost: null,
    userInfo: null,
    query: null,
    fragment: null,
    matches: '(.*)',
    responseRange: '100-399',
    canonicalEquivalence: false,
    caseInsensitive: false,
    comments: false,
    dotall: false,
    literal: false,
    multiline: false,
    unicodeCase: false,
    unixLines: false,
    parameters: [],
    headers: []
  },
  attributes: []
})

export const getDefaultAttrib = (): HttpAttrib => ({ alias: '', matchGroup: 1, type: 'gauge' })

const PRECONDITION_FAILED = 412
const RELOADED = 'The current version has been loaded; please make your change again.'

/**
 * State for the HTTP collector configuration (formerly http-datacollection-config.xml).
 *
 * The collection is the transactional unit: adding, editing or deleting a URI writes the whole collection back,
 * changed on a copy so the loaded collection only moves on once the save has succeeded.
 */
export const useHttpCollectionStore = defineStore('httpCollectionStore', () => {
  const collections = ref<HttpCollectionSummary[]>([])
  const currentCollection = ref<HttpCollection | null>(null)
  const settings = ref<HttpCollectionSettings | null>(null)
  const metadata = ref<HttpCollectionMetadata | null>(null)
  const isLoading = ref(false)

  const collectionNames = computed(() => collections.value.map(collection => collection.name))
  const currentUris = computed(() => currentCollection.value?.uris ?? [])
  const attributeTypes = computed(() => metadata.value?.attributeTypes ?? ['gauge', 'counter', 'string'])
  const methods = computed(() => metadata.value?.methods ?? ['GET', 'POST'])
  const httpVersions = computed(() => metadata.value?.httpVersions ?? ['1.0', '1.1'])
  const schemes = computed(() => metadata.value?.schemes ?? ['http', 'https'])
  const defaultRrd = computed(() => metadata.value?.defaultRrd ?? DEFAULT_RRD)

  const fetchCollections = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getHttpCollections()
    isLoading.value = false

    if (result.success) {
      collections.value = result.payload ?? []
    }
    return result
  }

  const fetchCollection = async (name: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getHttpCollection(name)
    isLoading.value = false

    currentCollection.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchSettings = async (): Promise<ValidationResult> => {
    const result = await API.getHttpCollectionSettings()
    settings.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchMetadata = async (): Promise<ValidationResult> => {
    const result = await API.getHttpCollectionMetadata()

    if (result.success && result.payload) {
      metadata.value = result.payload
    }
    return result
  }

  const saveSettings = async (changed: HttpCollectionSettings): Promise<ValidationResult> => {
    const result = await API.updateHttpCollectionSettings(changed)
    await fetchSettings()

    if (result.status === PRECONDITION_FAILED) {
      return { ...result, message: `${result.message} ${RELOADED}` }
    }
    return result
  }

  const createCollection = async (collection: HttpCollection): Promise<ValidationResult> => {
    const result = await API.createHttpCollection(collection)

    if (result.success) {
      await fetchCollections()
    }
    return result
  }

  /**
   * A 412 means the loaded copy is stale, and keeping it would make every later save fail the same way.
   * Load the stored collection instead, so the user sees what changed and the next save carries its version.
   */
  const reloadAfterConflict = async (name: string, result: ValidationResult): Promise<ValidationResult> => {
    if (result.status !== PRECONDITION_FAILED) {
      return result
    }

    await fetchCollection(name)
    return { ...result, message: `${result.message} ${RELOADED}` }
  }

  /**
   * Saves a collection under the name it was loaded with. A different name in the body renames it. Refetches
   * after a success, for the new entity tag and so the page shows what the server stored.
   */
  const saveCollection = async (oldName: string, collection: HttpCollection): Promise<ValidationResult> => {
    const result = await API.updateHttpCollection(oldName, collection)

    if (result.success) {
      await fetchCollection(collection.name)
      await fetchCollections()
      return result
    }
    return reloadAfterConflict(oldName, result)
  }

  /** Adds (index null or out of range) or replaces a URI of the loaded collection and saves it. */
  const saveUri = async (index: number | null, uri: HttpUri): Promise<ValidationResult> => {
    if (!currentCollection.value) {
      return createFailureResult('No HTTP collection is loaded.')
    }

    const collection = cloneDeep(currentCollection.value)

    if (index === null || index < 0 || index >= collection.uris.length) {
      collection.uris.push(uri)
    } else {
      collection.uris.splice(index, 1, uri)
    }
    return saveCollection(collection.name, collection)
  }

  /** Removes a URI of the loaded collection and saves it. */
  const deleteUri = async (index: number): Promise<ValidationResult> => {
    if (!currentCollection.value || index < 0 || index >= currentCollection.value.uris.length) {
      return createFailureResult('No such URI.')
    }

    const collection = cloneDeep(currentCollection.value)
    collection.uris.splice(index, 1)
    return saveCollection(collection.name, collection)
  }

  const deleteCollection = async (name: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteHttpCollection(name, version)

    if (result.success) {
      if (currentCollection.value?.name === name) {
        currentCollection.value = null
      }
      await fetchCollections()
    }
    return result
  }

  /** A deep clone of the URI at index so Cancel really discards; a new default URI for index null; null if absent. */
  const uriAt = (index: number | null): HttpUri | null => {
    if (index === null) {
      return getDefaultUri()
    }

    const uris = currentCollection.value?.uris

    return uris && index >= 0 && index < uris.length ? cloneDeep(uris[index]) : null
  }

  const resetState = (): void => {
    collections.value = []
    currentCollection.value = null
    settings.value = null
    metadata.value = null
    isLoading.value = false
  }

  return {
    collections,
    currentCollection,
    settings,
    metadata,
    isLoading,
    collectionNames,
    currentUris,
    attributeTypes,
    methods,
    httpVersions,
    schemes,
    defaultRrd,
    fetchCollections,
    fetchCollection,
    fetchSettings,
    fetchMetadata,
    saveSettings,
    createCollection,
    saveCollection,
    saveUri,
    deleteUri,
    deleteCollection,
    uriAt,
    resetState
  }
})

export default useHttpCollectionStore
