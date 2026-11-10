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

/** Paths of the JDBC data collection pages; ids and names are encoded the way the pages decode them. */
const encode = (value: string) => encodeURIComponent(value)

export const JDBC_HOME_PATH = '/jdbc-data-collection'
export const JDBC_SOURCES_TAB_PATH = '/jdbc-data-collection?tab=sources'
export const JDBC_IMPORT_TAB_PATH = '/jdbc-data-collection?tab=import'
export const JDBC_SOURCE_CREATE_PATH = '/jdbc-data-collection/create-source'

export const sourcePath = (id: string) => `/jdbc-data-collection/source/${encode(id)}`
export const collectionCreatePath = (id: string) => `${sourcePath(id)}/create-collection`
export const collectionEditPath = (id: string, name: string) => `${sourcePath(id)}/collection/${encode(name)}/edit`
export const queryCreatePath = (id: string, name: string) => `${sourcePath(id)}/collection/${encode(name)}/query/create`
export const queryEditPath = (id: string, name: string, index: number) =>
  `${sourcePath(id)}/collection/${encode(name)}/query/${index}/edit`

/** Reads an id or name from a route param. */
export const valueFromParam = (param: unknown) => decodeURIComponent(String(param ?? ''))

/** Reads an index from a route param; -1 for a create route. */
export const indexFromParam = (param: unknown) => (param === undefined || param === '' ? -1 : Number(param))
