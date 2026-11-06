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
import { ThresholdDefinitionKind, FilterOperator, ThresholdType } from '@/lib/thresholdValidator'
import API from '@/services'
import type { ValidationResult } from '@/types/validation'
import { createFailureResult } from '@/types/validation'
import type {
  BaseThresholdDef,
  Expression,
  ResourceFilter,
  Threshold,
  ThresholdDefinition,
  ThresholdGroup,
  ThresholdGroupSummary,
  ThresholdDsType
} from '@/types/thresholdConfig'

export const getDefaultBaseThresholdDef = (): BaseThresholdDef => ({
  relaxed: false,
  description: '',
  type: ThresholdType.High,
  dsType: 'node',
  value: '',
  // Populated rather than blank: the schema requires both even for the change thresholds that ignore them.
  rearm: '0',
  trigger: '1',
  dsLabel: '',
  exprLabel: '',
  triggeredUEI: '',
  rearmedUEI: '',
  filterOperator: FilterOperator.Or,
  resourceFilters: []
})

export const getDefaultThreshold = (): Threshold => ({ ...getDefaultBaseThresholdDef(), dsName: '' })

export const getDefaultExpression = (): Expression => ({ ...getDefaultBaseThresholdDef(), expression: '' })

export const getDefaultResourceFilter = (): ResourceFilter => ({ field: '', content: '' })

export const getDefaultThresholdGroup = (): ThresholdGroup => ({
  name: '',
  rrdRepository: '',
  thresholds: [],
  expressions: []
})

const PRECONDITION_FAILED = 412

/**
 * State for the thresholding configuration (formerly thresholds.xml).
 *
 * The group is the transactional unit: adding, editing or deleting a threshold writes the whole group back,
 * changed on a copy so the loaded group only moves on once the save has succeeded. That mirrors the REST API, which has no per-threshold
 * endpoint because a threshold has no identity beyond its position in the group.
 */
