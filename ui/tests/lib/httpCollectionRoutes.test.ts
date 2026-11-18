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
  collectionEditPath,
  collectionPath,
  indexFromParam,
  nameFromParam,
  uriCreatePath,
  uriEditPath
} from '@/lib/httpCollectionRoutes'

const NAMES = ['Doc Count', 'CPU 90%', 'CPU%20load', 'create', 'a/b', 'a\\b', 'item', '..']

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
  it('reach the collection page and read the name back', () => {
    const route = resolve(collectionPath(name))

    expect(route.name).toBe('HTTP Collection Detail')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the collection edit page and read the name back', () => {
    const route = resolve(collectionEditPath(name))

    expect(route.name).toBe('HTTP Collection Edit')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the URI create page and read the name back', () => {
    const route = resolve(uriCreatePath(name))

    expect(route.name).toBe('HTTP Collection URI Create')
    expect(nameFromParam(route.params.name)).toBe(name)
  })

  it('reach the URI edit page and read the name and index back', () => {
    const route = resolve(uriEditPath(name, 3))

    expect(route.name).toBe('HTTP Collection URI Edit')
    expect(nameFromParam(route.params.name)).toBe(name)
    expect(indexFromParam(route.params.index)).toBe(3)
  })
})
