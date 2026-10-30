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

const INVALID_NAME = /[&<>"`':/\\%?#\s\p{Z}\p{C}]/u
const INVALID_COMMENTS = /[&<>"`']/
// UserMgmtValidation.FREE_TEXT_LENGTH: comments longer than this are rejected, counted in code points once trimmed
export const FREE_TEXT_LENGTH = 4096
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
    return `The ${label} must not contain markup, whitespace, control or invisible characters, or the characters : / \\ % ? #`
  }
  if (trimmed === '.' || trimmed === '..') {
    return `The ${label} must not be a dot segment.`
  }
  return null
}

/**
 * A new or renamed user-id or group name that equals a reserved name ignoring case, but not exactly,
 * would be mistaken for the system entry; the server refuses it. The exact name is not flagged here:
 * it is a duplicate, which the server reports as such.
 */
export const validateReservedName = (value: string, reserved: string[], label: 'user-id' | 'group name'): string | null => {
  const trimmed = value.trim()
  const lower = trimmed.toLowerCase()
  if (reserved.some(name => name.toLowerCase() === lower && name !== trimmed)) {
    return `The ${label} "${trimmed}" is reserved.`
  }
  return null
}

/**
 * User and group comments may not contain HTML markup characters nor exceed FREE_TEXT_LENGTH
 * characters. Callers skip this for a stored value sent back unchanged, as the server does.
 */
export const validateAdminComments = (value: string): string | null => {
  if (value && INVALID_COMMENTS.test(value)) {
    return 'The comments must not contain the characters & < > " ` \''
  }
  // the server counts code points, not UTF-16 units. JS trim() also strips Unicode whitespace such as
  // NBSP, which Java's String.trim() keeps, so the counts can differ for such padding; the server decides.
  if (Array.from(value.trim()).length > FREE_TEXT_LENGTH) {
    return `The comments must not be longer than ${FREE_TEXT_LENGTH} characters.`
  }
  return null
}

/**
 * Names containing / \ or % cannot be addressed as a URL path segment (the
 * security filter rejects their encoded forms), so per-item API operations
 * are unavailable for such hand-edited legacy entries. "." and ".." are
 * normalised away by the browser, so their item URL would hit the collection.
 */
export const isPathAddressable = (name: string): boolean => !/[/\\%]/.test(name) && name !== '.' && name !== '..'

/** Field hint for a name that fails isPathAddressable. */
export const UNADDRESSABLE_NAME_HINT = 'The name must not contain / \\ or %'

/**
 * Loose shape check: every comma-separated recipient must contain a
 * local@domain somewhere, which also accepts RFC-5322 display-name forms
 * like `Bill Smith <bill@example.com>`.
 */
// The mismatch message of a password and its confirmation, for the create form and the change-password
// dialog. An empty confirmation is not a mismatch yet: the user has not typed it.
export const validatePasswordConfirmation = (password: string, confirmation: string): string | null =>
  confirmation && password !== confirmation ? 'Passwords do not match' : null

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
