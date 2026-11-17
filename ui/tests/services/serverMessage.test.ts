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
import { serverMessage } from '@/services/serverMessage'

const failure = (data: unknown, contentType?: string) =>
  ({ response: { status: 400, data, headers: contentType ? { 'content-type': contentType } : {}}})

describe('serverMessage', () => {
  it('returns a plain-text explanation, trimmed', () => {
    expect(serverMessage(failure('  Group \'x\' already exists.\n', 'text/plain;charset=UTF-8'), 'fallback')).toBe('Group \'x\' already exists.')
  })

  it('accepts a string under the JSON type and a body without a type', () => {
    expect(serverMessage(failure('Unknown category: Edge', 'application/json'), 'fallback')).toBe('Unknown category: Edge')
    expect(serverMessage(failure('Unknown category: Edge'), 'fallback')).toBe('Unknown category: Edge')
  })

  it('never returns an HTML error page, whatever its type', () => {
    const page = '<html><head><title>Error 502 Bad Gateway</title></head><body><h2>HTTP ERROR 502</h2></body></html>'
    expect(serverMessage(failure(page, 'text/html;charset=iso-8859-1'), 'Failed to load users.')).toBe('Failed to load users.')
    expect(serverMessage(failure(page), 'Failed to load users.')).toBe('Failed to load users.')
    expect(serverMessage(failure(`\n  ${page}`, 'text/plain'), 'Failed to load users.')).toBe('Failed to load users.')
  })

  it('falls back for another type, an empty or non-string body, and no response', () => {
    expect(serverMessage(failure('Gateway Timeout', 'text/html'), 'fallback')).toBe('fallback')
    expect(serverMessage(failure('   ', 'text/plain'), 'fallback')).toBe('fallback')
    expect(serverMessage(failure({ message: 'x' }, 'application/json'), 'fallback')).toBe('fallback')
    expect(serverMessage(new Error('Network Error'), 'fallback')).toBe('fallback')
    expect(serverMessage(undefined, 'fallback')).toBe('fallback')
  })
})