export const useThresholdGroupStore = defineStore('thresholdGroupStore', () => {
  const groups = ref<ThresholdGroupSummary[]>([])
  const currentGroup = ref<ThresholdGroup | null>(null)
  const dsTypes = ref<ThresholdDsType[]>([])
  const thresholdTypes = ref<string[]>([])
  const filterOperators = ref<string[]>([])
  const isLoading = ref(false)

  const groupNames = computed(() => groups.value.map(group => group.name))
  const currentThresholds = computed(() => currentGroup.value?.thresholds ?? [])
  const currentExpressions = computed(() => currentGroup.value?.expressions ?? [])

  const definitionsIn = (group: ThresholdGroup, kind: ThresholdDefinitionKind): ThresholdDefinition[] =>
    kind === ThresholdDefinitionKind.Threshold
      ? (group.thresholds as ThresholdDefinition[])
      : (group.expressions as ThresholdDefinition[])

  const definitionsOf = (kind: ThresholdDefinitionKind): ThresholdDefinition[] | undefined =>
    currentGroup.value ? definitionsIn(currentGroup.value, kind) : undefined

  const fetchGroups = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getThresholdGroups()
    isLoading.value = false

    if (result.success) {
      groups.value = result.payload ?? []
    }
    return result
  }

  const fetchGroup = async (name: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getThresholdGroup(name)
    isLoading.value = false

    currentGroup.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const fetchMetadata = async (): Promise<ValidationResult> => {
    const result = await API.getThresholdingMetadata()

    if (result.success && result.payload) {
      dsTypes.value = result.payload.dsTypes ?? []
      thresholdTypes.value = result.payload.thresholdTypes ?? []
      filterOperators.value = result.payload.filterOperators ?? []
    }
    return result
  }

  const createGroup = async (group: ThresholdGroup): Promise<ValidationResult> => {
    const result = await API.createThresholdGroup(group)

    if (result.success) {
      await fetchGroups()
    }
    return result
  }

  /**
   * A 412 means the loaded copy is stale, and keeping it would make every later save fail the same way.
   * Load the stored group instead, so the user sees what changed and the next save carries its version.
   */
  const reloadAfterConflict = async (name: string, result: ValidationResult): Promise<ValidationResult> => {
    if (result.status !== PRECONDITION_FAILED) {
      return result
    }

    await fetchGroup(name)
    return { ...result, message: `${result.message} The current version has been loaded; please make your change again.` }
  }

  /**
   * Writes a changed copy of the loaded group back. The change is never made on currentGroup itself, so a
   * failed save leaves the page showing what is stored. Refetches after a success for the new entity tag.
   */
  const writeGroup = async (group: ThresholdGroup): Promise<ValidationResult> => {
    const name = group.name
    const result = await API.updateThresholdGroup(name, group)

    if (result.success) {
      await fetchGroup(name)
      await fetchGroups()
      return result
    }
    return reloadAfterConflict(name, result)
  }

  /** Adds (index null or out of range) or replaces a definition and saves the group. */
  const saveDefinition = async (
    kind: ThresholdDefinitionKind,
    index: number | null,
    definition: ThresholdDefinition
  ): Promise<ValidationResult> => {
    if (!currentGroup.value) {
      return createFailureResult('No threshold group is loaded.')
    }

    const group = cloneDeep(currentGroup.value)
    const definitions = definitionsIn(group, kind)

    if (index === null || index < 0 || index >= definitions.length) {
      definitions.push(definition)
    } else {
      definitions.splice(index, 1, definition)
    }
    return writeGroup(group)
  }

  /** Removes a definition and saves the group. */
  const deleteDefinition = async (kind: ThresholdDefinitionKind, index: number): Promise<ValidationResult> => {
    const definitions = definitionsOf(kind)

    if (!currentGroup.value || !definitions || index < 0 || index >= definitions.length) {
      return createFailureResult('No such threshold.')
    }

    const group = cloneDeep(currentGroup.value)
    definitionsIn(group, kind).splice(index, 1)
    return writeGroup(group)
  }

  const renameGroup = async (oldName: string, group: ThresholdGroup): Promise<ValidationResult> => {
    const result = await API.updateThresholdGroup(oldName, group)

    if (result.success) {
      await fetchGroups()
      return result
    }
    return reloadAfterConflict(oldName, result)
  }

  const deleteGroup = async (name: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteThresholdGroup(name, version)

    if (result.success) {
      if (currentGroup.value?.name === name) {
        currentGroup.value = null
      }
      await fetchGroups()
    }
    return result
  }

  /**
   * Swaps a resource filter with its neighbour. Filters are applied in order, so this is meaningful, and it
   * lives here rather than in the component so it can be tested without mounting anything.
   */
  const moveResourceFilter = (filters: ResourceFilter[], index: number, direction: -1 | 1): ResourceFilter[] => {
    const target = index + direction

    if (index < 0 || index >= filters.length || target < 0 || target >= filters.length) {
      return filters
    }

    const reordered = [...filters]
    const [moved] = reordered.splice(index, 1)
    reordered.splice(target, 0, moved)
    return reordered
  }

  /**
   * A deep clone of the definition at index, so Cancel really discards; a new default definition for index null,
   * and null when there is no definition at index.
   */
  const definitionAt = (kind: ThresholdDefinitionKind, index: number | null): ThresholdDefinition | null => {
    if (index === null) {
      return kind === ThresholdDefinitionKind.Threshold ? getDefaultThreshold() : getDefaultExpression()
    }

    const definitions = definitionsOf(kind)

    return definitions && index >= 0 && index < definitions.length ? cloneDeep(definitions[index]) : null
  }

  const resetState = (): void => {
    groups.value = []
    currentGroup.value = null
    dsTypes.value = []
    thresholdTypes.value = []
    filterOperators.value = []
    isLoading.value = false
  }

  return {
    groups,
    currentGroup,
    dsTypes,
    thresholdTypes,
    filterOperators,
    isLoading,
    groupNames,
    currentThresholds,
    currentExpressions,
    fetchGroups,
    fetchGroup,
    fetchMetadata,
    createGroup,
    renameGroup,
    deleteGroup,
    saveDefinition,
    deleteDefinition,
    moveResourceFilter,
    definitionAt,
    resetState
  }
})

export default useThresholdGroupStore
