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
const EMAIL_SHAPE = /^[^\s@]+@[^\s@]+$/

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

/** Loose shape check: notification delivery needs at least local@domain. */
export const validateEmailShape = (value: string, label: string): string | null => {
  const trimmed = value.trim()
  if (trimmed && !EMAIL_SHAPE.test(trimmed)) {
    return `The ${label} must look like an email address (name@domain).`
  }
  return null
}
