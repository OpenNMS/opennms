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
import type { ValidationResult, ValidationResultWithPayload } from '@/types/validation'
import { createFailureResult } from '@/types/validation'
import type {
  JdbcCollection,
  JdbcColumn,
  JdbcDataCollectionMetadata,
  JdbcQuery,
  JdbcSource,
  JdbcSourceSummary
} from '@/types/jdbcDataCollection'

/** Used until the metadata has loaded; the same values the server hands out. */
const FALLBACK_METADATA: JdbcDataCollectionMetadata = {
  attributeTypes: ['gauge', 'counter', 'string'],
  ifTypes: ['ignore', 'all'],
  resourceTypes: [],
  defaultRrdRepository: '${install.share.dir}/rrd/snmp/',
  defaultRrdStep: 300,
  defaultRras: [
    'RRA:AVERAGE:0.5:1:2016',
    'RRA:AVERAGE:0.5:12:1488',
    'RRA:AVERAGE:0.5:288:366',
    'RRA:MAX:0.5:288:366',
    'RRA:MIN:0.5:288:366'
  ]
}

export const getDefaultColumn = (): JdbcColumn => ({ name: '', dataSourceName: '', type: 'gauge', alias: '' })

export const getDefaultQuery = (): JdbcQuery => ({
  name: '',
  recheckInterval: 0,
  ifType: 'ignore',
  resourceType: '',
  instanceColumn: '',
  queryString: '',
  columns: [getDefaultColumn()]
})

export const getDefaultCollection = (metadata: JdbcDataCollectionMetadata = FALLBACK_METADATA): JdbcCollection => ({
  name: '',
  rrdStep: metadata.defaultRrdStep,
  rras: [...metadata.defaultRras],
  queries: []
})

export const getDefaultSource = (metadata: JdbcDataCollectionMetadata = FALLBACK_METADATA): JdbcSource => ({
  id: '',
  rrdRepository: metadata.defaultRrdRepository,
  collections: []
})

const PRECONDITION_FAILED = 412

/** Drops the read-only fields the server computes, so a write carries only what is stored. */
const writable = (collection: JdbcCollection): JdbcCollection => {
  const { usedBy: _usedBy, shadowedBy: _shadowedBy, ...rest } = collection
  return rest
}

/**
 * State for the JDBC data collection configuration (formerly jdbc-datacollection-config.xml and
 * jdbc-datacollection.d/*.xml), one source per former file.
 *
 * The collection is the unit of transfer: adding, editing or deleting a query writes the whole collection back,
 * changed on a copy so the loaded source only moves on once the save has succeeded. Every write carries the
 * version of the loaded source and refetches it afterwards for the new one.
 */
