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

import axios from 'axios'
import { v2 } from './axiosInstances'
import type { DashboardLayout } from '@/types/dashboard'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'

// Single system-wide dashboard document, served by DashboardRestService at
// /api/v2/dashboard/system. GET 404s until a layout has been saved, in which
// case we fall back to the built-in default.
const endpoint = '/dashboard/system'

export const getSystemDashboard = async (): Promise<DashboardLayout> => {
  try {
    const response = await v2.get<DashboardLayout>(endpoint)

    if (response.status === 200 && Array.isArray(response.data?.panels)) {
      return response.data
    }

    return createDefaultLayout()
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      // no layout saved yet — use the built-in default
      return createDefaultLayout()
    }

    console.error('Failed to load system dashboard layout:', error)
    return createDefaultLayout()
  }
}

export const saveSystemDashboard = async (layout: DashboardLayout): Promise<void> => {
  await v2.put(endpoint, layout)
}
