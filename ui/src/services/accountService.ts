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

import { rest } from './axiosInstances'

const endpoint = '/account'

/**
 * Returns whether the current session has a pending password-change requirement
 * (set when admin logs in with the default admin/admin credentials).
 */
const getRequiresPasswordChange = async (): Promise<boolean> => {
  try {
    const resp = await rest.get(`${endpoint}/requiresPasswordChange`)
    return resp.data?.requiresPasswordChange === true
  } catch {
    return false
  }
}

/**
 * Clears the password-change flag without changing the password (Skip).
 */
const dismissPasswordChangePrompt = async (): Promise<void> => {
  try {
    await rest.delete(`${endpoint}/requiresPasswordChange`)
  } catch {
    // best-effort; flag will clear on session expiry anyway
  }
}

/**
 * Changes the current user's password.
 * Returns true on success, or an error message string on failure.
 */
const changePassword = async (currentPassword: string, newPassword: string): Promise<true | string> => {
  try {
    await rest.post(`${endpoint}/changePassword`, { currentPassword, newPassword })
    return true
  } catch (err: any) {
    const message = err?.response?.data?.error
    return typeof message === 'string' ? message : 'An unexpected error occurred.'
  }
}

export { getRequiresPasswordChange, dismissPasswordChangePrompt, changePassword }
