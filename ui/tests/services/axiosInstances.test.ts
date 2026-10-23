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


import { describe, expect, it } from 'vitest'
import { rest, restFile, v2 } from '@/services/axiosInstances'

describe('axiosInstances', () => {
  // without this header, a 401 from /rest or /api/v2 carries WWW-Authenticate: Basic
  // and the browser shows its own login dialog
  it('marks every request as XHR', () => {
    for (const instance of [v2, rest, restFile]) {
      expect(instance.defaults.headers['X-Requested-With']).toBe('XMLHttpRequest')
    }
  })

  it('keeps the multipart content type for file uploads', () => {
    expect(restFile.defaults.headers['Content-Type']).toBe('multipart/form-data')
  })
})
