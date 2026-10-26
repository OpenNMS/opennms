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

// Without this header, a 401 from the REST APIs carries "WWW-Authenticate: Basic" and the
// browser shows its own login dialog (see XRequestedWithAwareBasicAuthEntryPoint.java).
const XHR_HEADERS = { 'X-Requested-With': 'XMLHttpRequest' }

const v2 = axios.create({
  baseURL: import.meta.env.VITE_BASE_V2_URL?.toString() || '/opennms/api/v2',
  withCredentials: true,
  headers: XHR_HEADERS
})

const rest = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true,
  headers: XHR_HEADERS
})

const restFile = axios.create({
  baseURL: import.meta.env.VITE_BASE_REST_URL?.toString() || '/opennms/rest',
  withCredentials: true,
  headers: {
    ...XHR_HEADERS,
    'Content-Type': 'multipart/form-data'
  }
})

export { v2, rest, restFile }
