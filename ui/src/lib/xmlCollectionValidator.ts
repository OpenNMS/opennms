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

import type { XmlGroup, XmlKeyValue, XmlObject, XmlRrd, XmlSource } from '@/types/xmlCollectionConfig'

// Client copy of the rules in XmlCollectionConfigValidator.java, so the forms can point at the offending field
// before the server refuses the save. Keep the two in step.

/** Group file ids become configuration ids, and used to be file names. */
export const GROUP_FILE_ID_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._-]*$/

/** RRA:<cf>:<xff>:<steps>:<rows> */
export const RRA_PATTERN = /^RRA:(AVERAGE|MIN|MAX|LAST):(0(\.\d+)?|1(\.0+)?|\.\d+):[1-9]\d*:[1-9]\d*$/

const METHOD_PATTERN = /^[A-Za-z]+$/

/** The attribute types and the aliases AttributeType.parse() accepts. */
const ATTRIBUTE_TYPES = ['gauge', 'gauge32', 'integer32', 'counter', 'counter32', 'string']

export type Errors<T extends string> = Partial<Record<T, string>>

export const hasErrors = (errors: object): boolean => Object.values(errors).some(message => !!message)

const isBlank = (value?: string | null) => !value || value.trim().length === 0

export const validateRra = (rra: string): string | undefined => {
  if (isBlank(rra)) {
    return 'The RRA must not be empty.'
  }
  return RRA_PATTERN.test(rra.trim())
    ? undefined
    : `'${rra.trim()}' is invalid, expected RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>, e.g. RRA:AVERAGE:0.5:1:2016.`
}

export type CollectionErrors = Errors<'name' | 'step' | 'rras'>

/** Checks name and RRD settings; existingNames are the names the collection must not take. */
export const validateCollectionBasics = (name: string, rrd: XmlRrd, existingNames: string[]): CollectionErrors => {
  const errors: CollectionErrors = {}

  if (isBlank(name)) {
    errors.name = 'The name is required.'
  } else if (existingNames.includes(name.trim())) {
    errors.name = `A collection named '${name.trim()}' already exists.`
  }

  if (rrd.step === null || rrd.step === undefined || !Number.isInteger(rrd.step) || rrd.step <= 0) {
    errors.step = 'The step must be a positive number of seconds.'
  }

  if (!rrd.rras.length) {
    errors.rras = 'At least one RRA is required.'
  } else {
    errors.rras = rrd.rras.map(validateRra).find(message => !!message)
  }

  return errors
}

export const validateKeyValues = (entries: XmlKeyValue[], what: string): string | undefined => {
  const keys = new Set<string>()

  for (const entry of entries) {
    if (isBlank(entry.key)) {
      return `Every ${what} needs a name.`
    }
    if (keys.has(entry.key.trim())) {
      return `${what[0].toUpperCase()}${what.slice(1)} '${entry.key.trim()}' is defined more than once.`
    }
    keys.add(entry.key.trim())
  }
  return undefined
}

export type SourceErrors = Errors<'url' | 'method' | 'parameters' | 'headers' | 'contentType' | 'importGroups' | 'groups'>

/** otherUrls are the URLs of the other sources of the collection. */
export const validateSource = (source: XmlSource, otherUrls: string[], groupFileIds: string[]): SourceErrors => {
  const errors: SourceErrors = {}

  if (isBlank(source.url)) {
    errors.url = 'The URL is required.'
  } else if (otherUrls.map(url => url.trim()).includes(source.url.trim())) {
    errors.url = 'Another source of this collection already uses this URL.'
  }

  const request = source.request
  if (request) {
    if (!isBlank(request.method) && !METHOD_PATTERN.test(request.method.trim())) {
      errors.method = `'${request.method.trim()}' is not a valid HTTP method.`
    }
    errors.parameters = validateKeyValues(request.parameters, 'request parameter')
    errors.headers = validateKeyValues(request.headers, 'request header')
    if (!isBlank(request.content) && isBlank(request.contentType)) {
      errors.contentType = 'The request content needs a content type.'
    }
  }

  const missing = source.importGroups.filter(id => !groupFileIds.includes(id))
  if (missing.length) {
    errors.importGroups = `Group file ${missing.map(id => `'${id}'`).join(', ')} does not exist.`
  }

  if (!source.importGroups.length && !source.groups.length) {
    errors.groups = 'Define at least one group or import a group file.'
  } else {
    errors.groups = validateGroups(source.groups)
  }

  return errors
}

