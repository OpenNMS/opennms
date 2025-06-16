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
import { Resource, ResourcesApiResponse } from '@/types'

const endpoint = '/resources'

const getResources = async (): Promise<ResourcesApiResponse | null> => {
  try {
    const resp = await rest.get(`${endpoint}?depth=0`)

    if (resp.status === 204) {
      return { resource: [], count: 0, offset: 0, totalCount: 0 }
    }

    return resp.data
  } catch (err) {
    return null
  }
}

const getResourceForNode = async (name: string): Promise<Resource | null> => {
  try {
    const resp = await rest.get(`${endpoint}/fornode/${name}`)

    if (resp.status === 204) {
      return null
    }

    return resp.data
  } catch (err) {
    return null
  }
}

export { getResources, getResourceForNode }
