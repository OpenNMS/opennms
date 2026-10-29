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
import { describe, expect, it } from 'vitest'
import { adminItemUrl } from '@/services/adminItemUrl'

// what the browser requests for a target: the path, plus the query axios builds from params
const requested = (target: { url: string, params?: Record<string, string> }) => axios.getUri(target)

describe('adminItemUrl', () => {
  it('addresses a plain name by path', () => {
    expect(adminItemUrl('users', 'jdoe')).toEqual({ url: '/users/jdoe' })
    expect(adminItemUrl('groups', 'Ops')).toEqual({ url: '/groups/Ops' })
  })

  it('encodes a path-safe name in the path', () => {
    expect(adminItemUrl('users', 'My User')).toEqual({ url: '/users/My%20User' })
  })

  it('appends the action to the path form', () => {
    expect(adminItemUrl('users', 'jdoe', 'rename')).toEqual({ url: '/users/jdoe/rename' })
    expect(adminItemUrl('users', 'jdoe', 'password')).toEqual({ url: '/users/jdoe/password' })
    expect(adminItemUrl('groups', 'Ops', 'rename')).toEqual({ url: '/groups/Ops/rename' })
  })

  // a backslash, slash or percent sign cannot travel in a path segment through the servlet container
  it.each(['DOMAIN\\jdoe', 'a/b', '50%', '.', '..'])('addresses %s by query', (name) => {
    expect(adminItemUrl('users', name)).toEqual({ url: '/users/item', params: { userId: name }})
    expect(adminItemUrl('groups', name)).toEqual({ url: '/groups/item', params: { name }})
  })

  it('appends the action to the query form', () => {
    expect(adminItemUrl('users', 'DOMAIN\\jdoe', 'rename')).toEqual({ url: '/users/item/rename', params: { userId: 'DOMAIN\\jdoe' }})
    expect(adminItemUrl('users', 'DOMAIN\\jdoe', 'password')).toEqual({ url: '/users/item/password', params: { userId: 'DOMAIN\\jdoe' }})
    expect(adminItemUrl('groups', 'a/b', 'rename')).toEqual({ url: '/groups/item/rename', params: { name: 'a/b' }})
  })

  it('encodes the query value', () => {
    expect(requested(adminItemUrl('users', 'DOMAIN\\jdoe'))).toBe('/users/item?userId=DOMAIN%5Cjdoe')
    expect(requested(adminItemUrl('users', '50%', 'password'))).toBe('/users/item/password?userId=50%25')
    expect(requested(adminItemUrl('groups', 'a/b', 'rename'))).toBe('/groups/item/rename?name=a%2Fb')
  })

  // the path form of these names would reach the endpoint of the same name, not the user or group
  it.each([
    ['users', 'item'],
    ['users', 'available-roles'],
    ['groups', 'item'],
    ['groups', 'available-categories']
  ] as const)('addresses the %s name %s, a reserved segment, by query', (collection, name) => {
    const key = collection === 'users' ? 'userId' : 'name'
    expect(adminItemUrl(collection, name)).toEqual({ url: `/${collection}/item`, params: { [key]: name }})
    expect(adminItemUrl(collection, name, 'rename')).toEqual({ url: `/${collection}/item/rename`, params: { [key]: name }})
  })

  it('reserves each segment only for its own collection', () => {
    expect(adminItemUrl('groups', 'available-roles')).toEqual({ url: '/groups/available-roles' })
    expect(adminItemUrl('users', 'available-categories')).toEqual({ url: '/users/available-categories' })
  })
})
