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

import { useAuthStore } from '@/stores/authStore'

const enum Roles {
  ROLE_ADMIN = 'ROLE_ADMIN',
  ROLE_USER = 'ROLE_USER',
  ROLE_REST = 'ROLE_REST',
  ROLE_FILESYSTEM_EDITOR = 'ROLE_FILESYSTEM_EDITOR',
  ROLE_DEVICE_CONFIG_BACKUP = 'ROLE_DEVICE_CONFIG_BACKUP'
}

type Role = typeof Roles[keyof typeof Roles]

const authStore = computed(() => useAuthStore())

const roles = computed(() => authStore.value.whoAmI.roles)
const rolesAreLoaded = computed(() => authStore.value.loaded)

const hasOneOf = (...rolesToCheck: Role[]) => {
  for (const role of rolesToCheck) {
    if (roles.value.includes(role)) {
      return true
    }
  }
  return false
}

const useRole = () => {
  const adminRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN))
  const filesystemEditorRole = computed<boolean>(() => hasOneOf(Roles.ROLE_FILESYSTEM_EDITOR))
  const dcbRole = computed<boolean>(() => hasOneOf(Roles.ROLE_ADMIN, Roles.ROLE_REST, Roles.ROLE_DEVICE_CONFIG_BACKUP))

  return { adminRole, filesystemEditorRole, dcbRole, rolesAreLoaded }
}

export default useRole
