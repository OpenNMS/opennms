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
import { THRESHOLDING_GROUP_PARAMETER, ThreshdServiceStatus } from '@/lib/thresholdValidator'
import API from '@/services'
import { CreateEditMode } from '@/types'
import type { ValidationResult } from '@/types/validation'
import { createFailureResult } from '@/types/validation'
import type {
  ThreshdAddressRange,
  ThreshdConfiguration,
  ThreshdPackage,
  ThreshdPackageSummary,
  ThreshdParameter,
  ThreshdService,
  Thresholder
} from '@/types/thresholdConfig'

export const getDefaultThreshdConfiguration = (): ThreshdConfiguration => ({
  threads: 5,
  packages: [],
  thresholder: []
})

export const getDefaultThreshdPackage = (): ThreshdPackage => ({
  name: '',
  filter: '',
  specifics: [],
  includeRanges: [],
  excludeRanges: [],
  includeUrls: [],
  services: [],
  outageCalendars: []
})

export const getDefaultThreshdService = (): ThreshdService => ({
  name: '',
  interval: 300000,
  userDefined: false,
  status: ThreshdServiceStatus.On,
  // Seeded, because a service without this parameter thresholds nothing.
  parameters: [{ key: THRESHOLDING_GROUP_PARAMETER, value: '' }]
})

export const getDefaultThresholder = (): Thresholder => ({ service: '', className: '', parameters: [] })

export const getDefaultAddressRange = (): ThreshdAddressRange => ({ begin: '', end: '' })

export const getDefaultParameter = (): ThreshdParameter => ({ key: '', value: '' })

export interface EntityDrawerState {
  visible: boolean
  mode: CreateEditMode
  index: number
}

const closedDrawer = (): EntityDrawerState => ({ visible: false, mode: CreateEditMode.None, index: -1 })

/**
 * State for the threshd daemon configuration (formerly threshd-configuration.xml).
 *
 * Kept separate from the threshold group store because these are two independent documents with their own
 * endpoints and save semantics. The only coupling is read-only: a package service names a threshold group,
 * and the group list wants to know which packages use it.
 */
