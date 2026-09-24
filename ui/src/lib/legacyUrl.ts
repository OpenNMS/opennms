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

import { getActivePinia } from 'pinia'

import { useMenuStore } from '@/stores/menuStore'

// The legacy JSP pages live one level up from /ui, e.g. /opennms/admin/...
const LEGACY_BASE = import.meta.env.BASE_URL.replace(/ui\/?$/, '')

// the server's baseHref wins once the menu has loaded; before that (or with no
// store at all) the build-time base is the same path for a default deployment
const legacyBase = (): string => {
  if (!getActivePinia()) {
    return LEGACY_BASE
  }
  const baseHref = useMenuStore().mainMenu?.baseHref
  return typeof baseHref === 'string' && baseHref ? baseHref : LEGACY_BASE
}

export const legacyUrl = (path: string): string => `${legacyBase()}${path}`
