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

import { until } from '@vueuse/core'
import { Ref } from 'vue'
import { NavigationGuard } from 'vue-router'
import useRole from '@/composables/useRole'
import useSnackbar from '@/composables/useSnackbar'

/**
 * beforeEnter guard for a route restricted to a role, e.g.
 * `beforeEnter: requireRole(adminRole, 'Must be admin to ...')`.
 *
 * whoAmI loads asynchronously at app start (App.vue), so on a direct load or a
 * refresh the roles are not known yet when the first navigation runs. Waiting
 * for them here means the page never mounts for a user without the role, so it
 * never fires requests the server will refuse. (authStore sets `loaded` even
 * when whoAmI fails, so this cannot hang.)
 *
 * The redirect is returned rather than done with router.push() inside the
 * guard: an in-app navigation is cancelled, leaving the user where they were;
 * the first navigation of a fresh load has nowhere to stay, so it goes home.
 */
export const requireRole = (hasRole: Ref<boolean>, deniedMsg: string): NavigationGuard => async (_to, from) => {
  const { rolesAreLoaded } = useRole()
  await until(rolesAreLoaded).toBe(true)

  if (hasRole.value) {
    return true
  }

  useSnackbar().showSnackBar({ msg: deniedMsg, error: true })
  return from.matched.length ? false : '/'
}