export const useJdbcDataCollectionStore = defineStore('jdbcDataCollectionStore', () => {
  const sources = ref<JdbcSourceSummary[]>([])
  const currentSource = ref<JdbcSource | null>(null)
  const metadata = ref<JdbcDataCollectionMetadata>(FALLBACK_METADATA)
  const isLoading = ref(false)

  const sourceIds = computed(() => sources.value.map(source => source.id))

  const fetchSources = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getJdbcSources()
    isLoading.value = false

    if (result.success) {
      sources.value = result.payload ?? []
    }
    return result
  }

  const fetchSource = async (id: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getJdbcSource(id)
    isLoading.value = false

    currentSource.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchMetadata = async (): Promise<ValidationResult> => {
    const result = await API.getJdbcDataCollectionMetadata()

    if (result.success && result.payload) {
      metadata.value = result.payload
    }
    return result
  }

  /**
   * Names of the collections of every source, except the one named {@code exceptName} in source
   * {@code exceptSourceId}: the names a new or renamed collection must not take.
   */
  const takenCollectionNames = (exceptSourceId?: string, exceptName?: string): string[] => {
    const names: string[] = []
    for (const source of sources.value) {
      for (const name of source.collectionNames ?? []) {
        if (!(source.id === exceptSourceId && name === exceptName)) {
          names.push(name)
        }
      }
    }
    return names
  }

  /**
   * A 412 means the loaded copy is stale, and keeping it would make every later save fail the same way.
   * Load the stored source instead, so the user sees what changed and the next save carries its version.
   */
  const reloadAfterConflict = async (id: string, result: ValidationResult): Promise<ValidationResult> => {
    if (result.status !== PRECONDITION_FAILED) {
      return result
    }

    await fetchSource(id)
    return { ...result, message: `${result.message} The current version has been loaded; please make your change again.` }
  }

  /** Refetches the source and the list after a successful write; a failed refresh is reported in errors. */
  const refreshAfterWrite = async (id: string, result: ValidationResult): Promise<ValidationResult> => {
    const [source, list] = await Promise.all([fetchSource(id), fetchSources()])
    const errors = [source, list].filter(entry => !entry.success).map(entry => entry.message)
    return errors.length ? { ...result, errors } : result
  }

  const createSource = async (source: JdbcSource): Promise<ValidationResult> => {
    const result = await API.createJdbcSource({ ...source, id: source.id.trim(), collections: [] })

    if (result.success) {
      const list = await fetchSources()
      return list.success ? result : { ...result, errors: [list.message] }
    }
    return result
  }

  const updateRepository = async (rrdRepository: string): Promise<ValidationResult> => {
    if (!currentSource.value) {
      return createFailureResult('No JDBC data collection source is loaded.')
    }

    const id = currentSource.value.id
    const result = await API.updateJdbcSource({ ...cloneDeep(currentSource.value), rrdRepository })
    return result.success ? refreshAfterWrite(id, result) : reloadAfterConflict(id, result)
  }

  const deleteSource = async (id: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteJdbcSource(id, version)

    if (result.success) {
      if (currentSource.value?.id === id) {
        currentSource.value = null
      }
      const list = await fetchSources()
      return list.success ? result : { ...result, errors: [list.message] }
    }
    return result
  }

  const createCollection = async (collection: JdbcCollection): Promise<ValidationResult> => {
    if (!currentSource.value) {
      return createFailureResult('No JDBC data collection source is loaded.')
    }

    const id = currentSource.value.id
    const result = await API.createJdbcCollection(id, writable({ ...collection, name: collection.name.trim() }), currentSource.value.version)
    return result.success ? refreshAfterWrite(id, result) : reloadAfterConflict(id, result)
  }

  /** Replaces the collection named {@code name} of the loaded source; a different name in the body renames it. */
  const saveCollection = async (name: string, collection: JdbcCollection): Promise<ValidationResult> => {
    if (!currentSource.value) {
      return createFailureResult('No JDBC data collection source is loaded.')
    }

    const id = currentSource.value.id
    const result = await API.updateJdbcCollection(id, name, writable({ ...collection, name: collection.name.trim() }), currentSource.value.version)
    return result.success ? refreshAfterWrite(id, result) : reloadAfterConflict(id, result)
  }

  const deleteCollection = async (name: string): Promise<ValidationResult> => {
    if (!currentSource.value) {
      return createFailureResult('No JDBC data collection source is loaded.')
    }

    const id = currentSource.value.id
    const result = await API.deleteJdbcCollection(id, name, currentSource.value.version)
    return result.success ? refreshAfterWrite(id, result) : reloadAfterConflict(id, result)
  }

  const findCollection = (name: string): JdbcCollection | undefined =>
    currentSource.value?.collections.find(collection => collection.name === name)

  /** A deep clone of a collection of the loaded source, so Cancel really discards; null when there is none. */
  const collectionAt = (name: string): JdbcCollection | null => {
    const collection = findCollection(name)
    return collection ? cloneDeep(collection) : null
  }

  /** A deep clone of a query; a new default query for index -1; null when there is no such query. */
  const queryAt = (collectionName: string, index: number): JdbcQuery | null => {
    const collection = findCollection(collectionName)

    if (!collection) {
      return null
    }
    if (index < 0) {
      return getDefaultQuery()
    }
    return index < collection.queries.length ? cloneDeep(collection.queries[index]) : null
  }

  /** Adds (index -1) or replaces a query and saves its collection. */
  const saveQuery = async (collectionName: string, index: number, query: JdbcQuery): Promise<ValidationResult> => {
    const collection = collectionAt(collectionName)

    if (!collection) {
      return createFailureResult(`No collection named '${collectionName}' is loaded.`)
    }

    if (index < 0 || index >= collection.queries.length) {
      collection.queries.push(query)
    } else {
      collection.queries.splice(index, 1, query)
    }
    return saveCollection(collectionName, collection)
  }

  const deleteQuery = async (collectionName: string, index: number): Promise<ValidationResult> => {
    const collection = collectionAt(collectionName)

    if (!collection || index < 0 || index >= collection.queries.length) {
      return createFailureResult('No such query.')
    }

    collection.queries.splice(index, 1)
    return saveCollection(collectionName, collection)
  }

  const uploadSource = async (file: File, id: string, replace: boolean): Promise<ValidationResultWithPayload<JdbcSourceSummary>> => {
    const result = await API.uploadJdbcSource(file, id, replace)

    if (result.success) {
      const list = await fetchSources()
      return list.success ? result : { ...result, errors: [list.message] }
    }
    return result
  }

  const resetState = (): void => {
    sources.value = []
    currentSource.value = null
    metadata.value = FALLBACK_METADATA
    isLoading.value = false
  }

  return {
    sources,
    currentSource,
    metadata,
    isLoading,
    sourceIds,
    fetchSources,
    fetchSource,
    fetchMetadata,
    takenCollectionNames,
    createSource,
    updateRepository,
    deleteSource,
    createCollection,
    saveCollection,
    deleteCollection,
    collectionAt,
    queryAt,
    saveQuery,
    deleteQuery,
    uploadSource,
    resetState
  }
})

export default useJdbcDataCollectionStore
