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

import type {
  HttpAttrib,
  HttpCollection,
  HttpCollectionErrors,
  HttpCollectionSettings,
  HttpSettingsErrors,
  HttpUri,
  HttpUriErrors,
  HttpUrl
} from '@/types/httpCollectionConfig'

// Mirrors HttpCollectionConfigValidator in the REST service, which checks the same rules again.

/** RRDtool cannot store a datasource name longer than this. */
export const MAX_DS_NAME_LENGTH = 19

/** Mirrors the rra pattern in http-datacollection-config.xsd, anchored as XSD patterns implicitly are. */
export const RRA_PATTERN = /^RRA:(AVERAGE|MIN|MAX|LAST):.*$/

const RESPONSE_RANGE_PATTERN = /^\s*(\d+)\s*-\s*(\d+)\s*$/

const ATTRIBUTE_TYPE_ALIASES: Record<string, string> = {
  gauge: 'gauge',
  gauge32: 'gauge',
  integer32: 'gauge',
  counter: 'counter',
  counter32: 'counter',
  string: 'string'
}

const isBlank = (value?: string | null): boolean => !value || value.trim().length === 0

/** True when at least one field of the error bag is set. */
export const hasErrors = (errors: object): boolean => Object.values(errors).some(message => !!message)

/** The collector looks collections up ignoring case, so names that differ only in case clash. */
export const sameCollectionName = (a?: string | null, b?: string | null): boolean =>
  (a ?? '').trim().toLowerCase() === (b ?? '').trim().toLowerCase()

export const validateSettings = (settings: HttpCollectionSettings): HttpSettingsErrors => {
  const errors: HttpSettingsErrors = {}

  if (isBlank(settings.rrdRepository)) {
    errors.rrdRepository = 'RRD repository is required.'
  }
  return errors
}

/**
 * @param existingNames names of the other collections; the collection's own original name must not be among
 *   them, or an unchanged name would be reported as taken
 */
export const validateCollection = (collection: HttpCollection, existingNames: string[] = []): HttpCollectionErrors => {
  const errors: HttpCollectionErrors = {}

  if (isBlank(collection.name)) {
    errors.name = 'Name is required.'
  } else if (existingNames.some(name => sameCollectionName(name, collection.name))) {
    errors.name = 'A collection with this name already exists.'
  }

  const step = collection.rrd?.step
  if (step === null || step === undefined || !Number.isInteger(step) || step < 1) {
    errors.step = 'Step must be a positive number of seconds.'
  }

  const rras = collection.rrd?.rras ?? []
  if (rras.length === 0) {
    errors.rras = 'At least one RRA is required.'
  } else {
    const invalid = rras.find(rra => !RRA_PATTERN.test((rra ?? '').trim()))
    if (invalid !== undefined) {
      errors.rras = `'${invalid}' must have the form RRA:(AVERAGE|MIN|MAX|LAST):xff:steps:rows.`
    }
  }
  return errors
}

/** The JavaScript counterpart of the Java regex flags the collector compiles matches with, where one exists. */
const jsFlags = (url: HttpUrl): string => {
  let flags = ''
  if (url.caseInsensitive) {
    flags += 'i'
  }
  if (url.dotall) {
    flags += 's'
  }
  if (url.multiline) {
    flags += 'm'
  }
  return flags
}

/**
 * The number of groups in matches, or null when it cannot be told here: Java and JavaScript regular expressions
 * differ (possessive quantifiers, the COMMENTS flag, ...), so an expression JavaScript rejects is left to the
 * server rather than reported as wrong.
 */
export const groupCount = (url: HttpUrl): number | null => {
  const matches = isBlank(url.matches) ? '(.*)' : url.matches as string

  if (url.literal) {
    return 0
  }
  if (url.comments) {
    return null
  }
  try {
    // An alternative that matches the empty string tells how many groups the expression has.
    const result = new RegExp(`${matches}|`, jsFlags(url)).exec('')
    return result ? result.length - 1 : null
  } catch {
    return null
  }
}

export const validateAttrib = (attrib: HttpAttrib, groups: number | null): string | undefined => {
  if (isBlank(attrib.alias)) {
    return 'Every attribute needs an alias.'
  }
  if (attrib.alias.trim().length > MAX_DS_NAME_LENGTH) {
    return `Alias '${attrib.alias.trim()}' is longer than ${MAX_DS_NAME_LENGTH} characters.`
  }
  if (attrib.matchGroup === null || attrib.matchGroup === undefined || !Number.isInteger(attrib.matchGroup) || attrib.matchGroup < 1) {
    return `Match group of '${attrib.alias.trim()}' must be 1 or greater.`
  }
  if (groups !== null && attrib.matchGroup > groups) {
    return `Match group ${attrib.matchGroup} of '${attrib.alias.trim()}' does not exist; the expression has ${groups} ${groups === 1 ? 'group' : 'groups'}.`
  }
  if (!ATTRIBUTE_TYPE_ALIASES[(attrib.type ?? '').trim().toLowerCase()]) {
    return `Type of '${attrib.alias.trim()}' must be gauge, counter or string.`
  }
  return undefined
}

/**
 * @param otherUriNames names of the other URIs of the collection
 */
export const validateUri = (uri: HttpUri, otherUriNames: string[] = []): HttpUriErrors => {
  const errors: HttpUriErrors = {}
  const url = uri.url

  if (isBlank(uri.name)) {
    errors.name = 'Name is required.'
  } else if (otherUriNames.includes(uri.name.trim())) {
    errors.name = 'Another URI of this collection has this name.'
  }

  if (isBlank(url.path)) {
    errors.path = 'Path is required.'
  }

  if (url.port !== null && url.port !== undefined && (!Number.isInteger(url.port) || url.port < 1 || url.port > 65535)) {
    errors.port = 'Port must be between 1 and 65535.'
  }

  if (!isBlank(url.responseRange)) {
    const match = RESPONSE_RANGE_PATTERN.exec(url.responseRange as string)
    if (!match || Number(match[1]) > Number(match[2])) {
      errors.responseRange = 'Response range must have the form low-high, such as 100-399.'
    }
  }

  if ((url.parameters ?? []).some(parameter => isBlank(parameter.key) || isBlank(parameter.value))) {
    errors.parameters = 'Every parameter needs a key and a value.'
  }

  if ((url.headers ?? []).some(header => isBlank(header.name) || isBlank(header.value))) {
    errors.headers = 'Every header needs a name and a value.'
  }

  const attributes = uri.attributes ?? []
  const groups = groupCount(url)
  const aliases = new Set<string>()
  for (const attrib of attributes) {
    const message = validateAttrib(attrib, groups)
    if (message) {
      errors.attributes = message
      break
    }
    if (aliases.has(attrib.alias.trim())) {
      errors.attributes = `Alias '${attrib.alias.trim()}' is used more than once.`
      break
    }
    aliases.add(attrib.alias.trim())
  }
  return errors
}
