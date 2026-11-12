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
  JdbcCollection,
  JdbcCollectionErrors,
  JdbcColumn,
  JdbcColumnErrors,
  JdbcQuery,
  JdbcQueryErrors,
  JdbcSource,
  JdbcSourceErrors
} from '@/types/jdbcDataCollection'

/*
 * Mirrors JdbcDataCollectionConfigValidator of the REST service; keep the two in step. The server checks again,
 * so these only have to catch what a user can fix in the form before saving.
 */

/** RRDtool cannot store a datasource name longer than this; the alias of a column becomes that name. */
export const MAX_ALIAS_LENGTH = 19

export const SOURCE_ID_PATTERN = /^[A-Za-z0-9][A-Za-z0-9._-]*$/

export const RRA_PATTERN = /^RRA:(AVERAGE|MIN|MAX|LAST):\d*\.?\d+:\d+:\d+$/

/** The id that holds the former jdbc-datacollection-config.xml; it cannot be deleted. */
export const DEFAULT_SOURCE_ID = 'default'

const isBlank = (value?: string | null): boolean => !value || value.trim().length === 0

/** True when at least one field of the error bag is set; nested column errors count too. */
export const hasErrors = (errors: object): boolean =>
  Object.values(errors).some(value => (Array.isArray(value) ? value.some(entry => hasErrors(entry)) : !!value))

export const validateSource = (source: JdbcSource, existingIds: string[]): JdbcSourceErrors => {
  const errors: JdbcSourceErrors = {}
  const id = source.id?.trim() ?? ''

  if (!id) {
    errors.id = 'Id is required.'
  } else if (!SOURCE_ID_PATTERN.test(id)) {
    errors.id = 'Start with a letter or digit and use only letters, digits, \'.\', \'_\' and \'-\'.'
  } else if (existingIds.includes(id)) {
    errors.id = `A source with id '${id}' already exists.`
  }
  return errors
}

/**
 * @param takenNames collection names used anywhere else, in this source or another one
 */
export const validateCollection = (collection: JdbcCollection, takenNames: string[]): JdbcCollectionErrors => {
  const errors: JdbcCollectionErrors = {}
  const name = collection.name?.trim() ?? ''

  if (!name) {
    errors.name = 'Name is required.'
  } else if (takenNames.includes(name)) {
    errors.name = `A collection named '${name}' already exists. Collection names must be unique across all sources.`
  }

  if (collection.rrdStep === null || collection.rrdStep === undefined || !Number.isInteger(collection.rrdStep) || collection.rrdStep < 1) {
    errors.rrdStep = 'Step must be a positive number of seconds.'
  }

  if (!collection.rras.length) {
    errors.rras = 'At least one RRA is required.'
  } else {
    const invalid = collection.rras.find(rra => !RRA_PATTERN.test(rra.trim()))
    if (invalid !== undefined) {
      errors.rras = `'${invalid}' is not of the form RRA:<AVERAGE|MIN|MAX|LAST>:<xff>:<steps>:<rows>.`
    }
  }
  return errors
}

export const validateColumn = (column: JdbcColumn, types: string[]): JdbcColumnErrors => {
  const errors: JdbcColumnErrors = {}

  if (isBlank(column.name)) {
    errors.name = 'Name is required.'
  }
  if (isBlank(column.type) || (types.length > 0 && !types.includes(column.type.trim().toLowerCase()))) {
    errors.type = 'Type is required.'
  }
  if (isBlank(column.alias)) {
    errors.alias = 'Alias is required.'
  } else if (column.alias.trim().length > MAX_ALIAS_LENGTH) {
    errors.alias = `At most ${MAX_ALIAS_LENGTH} characters.`
  }
  return errors
}

/**
 * @param otherQueryNames names of the other queries of the collection
 * @param types the column types the server accepts; empty skips that check
 */
export const validateQuery = (query: JdbcQuery, otherQueryNames: string[], types: string[]): JdbcQueryErrors => {
  const errors: JdbcQueryErrors = {}
  const name = query.name?.trim() ?? ''

  if (!name) {
    errors.name = 'Name is required.'
  } else if (otherQueryNames.includes(name)) {
    errors.name = `The collection already has a query named '${name}'.`
  }

  if (query.recheckInterval === null || query.recheckInterval === undefined ||
    !Number.isInteger(query.recheckInterval) || query.recheckInterval < 0) {
    errors.recheckInterval = 'Recheck interval must be 0 or a positive number of milliseconds.'
  }

  if (isBlank(query.ifType)) {
    errors.ifType = 'Interface type is required.'
  }

  if (isBlank(query.queryString)) {
    errors.queryString = 'SQL statement is required.'
  }

  // A query returning several rows needs both to build one resource per row.
  if (isBlank(query.resourceType) !== isBlank(query.instanceColumn)) {
    if (isBlank(query.resourceType)) {
      errors.resourceType = 'Required together with the instance column.'
    } else {
      errors.instanceColumn = 'Required together with the resource type.'
    }
  }

  if (!query.columns.length) {
    errors.columns = 'At least one column is required.'
  }

  const columnErrors = query.columns.map(column => validateColumn(column, types))
  const aliases = query.columns.map(column => column.alias?.trim() ?? '')
  aliases.forEach((alias, index) => {
    if (alias && !columnErrors[index].alias && aliases.indexOf(alias) !== index) {
      columnErrors[index].alias = `Alias '${alias}' is used by another column.`
    }
  })
  if (columnErrors.some(entry => hasErrors(entry))) {
    errors.columnErrors = columnErrors
  }
  return errors
}
