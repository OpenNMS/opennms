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

import { getDefaultTrapdConfig } from '@/lib/trapdValidator'
import { getTrapdConfiguration } from '@/services/trapdConfigurationService'
import { CreateEditMode } from '@/types'
import { TrapConfigStoreState } from '@/types/trapConfig'
import { defineStore } from 'pinia'

export const useTrapdConfigStore = defineStore('useTrapdConfigStore', {
  state: (): TrapConfigStoreState => ({
    isLoading: false,
    trapdConfig: getDefaultTrapdConfig(),
    snmpV3Users: [],
    activeTab: 0,
    credentialDrawerState: {
      visible: false,
      key: null
    },
    createUserDrawerState: {
      visible: false,
      mode: CreateEditMode.None,
      selectedUserIndex: -1
    }
  }),
  actions: {
    async fetchTrapConfig() {
      // Implementation for fetching trap configuration goes here
      const response = await getTrapdConfiguration()
      this.trapdConfig = response
      this.snmpV3Users = response.snmpv3User
    },
    openCredentialDrawer(key: string) {
      this.credentialDrawerState.visible = true
      this.credentialDrawerState.key = key
    },
    closeCredentialDrawer() {
      this.credentialDrawerState.visible = false
      this.credentialDrawerState.key = null
    },
    openCreateUserDrawer(mode: CreateEditMode, selectedUserIndex: number) {
      this.createUserDrawerState.visible = true
      this.createUserDrawerState.mode = mode
      this.createUserDrawerState.selectedUserIndex = selectedUserIndex
    },
    closeCreateUserDrawer() {
      this.createUserDrawerState.visible = false
      this.createUserDrawerState.mode = CreateEditMode.None
      this.createUserDrawerState.selectedUserIndex = -1
    }
  }
})
