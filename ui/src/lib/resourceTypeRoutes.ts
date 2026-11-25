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

/** Paths of the resource types pages; ids are encoded once, which Vue Router undoes when it reads the params. */
const encode = (id: string) => encodeURIComponent(id)

export const RESOURCE_TYPES_PATH = '/resource-types'
export const CONFIG_CREATE_PATH = '/resource-types/create-config'

export const configPath = (id: string) => `/resource-types/config/${encode(id)}`
export const configEditPath = (id: string) => `${configPath(id)}/edit`
export const typeCreatePath = (id: string) => `${configPath(id)}/create-type`
export const typeEditPath = (id: string, index: number) => `${configPath(id)}/type/${index}`

/**
 * Reads a configuration id from a route param. Vue Router has already decoded it; decoding it again would
 * change the id.
 */
export const idFromParam = (param: unknown) => String(param ?? '')

/** Reads an index from a route param; -1 for a create route. */
export const indexFromParam = (param: unknown) => (param === undefined || param === '' ? -1 : Number(param))
