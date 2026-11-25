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
  ResourceTypeDefinition,
  ResourceTypeErrors,
  ResourceTypeParameter,
  ResourceTypesConfigErrors,
  ResourceTypesMetadata,
  ResourceTypeStrategy
} from '@/types/resourceTypesConfig'

/** The same rules as ResourceTypesConfigValidator on the server, so a save only fails on what the client can't know. */
const CONFIG_ID = /^[A-Za-z0-9][A-Za-z0-9._-]*$/
// Resource type names become directory names below the RRD repository.
const TYPE_NAME = /^[^\s/\\]+$/
const CLASS_NAME = /^[A-Za-z_$][\w$]*(\.[A-Za-z_$][\w$]*)*$/

const isBlank = (value?: string) => !value || !value.trim()

export const hasErrors = (errors: object): boolean => Object.values(errors).some(message => !!message)

/**
 * @param existingIds the ids of the other configurations; on rename the configuration's own id is left out
 */
export const validateConfigId = (id: string, existingIds: string[]): ResourceTypesConfigErrors => {
  const errors: ResourceTypesConfigErrors = {}
  const trimmed = id.trim()

  if (!trimmed) {
    errors.id = 'Name is required.'
  } else if (!CONFIG_ID.test(trimmed)) {
    errors.id = 'Use letters, digits, \'.\', \'_\' and \'-\', starting with a letter or digit.'
  } else if (existingIds.includes(trimmed)) {
    errors.id = `A configuration named '${trimmed}' already exists.`
  }
  return errors
}

const validateParameters = (parameters: ResourceTypeParameter[]): string | undefined => {
  for (const parameter of parameters) {
    if (isBlank(parameter.key)) {
      return 'Every parameter needs a key.'
    }
    // The configuration manager drops empty values, which then fail its schema check.
    if (!parameter.value) {
      return `Parameter '${parameter.key.trim()}' needs a value.`
    }
  }
  return undefined
}

const validateClassName = (strategy: ResourceTypeStrategy): string | undefined => {
  if (isBlank(strategy.className)) {
    return 'The class is required.'
  }
  if (!CLASS_NAME.test(strategy.className.trim())) {
    return 'Not a valid Java class name.'
  }
  return undefined
}

/**
 * @param takenNames names defined elsewhere: by the other types of the configuration and by the other configurations
 */
export const validateResourceType = (type: ResourceTypeDefinition, takenNames: string[]): ResourceTypeErrors => {
  const errors: ResourceTypeErrors = {}
  const name = type.name.trim()

  if (!name) {
    errors.name = 'Name is required.'
  } else if (!TYPE_NAME.test(name)) {
    errors.name = 'The name must not contain blanks, \'/\' or \'\\\'.'
  } else if (takenNames.includes(name)) {
    errors.name = `A resource type named '${name}' already exists.`
  }

  if (isBlank(type.label)) {
    errors.label = 'Label is required.'
  }

  errors.persistenceSelectorStrategy = validateClassName(type.persistenceSelectorStrategy)
  errors.storageStrategy = validateClassName(type.storageStrategy)
  errors.persistenceSelectorParameters = validateParameters(type.persistenceSelectorStrategy.parameters)
  errors.storageParameters = validateParameters(type.storageStrategy.parameters)
  return errors
}

/** Not an error: explains which definition is used when the name is also defined outside of the stored configurations. */
export const overrideWarning = (name: string, metadata: ResourceTypesMetadata | null): string | undefined => {
  const trimmed = name.trim()

  if (!trimmed || !metadata) {
    return undefined
  }
  if (metadata.extensionTypeNames.includes(trimmed)) {
    return `An extension provides a resource type named '${trimmed}'. The extension's definition is used, not this one.`
  }
  if (metadata.dataCollectionTypeNames.includes(trimmed)) {
    return `The data collection configuration defines a resource type named '${trimmed}'. This definition replaces it.`
  }
  return undefined
}
