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

export const CONFIG_HELP = `
A resource type describes one kind of resource that data is collected for, such as a disk, a database or a
tablespace. Data collection groups and thresholds refer to it by its name.

Resource types are kept in configurations. Each file of the former etc/resource-types.d directory became a
configuration of the same name, and you can add your own. A resource type name must be unique across all
configurations.

Deleting or renaming a resource type is not checked against its users: data collection groups and thresholds
that refer to it stop working.
`.trim()

export const RESOURCE_TYPE_HELP = `
The name identifies the resource type. Data collection groups and thresholds refer to it, and it becomes the
name of the directory the data is stored in, so it must not contain blanks or slashes.

The label is shown for the resource type in the web UI. The resource label is shown for each resource of this
type; it is an expression in which \${name} is replaced by the collected value of that name, and \${index} by
the instance of the resource.

The persistence selector strategy decides which resources of this type are stored. PersistAllSelectorStrategy
stores all of them; PersistRegexSelectorStrategy only those matching its match-expression parameter.

The storage strategy decides where the data of a resource is stored and how it is named.
IndexStorageStrategy uses the instance; SiblingColumnStorageStrategy uses the value of another column,
named by its sibling-column-name parameter, and can rewrite it with replace parameters.
`.trim()

export const RESOURCE_TYPE_FIELD_HINTS = {
  name: 'Referenced by data collection groups and thresholds, and used as a directory name.',
  label: 'Shown for the resource type in the web UI.',
  resourceLabel: 'Shown for each resource, e.g. ${ifDescr} (${index}).'
}

const USERS_STOP_WORKING = 'Data collection groups and thresholds that use them stop working; this is not checked.'

export const configDeleteMessage = (id: string, typeNames: string[]) =>
  typeNames.length
    ? `'${id}' and its resource types ${typeNames.join(', ')} will be removed. ${USERS_STOP_WORKING}`
    : `'${id}' will be removed.`

export const typeDeleteMessage = (name: string) =>
  `Resource type '${name}' will be removed. Data collection groups and thresholds that use it stop working; this is not checked.`
