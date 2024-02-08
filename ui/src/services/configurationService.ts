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

import { rest } from '@/services/axiosInstances'

const getProvisionD = '/cm/provisiond/default'

const getProvisionDService = async () => {
  try {
    const response = await rest.get(getProvisionD)
    if (response.status === 200) {
      return response.data
    }
  } catch (err) {
    console.error('issue with getProvisionDService api', err)
  }
}

const putProvisionDService = async (payload: any) => {
  const resp = await rest.put(getProvisionD, payload)

  try {
    if (resp.status === 200) {
      return resp
    }
  } catch {
    console.error('issue with putProvisionDService api')
  }
}

export { getProvisionDService, putProvisionDService }
