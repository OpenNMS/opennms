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

import { cloneDeep } from 'lodash'
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
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
  ThreshdService
} from '@/types/thresholdConfig'

export const getDefaultThreshdConfiguration = (): ThreshdConfiguration => ({
  packages: []
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

export const getDefaultAddressRange = (): ThreshdAddressRange => ({ begin: '', end: '' })

export const getDefaultParameter = (): ThreshdParameter => ({ key: '', value: '' })

export interface EntityDrawerState {
  visible: boolean
  mode: CreateEditMode
  index: number
}

const PRECONDITION_FAILED = 412

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
  /**
   * The name currentPackage is stored under. Distinct from currentPackage.name, which the form edits in
   * place, so it still addresses the stored package while a rename is pending.
   */
  const loadedPackageName = ref<string | null>(null)
  const isLoading = ref(false)
  const serviceDrawer = ref<EntityDrawerState>(closedDrawer())

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
    loadedPackageName.value = currentPackage.value ? name : null
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
   * Writes a package back in place of the loaded one, then refetches: the save yields a new entity tag, and
   * the scheduled outages page may have changed the outage calendars in the meantime. When pkg carries a
   * new name this is a rename, and loadedPackageName follows it.
   */
  const writePackage = async (pkg: ThreshdPackage): Promise<ValidationResult> => {
    if (!loadedPackageName.value) {
      return createFailureResult('No threshd package is loaded.')
    }

    const name = loadedPackageName.value
    const result = await API.updateThreshdPackage(name, pkg)

    if (result.success) {
      // The server trims the name it stores.
      await fetchPackage(pkg.name.trim())
      await fetchConfiguration()
      await fetchPackages()
    } else if (result.status === PRECONDITION_FAILED) {
      // The loaded copy is stale, and its version would make every later save fail the same way. Reloading
      // also drops pending edits in the form, which were made against what someone has since changed.
      await fetchPackage(name)
      return { ...result, message: `${result.message} The current version has been loaded; please make your change again.` }
    }
    return result
  }

  /** Writes the whole current package back, including any pending edit to its name. */
  const saveCurrentPackage = async (): Promise<ValidationResult> => {
    if (!currentPackage.value) {
      return createFailureResult('No threshd package is loaded.')
    }
    return writePackage(currentPackage.value)
  }

  /**
   * Adds (index null) or replaces a service and saves the package. The change is made on a copy, so a
   * failed save leaves the loaded package as it was instead of showing a service that is not stored.
   */
  const saveService = async (index: number | null, service: ThreshdService): Promise<ValidationResult> => {
    if (!currentPackage.value) {
      return createFailureResult('No threshd package is loaded.')
    }

    const pkg = cloneDeep(currentPackage.value)

    if (index === null || index < 0 || index >= pkg.services.length) {
      pkg.services.push(service)
    } else {
      pkg.services.splice(index, 1, service)
    }
    return writePackage(pkg)
  }

  /** Removes a service and saves the package, on a copy for the same reason as saveService. */
  const deleteService = async (index: number): Promise<ValidationResult> => {
    if (!currentPackage.value || index < 0 || index >= currentPackage.value.services.length) {
      return createFailureResult('No such service.')
    }

    const pkg = cloneDeep(currentPackage.value)
    pkg.services.splice(index, 1)
    return writePackage(pkg)
  }

  const deletePackage = async (name: string, version?: string): Promise<ValidationResult> => {
    const result = await API.deleteThreshdPackage(name, version)

    if (result.success) {
      if (loadedPackageName.value === name) {
        currentPackage.value = null
        loadedPackageName.value = null
      }
      await fetchConfiguration()
      await fetchPackages()
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

  const resetState = (): void => {
    config.value = getDefaultThreshdConfiguration()
    packages.value = []
    currentPackage.value = null
    loadedPackageName.value = null
    isLoading.value = false
    serviceDrawer.value = closedDrawer()
  }

  return {
    config,
    packages,
    currentPackage,
    loadedPackageName,
    isLoading,
    serviceDrawer,
    packageNames,
    packagesUsingGroup,
    fetchConfiguration,
    fetchPackages,
    fetchPackage,
    createPackage,
    saveCurrentPackage,
    deletePackage,
    saveService,
    deleteService,
    reloadThreshdConfiguration,
    openServiceDrawer,
    closeServiceDrawer,
    resetState
  }
})

export default useThreshdConfigurationStore
