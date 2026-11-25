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
  ResourceTypeDefinition,
  ResourceTypesConfig,
  ResourceTypesConfigSummary,
  ResourceTypesMetadata
} from '@/types/resourceTypesConfig'

export const PERSIST_ALL_SELECTOR_STRATEGY = 'org.opennms.netmgt.collection.support.PersistAllSelectorStrategy'
export const INDEX_STORAGE_STRATEGY = 'org.opennms.netmgt.collection.support.IndexStorageStrategy'

export const getDefaultResourceType = (): ResourceTypeDefinition => ({
  name: '',
  label: '',
  resourceLabel: '',
  persistenceSelectorStrategy: { className: PERSIST_ALL_SELECTOR_STRATEGY, parameters: [] },
  storageStrategy: { className: INDEX_STORAGE_STRATEGY, parameters: [] }
})

export const getDefaultResourceTypesConfig = (): ResourceTypesConfig => ({
  id: '',
  resourceTypes: []
})

/**
 * State for the resource types stored in the configuration manager (formerly etc/resource-types.d).
 *
 * The configuration is the transactional unit: adding, editing or deleting a resource type writes the whole
 * configuration back, changed on a copy so the loaded configuration only moves on once the save has succeeded.
 */
export const useResourceTypesConfigStore = defineStore('resourceTypesConfigStore', () => {
  const configs = ref<ResourceTypesConfigSummary[]>([])
  const currentConfig = ref<ResourceTypesConfig | null>(null)
  const metadata = ref<ResourceTypesMetadata | null>(null)
  const isLoading = ref(false)

  const configIds = computed(() => configs.value.map(config => config.id))

  /** The resource type names of every configuration except the given one. */
  const typeNamesOutside = (configId: string): string[] =>
    configs.value.filter(config => config.id !== configId).flatMap(config => config.resourceTypeNames)

  const fetchConfigs = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getResourceTypesConfigs()
    isLoading.value = false

    if (result.success) {
      configs.value = result.payload ?? []
    }
    return result
  }

  const fetchConfig = async (id: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getResourceTypesConfig(id)
    isLoading.value = false

    currentConfig.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchMetadata = async (): Promise<ValidationResult> => {
    const result = await API.getResourceTypesMetadata()

    if (result.success && result.payload) {
      metadata.value = result.payload
    }
    return result
  }

  /** After a success the list is refreshed; a failed refresh goes in the result's errors. */
  const refreshConfigs = async (result: ValidationResult): Promise<ValidationResult> => {
    const refreshed = await fetchConfigs()
    return refreshed.success ? result : { ...result, errors: [...(result.errors ?? []), refreshed.message] }
  }

  /**
   * A 412 means the loaded copy is stale, and keeping it would make every later save fail the same way.
   * Load the stored configuration instead, so the user sees what changed and the next save carries its version.
   */
  const reloadAfterPreconditionFailed = async (id: string, result: ValidationResult): Promise<ValidationResult> => {
    if (!isPreconditionFailed(result)) {
      return result
    }

    await fetchConfig(id)
    return { ...result, message: `${result.message} ${RELOADED_AFTER_PRECONDITION_FAILED}` }
  }

  const createConfig = async (config: ResourceTypesConfig): Promise<ValidationResult> => {
    const result = await API.createResourceTypesConfig(config)
    return result.success ? refreshConfigs(result) : result
  }

  /** Writes a changed copy of the configuration stored under id; config.id differing from id renames it. */
  const writeConfig = async (id: string, config: ResourceTypesConfig): Promise<ValidationResult> => {
    const result = await API.updateResourceTypesConfig(id, config)

    if (result.success) {
      // for the new entity tag
      await fetchConfig(config.id)
      return refreshConfigs(result)
    }
    return reloadAfterPreconditionFailed(id, result)
  }

  const renameConfig = async (id: string, newId: string): Promise<ValidationResult> => {
    if (!currentConfig.value || currentConfig.value.id !== id) {
      return createFailureResult('No resource types configuration is loaded.')
    }
    return writeConfig(id, { ...cloneState(currentConfig.value), id: newId })
  }

  const deleteConfig = async (id: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteResourceTypesConfig(id, version)

    if (!result.success) {
      return reloadAfterPreconditionFailed(id, result)
    }
    if (currentConfig.value?.id === id) {
      currentConfig.value = null
    }
    return refreshConfigs(result)
  }

  /** Adds (index null) or replaces a resource type and saves the configuration. */
  const saveResourceType = async (index: number | null, type: ResourceTypeDefinition): Promise<ValidationResult> => {
    if (!currentConfig.value) {
      return createFailureResult('No resource types configuration is loaded.')
    }

    const config = cloneState(currentConfig.value)

    if (index === null) {
      config.resourceTypes.push(type)
    } else if (index >= 0 && index < config.resourceTypes.length) {
      config.resourceTypes.splice(index, 1, type)
    } else {
      return createFailureResult('No such resource type.')
    }
    return writeConfig(config.id, config)
  }

  /** Removes a resource type and saves the configuration. */
  const deleteResourceType = async (index: number): Promise<ValidationResult> => {
    if (!currentConfig.value || index < 0 || index >= currentConfig.value.resourceTypes.length) {
      return createFailureResult('No such resource type.')
    }

    const config = cloneState(currentConfig.value)
    config.resourceTypes.splice(index, 1)
    return writeConfig(config.id, config)
  }

  /**
   * A deep clone of the resource type at index, so Cancel really discards; null when there is none.
   */
  const resourceTypeAt = (index: number): ResourceTypeDefinition | null => {
    const types = currentConfig.value?.resourceTypes

    if (!types || index < 0 || index >= types.length) {
      return null
    }

    const type = cloneState(types[index])
    // The server leaves out empty lists and values; the forms want them present.
    return {
      ...type,
      resourceLabel: type.resourceLabel ?? '',
      persistenceSelectorStrategy: { className: type.persistenceSelectorStrategy?.className ?? '', parameters: type.persistenceSelectorStrategy?.parameters ?? [] },
      storageStrategy: { className: type.storageStrategy?.className ?? '', parameters: type.storageStrategy?.parameters ?? [] }
    }
  }

  const uploadConfig = async (id: string, file: File, isXml: boolean): Promise<ValidationResult> => {
    const result = await API.uploadResourceTypesConfig(id, file, isXml)

    if (!result.success) {
      return result
    }
    if (currentConfig.value?.id === id) {
      await fetchConfig(id)
    }
    return refreshConfigs(result)
  }

  const resetState = (): void => {
    configs.value = []
    currentConfig.value = null
    metadata.value = null
    isLoading.value = false
  }

  return {
    configs,
    currentConfig,
    metadata,
    isLoading,
    configIds,
    typeNamesOutside,
    fetchConfigs,
    fetchConfig,
    fetchMetadata,
    createConfig,
    renameConfig,
    deleteConfig,
    saveResourceType,
    deleteResourceType,
    resourceTypeAt,
    uploadConfig,
    resetState
  }
})

export default useResourceTypesConfigStore
