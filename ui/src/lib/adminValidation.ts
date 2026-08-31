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

// Client-side mirrors of the /api/v2 admin validation rules so forms can flag
// problems before submitting. These must stay in sync with UsersRestService,
// GroupsRestService and OnCallRolesRestService (INVALID_NAME/INVALID_COMMENTS).

const INVALID_NAME = /[&<>"`':/\\%?#\s]/
const INVALID_COMMENTS = /[&<>"`']/
const EMAIL_SHAPE = /[^\s@]+@[^\s@]+/

/**
 * Validates a user-id, group name or on-call role name.
 * Returns a problem description, or null when the value is acceptable.
 * Emptiness is not checked here; required-ness is a per-form concern.
 */
export const validateAdminName = (value: string, label: string): string | null => {
  const trimmed = value.trim()
  if (!trimmed) {
    return null
  }
  if (INVALID_NAME.test(trimmed)) {
    return `The ${label} must not contain markup, whitespace, or the characters : / \\ % ? #`
  }
  if (trimmed === '.' || trimmed === '..') {
    return `The ${label} must not be a dot segment.`
  }
  return null
}

/** Group comments may not contain HTML markup characters. */
export const validateAdminComments = (value: string): string | null => {
  if (value && INVALID_COMMENTS.test(value)) {
    return 'The comments must not contain the characters & < > " ` \''
  }
  return null
}

/**
 * Names containing / \ or % cannot be addressed as a URL path segment (the
 * security filter rejects their encoded forms), so per-item API operations
 * are unavailable for such hand-edited legacy entries.
 */
export const isPathAddressable = (name: string): boolean => !/[/\\%]/.test(name)

/**
 * Loose shape check: every comma-separated recipient must contain a
 * local@domain somewhere, which also accepts RFC-5322 display-name forms
 * like `Bill Smith <bill@example.com>`.
 */
export const validateEmailShape = (value: string, label: string): string | null => {
  const trimmed = value.trim()
  if (!trimmed) {
    return null
  }
  const parts = trimmed.split(',').map(part => part.trim())
  if (parts.some(part => !part || !EMAIL_SHAPE.test(part))) {
    return `The ${label} must look like an email address (name@domain).`
  }
  return null
}

/**
 * notifd delay/interval grammar: TimeConverter.convertToMillis accepts a
 * number with an optional unit suffix us/ms/s/m/h/d — nothing else. It parses
 * these AFTER the notice row is inserted, so a value it rejects persists the
 * notice, schedules nothing, and throws inside eventd's dispatch. Blank is
 * allowed here; callers that require a value check for blank separately.
 */
export const NOTIFD_DURATION_HINT = 'Use a number with an optional unit: us, ms, s, m, h or d (e.g. 30s, 15m, 1h).'

export const isValidNotifdDuration = (value: string | undefined | null): boolean => {
  const trimmed = (value ?? '').trim()
  if (!trimmed) {
    return true
  }
  return /^\d+(\.\d+)?(us|ms|s|m|h|d)?$/i.test(trimmed)
}
