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
import { CreateEditMode } from '@/types'
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

export interface DefinitionDrawerState {
  visible: boolean
  mode: CreateEditMode
  kind: ThresholdDefinitionKind
  index: number
}

const closedDrawer = (): DefinitionDrawerState => ({
  visible: false,
  mode: CreateEditMode.None,
  kind: ThresholdDefinitionKind.Threshold,
  index: -1
})

/**
 * State for the thresholding configuration (formerly thresholds.xml).
 *
 * The group is the transactional unit: adding, editing or deleting a threshold changes the local copy, and
 * one save then writes the whole group back. That mirrors the REST API, which has no per-threshold
 * endpoint because a threshold has no identity beyond its position in the group.
 */
export const useThresholdGroupStore = defineStore('thresholdGroupStore', () => {
  const groups = ref<ThresholdGroupSummary[]>([])
  const currentGroup = ref<ThresholdGroup | null>(null)
  const dsTypes = ref<ThresholdDsType[]>([])
  const thresholdTypes = ref<string[]>([])
  const filterOperators = ref<string[]>([])
  const isLoading = ref(false)
  const definitionDrawer = ref<DefinitionDrawerState>(closedDrawer())

  const groupNames = computed(() => groups.value.map(group => group.name))
  const currentThresholds = computed(() => currentGroup.value?.thresholds ?? [])
  const currentExpressions = computed(() => currentGroup.value?.expressions ?? [])

  const definitionsOf = (kind: ThresholdDefinitionKind): ThresholdDefinition[] | undefined => {
    if (!currentGroup.value) {
      return undefined
    }
    return kind === ThresholdDefinitionKind.Threshold
      ? (currentGroup.value.thresholds as ThresholdDefinition[])
      : (currentGroup.value.expressions as ThresholdDefinition[])
  }

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
   * Writes the whole current group back. Always refetches afterwards: the save replaces the stored document
   * and yields a new entity tag, so keeping the old copy would make the next save fail with a 412.
   */
  const saveCurrentGroup = async (): Promise<ValidationResult> => {
    if (!currentGroup.value) {
      return createFailureResult('No threshold group is loaded.')
    }

    const name = currentGroup.value.name
    const result = await API.updateThresholdGroup(name, currentGroup.value)

    if (result.success) {
      await fetchGroup(name)
      await fetchGroups()
    }
    return result
  }

  const renameGroup = async (oldName: string, group: ThresholdGroup): Promise<ValidationResult> => {
    const result = await API.updateThresholdGroup(oldName, group)

    if (result.success) {
      await fetchGroups()
    }
    return result
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

  const upsertDefinition = (
    kind: ThresholdDefinitionKind,
    index: number | null,
    definition: ThresholdDefinition
  ): void => {
    const definitions = definitionsOf(kind)

    if (!definitions) {
      return
    }

    if (index === null || index < 0 || index >= definitions.length) {
      definitions.push(definition)
    } else {
      definitions.splice(index, 1, definition)
    }
  }

  const removeDefinition = (kind: ThresholdDefinitionKind, index: number): void => {
    const definitions = definitionsOf(kind)

    if (definitions && index >= 0 && index < definitions.length) {
      definitions.splice(index, 1)
    }
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

  const reloadThresholdConfiguration = async (): Promise<ValidationResult> => API.reloadThresholdingConfiguration()

  const openDefinitionDrawer = (kind: ThresholdDefinitionKind, mode: CreateEditMode, index = -1): void => {
    definitionDrawer.value = { visible: true, mode, kind, index }
  }

  const closeDefinitionDrawer = (): void => {
    definitionDrawer.value = closedDrawer()
  }

  /** The definition the drawer is editing, deep-cloned so Cancel really discards. */
  const drawerDefinition = (): ThresholdDefinition => {
    const { kind, mode, index } = definitionDrawer.value

    if (mode === CreateEditMode.Edit) {
      const definitions = definitionsOf(kind)

      if (definitions && index >= 0 && index < definitions.length) {
        return cloneDeep(definitions[index])
      }
    }

    return kind === ThresholdDefinitionKind.Threshold ? getDefaultThreshold() : getDefaultExpression()
  }

  const resetState = (): void => {
    groups.value = []
    currentGroup.value = null
    dsTypes.value = []
    thresholdTypes.value = []
    filterOperators.value = []
    isLoading.value = false
    definitionDrawer.value = closedDrawer()
  }

  return {
    groups,
    currentGroup,
    dsTypes,
    thresholdTypes,
    filterOperators,
    isLoading,
    definitionDrawer,
    groupNames,
    currentThresholds,
    currentExpressions,
    fetchGroups,
    fetchGroup,
    fetchMetadata,
    createGroup,
    saveCurrentGroup,
    renameGroup,
    deleteGroup,
    upsertDefinition,
    removeDefinition,
    moveResourceFilter,
    reloadThresholdConfiguration,
    openDefinitionDrawer,
    closeDefinitionDrawer,
    drawerDefinition,
    resetState
  }
})

export default useThresholdGroupStore
