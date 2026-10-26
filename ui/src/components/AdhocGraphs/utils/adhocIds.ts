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

/**
 * The grammar of the identifiers the ad-hoc graph passes around, in one place so
 * the store and the link codec cannot drift apart on it.
 */

const NODE_SEGMENT = /^(?:node|nodeSource)\[([^\]]+)\]/

/**
 * The node a resource belongs to, as `m_nodeDao.get()` accepts it: a bare node id
 * from `node[1].interfaceSnmp[eth0]`, or a foreign-source pair from
 * `nodeSource[Demo:1].interfaceSnmp[eth0]`.
 */
export const nodeCriteriaOf = (resourceId: string): string | null => NODE_SEGMENT.exec(resourceId)?.[1] ?? null

/** The part of a resource id after the node segment: `interfaceSnmp[eth0]`. */
export const resourceShortId = (resourceId: string): string =>
  resourceId.replace(/^(?:node|nodeSource)\[[^\]]*\]\.?/, '')

/** The selection identity of one attribute on one resource. */
export const datasourceKey = (resourceId: string, attribute: string): string => `${resourceId}|${attribute}`

/**
 * The inverse of `datasourceKey`. Split on the LAST separator: a resource id can
 * carry a `|` of its own, an attribute name cannot.
 */
export const splitDatasourceKey = (key: string): { resourceId: string, attribute: string } => {
  const separator = key.lastIndexOf('|')

  if (separator === -1) {
    return { resourceId: key, attribute: '' }
  }

  return { resourceId: key.slice(0, separator), attribute: key.slice(separator + 1) }
}
