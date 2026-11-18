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

/** Paths of the HTTP collection configuration pages; names are encoded once, which Vue Router undoes when it reads the params. */
const encode = (name: string) => encodeURIComponent(name)

export const HTTP_COLLECTION_ROOT = '/http-collection-config'
export const COLLECTIONS_TAB_PATH = `${HTTP_COLLECTION_ROOT}?tab=collections`
export const COLLECTION_CREATE_PATH = `${HTTP_COLLECTION_ROOT}/create-collection`

export const collectionPath = (name: string) => `${HTTP_COLLECTION_ROOT}/collection/${encode(name)}`
export const collectionEditPath = (name: string) => `${collectionPath(name)}/edit`
export const uriCreatePath = (name: string) => `${collectionPath(name)}/uri/create`
export const uriEditPath = (name: string, index: number) => `${collectionPath(name)}/uri/${index}/edit`

/**
 * Reads a collection name from a route param. Vue Router has already decoded it; decoding it again would
 * throw on a name like 'CPU 90%' and turn the name 'CPU%20load' into another one.
 */
export const nameFromParam = (param: unknown) => String(param ?? '')

/** Reads an index from a route param; -1 for a create route. */
export const indexFromParam = (param: unknown) => (param === undefined || param === '' ? -1 : Number(param))
