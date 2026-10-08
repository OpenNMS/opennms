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

import type { ThresholdDefinitionKind } from '@/lib/thresholdValidator'
import type { RouteLocationNormalized } from 'vue-router'

/** Paths of the threshold configuration pages; names are encoded the same way the detail pages decode them. */
const encode = (name: string) => encodeURIComponent(name)

export const GROUPS_TAB_PATH = '/threshold-config?tab=groups'
export const PACKAGES_TAB_PATH = '/threshold-config?tab=packages'
export const GROUP_CREATE_PATH = '/threshold-config/create-group'
export const PACKAGE_CREATE_PATH = '/threshold-config/create-package'

export const groupPath = (name: string) => `/threshold-config/group/${encode(name)}`
export const groupEditPath = (name: string) => `${groupPath(name)}/edit`
export const definitionCreatePath = (name: string, kind: ThresholdDefinitionKind) => `${groupPath(name)}/${kind}/create`
export const definitionEditPath = (name: string, kind: ThresholdDefinitionKind, index: number) =>
  `${groupPath(name)}/${kind}/${index}/edit`

export const packagePath = (name: string) => `/threshold-config/package/${encode(name)}`
export const serviceCreatePath = (name: string) => `${packagePath(name)}/service/create`
export const serviceEditPath = (name: string, index: number) => `${packagePath(name)}/service/${index}/edit`

/** Reads a group or package name from a route param. */
export const nameFromParam = (param: unknown) => decodeURIComponent(String(param ?? ''))

/** Reads an index from a route param; -1 for a create route. */
export const indexFromParam = (param: unknown) => (param === undefined || param === '' ? -1 : Number(param))

const SERVICE_ROUTE_NAMES = ['Threshold Service Create', 'Threshold Service Edit']

/** Whether a route is a service page of the given package. */
export const isServicePageOf = (to: RouteLocationNormalized, packageName: string | null) =>
  !!packageName && SERVICE_ROUTE_NAMES.includes(String(to.name)) && nameFromParam(to.params.name) === packageName

/** Whether a route is the detail page of the given package. */
export const isPackagePageOf = (to: RouteLocationNormalized, packageName: string | null) =>
  !!packageName && to.name === 'Threshold Package Detail' && nameFromParam(to.params.name) === packageName
