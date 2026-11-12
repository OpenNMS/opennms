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
import router from '@/main/router'
import {
  definitionEditPath,
  groupEditPath,
  groupPath,
  indexFromParam,
  nameFromParam,
  packagePath,
  serviceEditPath
} from '@/lib/thresholdRoutes'
import { ThresholdDefinitionKind } from '@/lib/thresholdValidator'

const NAMES = ['CPU Load', 'CPU 90%', 'CPU%20load', 'a/b', 'create']

/** Resolves a path against the app's own routes; resolving runs no guards. */
const resolve = (path: string) => router.resolve(path)

describe('nameFromParam', () => {
  it('returns the param as it is, because the router has already decoded it', () => {
    expect(nameFromParam('CPU 90%')).toBe('CPU 90%')
    expect(nameFromParam('CPU%20load')).toBe('CPU%20load')
    expect(nameFromParam(undefined)).toBe('')
  })
})

describe.each(NAMES)('the paths built for the name %s', (name) => {
  it('reach the group page and read the name back', () => {
    const route = resolve(groupPath(name))

    expect(route.name).toBe('Threshold Group Detail')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the group edit page and read the name back', () => {
    const route = resolve(groupEditPath(name))

    expect(route.name).toBe('Threshold Group Edit')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the definition edit page and read the name and index back', () => {
    const route = resolve(definitionEditPath(name, ThresholdDefinitionKind.Expression, 3))

    expect(route.name).toBe('Threshold Definition Edit')
    expect(nameFromParam(route.params.name)).toBe(name)
    expect(route.params.kind).toBe(ThresholdDefinitionKind.Expression)
    expect(indexFromParam(route.params.index)).toBe(3)
  })

  it('reach the package page and read the name back', () => {
    const route = resolve(packagePath(name))

    expect(route.name).toBe('Threshold Package Detail')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the service edit page and read the name and index back', () => {
    const route = resolve(serviceEditPath(name, 2))

    expect(route.name).toBe('Threshold Service Edit')
    expect(nameFromParam(route.params.name)).toBe(name)
    expect(indexFromParam(route.params.index)).toBe(2)
  })
})