export type GroupErrors = Errors<'name' | 'resourceType' | 'resourceXpath' | 'objects'>

/** otherNames are the names of the other groups of the same source or group file. */
export const validateGroup = (group: XmlGroup, otherNames: string[] = []): GroupErrors => {
  const errors: GroupErrors = {}

  if (isBlank(group.name)) {
    errors.name = 'The name is required.'
  } else if (otherNames.includes(group.name.trim())) {
    errors.name = `A group named '${group.name.trim()}' already exists here.`
  }
  if (isBlank(group.resourceType)) {
    errors.resourceType = 'The resource type is required.'
  }
  if (isBlank(group.resourceXpath)) {
    errors.resourceXpath = 'The resource XPath is required.'
  }

  if (!group.objects.length) {
    errors.objects = 'At least one object is required.'
  } else {
    const names = group.objects.map(object => object.name.trim())
    group.objects.forEach((object, index) => {
      if (!errors.objects) {
        const message = Object.values(validateObject(object, names.filter((_, i) => i !== index))).find(m => !!m)
        errors.objects = message ? `Object ${index + 1}: ${message}` : undefined
      }
    })
  }

  return errors
}

/** The first problem of any group in the list, or undefined. */
export const validateGroups = (groups: XmlGroup[]): string | undefined => {
  const names = groups.map(group => group.name.trim())

  for (let index = 0; index < groups.length; index++) {
    const message = Object.values(validateGroup(groups[index], names.filter((_, i) => i !== index))).find(m => !!m)
    if (message) {
      return `Group '${groups[index].name || index + 1}': ${message}`
    }
  }
  return undefined
}

export type ObjectErrors = Errors<'name' | 'type' | 'xpath' | 'mappings'>

export const validateObject = (object: XmlObject, otherNames: string[] = []): ObjectErrors => {
  const errors: ObjectErrors = {}

  if (isBlank(object.name)) {
    errors.name = 'The name is required.'
  } else if (otherNames.includes(object.name.trim())) {
    errors.name = `'${object.name.trim()}' is defined more than once.`
  }
  if (isBlank(object.type)) {
    errors.type = 'The type is required.'
  } else if (!ATTRIBUTE_TYPES.includes(object.type.trim().toLowerCase())) {
    errors.type = `'${object.type.trim()}' is invalid, expected gauge, counter or string.`
  }
  if (isBlank(object.xpath)) {
    errors.xpath = 'The XPath is required.'
  }

  const froms = new Set<string>()
  for (const mapping of object.mappings) {
    if (isBlank(mapping.to)) {
      errors.mappings = 'Every value mapping needs a value to map to.'
      break
    }
    const from = (mapping.from ?? '').trim()
    if (froms.has(from)) {
      errors.mappings = from ? `Value '${from}' is mapped more than once.` : 'Only one value mapping may leave "from" empty.'
      break
    }
    froms.add(from)
  }

  return errors
}

export const validateGroupFileId = (id: string, existingIds: string[]): string | undefined => {
  if (isBlank(id)) {
    return 'The id is required.'
  }
  if (!GROUP_FILE_ID_PATTERN.test(id.trim())) {
    return 'The id may only contain letters, digits, ".", "_" and "-", and must start with a letter or digit.'
  }
  if (existingIds.includes(id.trim())) {
    return `A group file with id '${id.trim()}' already exists.`
  }
  return undefined
}
