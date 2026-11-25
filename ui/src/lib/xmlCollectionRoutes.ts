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

import type { BreadCrumb } from '@/types'
import type { MainMenu } from '@/types/mainMenu'

/** Paths of the XML collector configuration pages; names are encoded once, which Vue Router undoes when it reads the params. */
const encode = (name: string) => encodeURIComponent(name)

export const XML_COLLECTION_HOME = '/xml-collection-config'
export const COLLECTIONS_TAB_PATH = `${XML_COLLECTION_HOME}?tab=collections`
export const GROUP_FILES_TAB_PATH = `${XML_COLLECTION_HOME}?tab=group-files`
export const COLLECTION_CREATE_PATH = `${XML_COLLECTION_HOME}/create-collection`
export const GROUP_FILE_CREATE_PATH = `${XML_COLLECTION_HOME}/create-group-file`

export const collectionPath = (name: string) => `${XML_COLLECTION_HOME}/collection/${encode(name)}`
export const collectionEditPath = (name: string) => `${collectionPath(name)}/edit`
export const sourceCreatePath = (name: string) => `${collectionPath(name)}/source/create`
export const sourceEditPath = (name: string, index: number) => `${collectionPath(name)}/source/${index}/edit`
export const groupFilePath = (id: string) => `${XML_COLLECTION_HOME}/group-file/${encode(id)}`

/** Reads a name from a route param. Vue Router has already decoded it; decoding again would corrupt names with '%'. */
export const nameFromParam = (param: unknown) => String(param ?? '')

/** Reads an index from a route param; -1 for a create route. */
export const indexFromParam = (param: unknown) => (param === undefined || param === '' ? -1 : Number(param))

/**
 * Breadcrumbs of the XML collector pages: Home, Admin (the only entry point, these pages are not in the menu),
 * the overview, then the given trail. The last crumb is the current page.
 */
export const xmlBreadcrumbs = (menu: MainMenu | undefined, trail: { label: string, to?: string }[]): BreadCrumb[] => {
  const crumbs: BreadCrumb[] = [
    { label: 'Home', to: menu?.homeUrl ?? '', isAbsoluteLink: true },
    { label: 'Admin', to: `${menu?.baseHref ?? ''}admin/index.jsp`, isAbsoluteLink: true },
    { label: 'XML Data Collection Configuration', to: XML_COLLECTION_HOME },
    ...trail.map(crumb => ({ label: crumb.label, to: crumb.to ?? '#' }))
  ]
  const last = crumbs[crumbs.length - 1]
  crumbs[crumbs.length - 1] = { ...last, to: '#', position: 'last' }
  return crumbs
}
