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

import { QueryParameters } from '@/types'

const queryParametersHandler = (queryParameters: QueryParameters, endpoint: string): string => {
  let modifiedEndpoint = endpoint + '?'
  let queryString = ''

  for (const key in queryParameters) {
    queryString = `${queryString}${key}=${(queryParameters as any)[key]}&`
  }

  modifiedEndpoint += queryString

  // remove last useless ampersand
  return modifiedEndpoint.slice(0, -1)
}

export { queryParametersHandler }