export const useThreshdConfigurationStore = defineStore('threshdConfigurationStore', () => {
  const config = ref<ThreshdConfiguration>(getDefaultThreshdConfiguration())
  const packages = ref<ThreshdPackageSummary[]>([])
  const currentPackage = ref<ThreshdPackage | null>(null)
  const isLoading = ref(false)
  const serviceDrawer = ref<EntityDrawerState>(closedDrawer())
  const thresholderDrawer = ref<EntityDrawerState>(closedDrawer())

  const packageNames = computed(() => packages.value.map(pkg => pkg.name))

  /** Names of the packages whose services apply the given threshold group. */
  const packagesUsingGroup = computed(() => (groupName: string): string[] =>
    config.value.packages
      .filter(pkg =>
        (pkg.services ?? []).some(service =>
          (service.parameters ?? []).some(
            parameter => parameter.key === THRESHOLDING_GROUP_PARAMETER && parameter.value === groupName
          )
        )
      )
      .map(pkg => pkg.name)
  )

  const fetchConfiguration = async (): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getThreshdConfiguration()
    isLoading.value = false

    if (result.success && result.payload) {
      config.value = result.payload
    }
    return result
  }

  const fetchPackages = async (): Promise<ValidationResult> => {
    const result = await API.getThreshdPackages()

    if (result.success) {
      packages.value = result.payload ?? []
    }
    return result
  }

  const fetchPackage = async (name: string): Promise<ValidationResult> => {
    isLoading.value = true
    const result = await API.getThreshdPackage(name)
    isLoading.value = false

    currentPackage.value = result.success ? (result.payload ?? null) : null
    return result
  }

  const saveThreads = async (threads: number): Promise<ValidationResult> => {
    const updated = cloneDeep(config.value)
    updated.threads = threads

    const result = await API.updateThreshdConfiguration(updated)

    if (result.success) {
      await fetchConfiguration()
    }
    return result
  }

  const createPackage = async (pkg: ThreshdPackage): Promise<ValidationResult> => {
    const result = await API.createThreshdPackage(pkg)

    if (result.success) {
      await fetchConfiguration()
      await fetchPackages()
    }
    return result
  }

  /**
   * Writes the whole current package back, then refetches: the save yields a new entity tag, and the
   * scheduled outages page may have changed the outage calendars in the meantime.
   */
  const saveCurrentPackage = async (): Promise<ValidationResult> => {
    if (!currentPackage.value) {
      return createFailureResult('No threshd package is loaded.')
    }

    const name = currentPackage.value.name
    const result = await API.updateThreshdPackage(name, currentPackage.value)

    if (result.success) {
      await fetchPackage(name)
      await fetchConfiguration()
    }
    return result
  }

  const renamePackage = async (oldName: string, pkg: ThreshdPackage): Promise<ValidationResult> => {
    const result = await API.updateThreshdPackage(oldName, pkg)

    if (result.success) {
      await fetchConfiguration()
      await fetchPackages()
    }
    return result
  }

  const deletePackage = async (name: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteThreshdPackage(name, version)

    if (result.success) {
      if (currentPackage.value?.name === name) {
        currentPackage.value = null
      }
      await fetchConfiguration()
      await fetchPackages()
    }
    return result
  }

  const upsertService = (index: number | null, service: ThreshdService): void => {
    if (!currentPackage.value) {
      return
    }

    const services = currentPackage.value.services

    if (index === null || index < 0 || index >= services.length) {
      services.push(service)
    } else {
      services.splice(index, 1, service)
    }
  }

  const removeService = (index: number): void => {
    if (currentPackage.value && index >= 0 && index < currentPackage.value.services.length) {
      currentPackage.value.services.splice(index, 1)
    }
  }

  const upsertThresholder = async (index: number | null, thresholder: Thresholder): Promise<ValidationResult> => {
    const updated = cloneDeep(config.value)

    if (index === null || index < 0 || index >= updated.thresholder.length) {
      updated.thresholder.push(thresholder)
    } else {
      updated.thresholder.splice(index, 1, thresholder)
    }

    const result = await API.updateThreshdConfiguration(updated)

    if (result.success) {
      await fetchConfiguration()
    }
    return result
  }

  const removeThresholder = async (index: number): Promise<ValidationResult> => {
    const updated = cloneDeep(config.value)

    if (index < 0 || index >= updated.thresholder.length) {
      return createFailureResult('No such thresholder.')
    }

    updated.thresholder.splice(index, 1)

    const result = await API.updateThreshdConfiguration(updated)

    if (result.success) {
      await fetchConfiguration()
    }
    return result
  }

  const reloadThreshdConfiguration = async (): Promise<ValidationResult> => API.reloadThreshdConfiguration()

  const openServiceDrawer = (mode: CreateEditMode, index = -1): void => {
    serviceDrawer.value = { visible: true, mode, index }
  }

  const closeServiceDrawer = (): void => {
    serviceDrawer.value = closedDrawer()
  }

  const openThresholderDrawer = (mode: CreateEditMode, index = -1): void => {
    thresholderDrawer.value = { visible: true, mode, index }
  }

  const closeThresholderDrawer = (): void => {
    thresholderDrawer.value = closedDrawer()
  }

  const resetState = (): void => {
    config.value = getDefaultThreshdConfiguration()
    packages.value = []
    currentPackage.value = null
    isLoading.value = false
    serviceDrawer.value = closedDrawer()
    thresholderDrawer.value = closedDrawer()
  }

  return {
    config,
    packages,
    currentPackage,
    isLoading,
    serviceDrawer,
    thresholderDrawer,
    packageNames,
    packagesUsingGroup,
    fetchConfiguration,
    fetchPackages,
    fetchPackage,
    saveThreads,
    createPackage,
    saveCurrentPackage,
    renamePackage,
    deletePackage,
    upsertService,
    removeService,
    upsertThresholder,
    removeThresholder,
    reloadThreshdConfiguration,
    openServiceDrawer,
    closeServiceDrawer,
    openThresholderDrawer,
    closeThresholderDrawer,
    resetState
  }
})

export default useThreshdConfigurationStore
