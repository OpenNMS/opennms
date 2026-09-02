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

import {
  WsmanAgentSettings,
  WsmanConfig,
  WsmanConfigInput,
  WsmanDefinition,
  WsmanDefinitionInput,
  WsmanSettingsInput
} from '@/types/wsmanAdmin'

// Form model for the settings shared by the defaults and every definition.
// Booleans are tri-state because "not set" is meaningful: the built-in default
// on the root element, or "inherit from the defaults" on a definition.
export type TriState = 'unset' | 'true' | 'false'

export interface WsmanSettingsForm {
  username: string
  password: string
  clearPassword: boolean
  ssl: TriState
  strictSsl: TriState
  gssAuth: TriState
  port: number | null
  timeout: number | null
  retry: number | null
  maxElements: number | null
  path: string
  productVendor: string
  productVersion: string
}

export const TRI_STATE_OPTIONS = (unsetLabel: string) => [
  { label: unsetLabel, value: 'unset' },
  { label: 'Yes', value: 'true' },
  { label: 'No', value: 'false' }
]

const toTri = (value: boolean | null | undefined): TriState => (value === null || value === undefined ? 'unset' : value ? 'true' : 'false')
const fromTri = (value: TriState): boolean | null => (value === 'unset' ? null : value === 'true')

export const settingsToForm = (s: WsmanAgentSettings): WsmanSettingsForm => ({
  username: s.username ?? '',
  password: '',
  clearPassword: false,
  ssl: toTri(s.ssl),
  strictSsl: toTri(s.strictSsl),
  gssAuth: toTri(s.gssAuth),
  port: s.port ?? null,
  timeout: s.timeout ?? null,
  retry: s.retry ?? null,
  maxElements: s.maxElements ?? null,
  path: s.path ?? '',
  productVendor: s.productVendor ?? '',
  productVersion: s.productVersion ?? ''
})

export const emptySettingsForm = (): WsmanSettingsForm => settingsToForm({
  retry: null, timeout: null, username: null, hasPassword: false, port: null, maxElements: null,
  ssl: null, strictSsl: null, path: null, productVendor: null, productVersion: null, gssAuth: null
})

const blankToNull = (s: string): string | null => (s.trim() ? s.trim() : null)

export const formToInput = (f: WsmanSettingsForm): WsmanSettingsInput => ({
  username: blankToNull(f.username),
  // an empty password field means "keep what is stored"
  password: f.password ? f.password : null,
  clearPassword: f.clearPassword,
  ssl: fromTri(f.ssl),
  strictSsl: fromTri(f.strictSsl),
  gssAuth: fromTri(f.gssAuth),
  port: f.port,
  timeout: f.timeout,
  retry: f.retry,
  maxElements: f.maxElements,
  path: blankToNull(f.path),
  productVendor: blankToNull(f.productVendor),
  productVersion: blankToNull(f.productVersion)
})

// Mirrors the server rules so the dialog can flag problems before submitting.
export const validateSettingsForm = (f: WsmanSettingsForm): Partial<Record<keyof WsmanSettingsForm, string>> => {
  const errors: Partial<Record<keyof WsmanSettingsForm, string>> = {}
  if (f.port !== null && (!Number.isInteger(f.port) || f.port < 1 || f.port > 65535)) {
    errors.port = 'The port must be between 1 and 65535.'
  }
  if (f.timeout !== null && (!Number.isInteger(f.timeout) || f.timeout < 1)) {
    errors.timeout = 'The timeout must be at least 1 millisecond.'
  }
  if (f.retry !== null && (!Number.isInteger(f.retry) || f.retry < 0)) {
    errors.retry = 'Retries must be 0 or more.'
  }
  if (f.maxElements !== null && (!Number.isInteger(f.maxElements) || f.maxElements < 1)) {
    errors.maxElements = 'Max elements must be at least 1.'
  }
  const path = f.path.trim()
  if (path && (!path.startsWith('/') || /\s/.test(path))) {
    errors.path = 'The path must start with / and contain no whitespace.'
  }
  if (f.clearPassword && f.password) {
    errors.password = 'Enter a new password or clear it, not both.'
  }
  return errors
}

// The unchanged definitions of the loaded config, each carrying its index so
// the server keeps its stored password.
export const definitionToInput = (d: WsmanDefinition, sourceIndex: number | null): WsmanDefinitionInput => ({
  ...formToInput(settingsToForm(d)),
  ranges: d.ranges.map(r => ({ ...r })),
  specifics: [...d.specifics],
  ipMatches: [...d.ipMatches],
  sourceIndex
})

export const configToInput = (c: WsmanConfig): WsmanConfigInput => ({
  defaults: formToInput(settingsToForm(c.defaults)),
  definitions: c.definitions.map((d, i) => definitionToInput(d, i))
})

// --- address checks (shape only; the server does the authoritative parse) ---

const IPV4 = /^(\d{1,3})\.(\d{1,3})\.(\d{1,3})\.(\d{1,3})$/

export const isIpAddress = (s: string): boolean => {
  const v = s.trim()
  const m = IPV4.exec(v)
  if (m) {
    return m.slice(1).every(o => Number(o) <= 255)
  }
  return v.includes(':') && /^[0-9a-fA-F:.]+$/.test(v) && v.length >= 3
}

export const isIpv4 = (s: string): boolean => IPV4.test(s.trim())

const ipv4ToNumber = (s: string): number =>
  s.trim().split('.').reduce((acc, o) => acc * 256 + Number(o), 0)

// null when fine, else the problem
export const rangeProblem = (begin: string, end: string): string | null => {
  if (!isIpAddress(begin) || !isIpAddress(end)) {
    return 'Both ends of a range must be valid IP addresses.'
  }
  if (isIpv4(begin) !== isIpv4(end)) {
    return 'A range must not mix IPv4 and IPv6 addresses.'
  }
  if (isIpv4(begin) && ipv4ToNumber(begin) > ipv4ToNumber(end)) {
    return 'The end of a range must not be before its beginning.'
  }
  return null
}

// IPLIKE: four dotted fields (IPv4) or colon-separated fields (IPv6), each a
// number, *, a-b range, or a comma list of those.
const IPLIKE_FIELD = '(\\*|[0-9a-fA-F]+(-[0-9a-fA-F]+)?)(,(\\*|[0-9a-fA-F]+(-[0-9a-fA-F]+)?))*'
const IPLIKE_V4 = new RegExp(`^${IPLIKE_FIELD}(\\.${IPLIKE_FIELD}){3}$`)
const IPLIKE_V6 = new RegExp(`^${IPLIKE_FIELD}(:${IPLIKE_FIELD}){7}$`)

export const isIplikePattern = (s: string): boolean => {
  const v = s.trim()
  return v.includes(':') ? IPLIKE_V6.test(v) : IPLIKE_V4.test(v) && !/[a-fA-F]/.test(v)
}
