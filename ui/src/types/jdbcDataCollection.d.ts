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

/** A result column of a JDBC query that is stored as an attribute. */
export interface JdbcColumn {
  name: string
  /** Column label in the result set; the name is used when empty. */
  dataSourceName?: string
  /** gauge, counter or string */
  type: string
  /** Name the value is stored under; at most 19 characters. */
  alias: string
}

export interface JdbcQuery {
  name: string
  /** Milliseconds after which the availability of the query is checked again; 0 checks every time. */
  recheckInterval: number
  ifType: string
  resourceType?: string
  instanceColumn?: string
  queryString: string
  columns: JdbcColumn[]
}

export interface JdbcCollection {
  name: string
  rrdStep: number | null
  rras: string[]
  queries: JdbcQuery[]
  /** Read only: collectd services using the collection, as package/service. */
  usedBy?: string[]
  /** Read only: id of an earlier source defining the same collection name, which wins. */
  shadowedBy?: string | null
}

export interface JdbcSourceSummary {
  id: string
  rrdRepository?: string
  collectionCount: number
  collectionNames: string[]
}

export interface JdbcSource {
  id: string
  rrdRepository?: string
  /** Read only: the repository the collector actually uses for all collections. */
  effectiveRrdRepository?: string
  collections: JdbcCollection[]
  /** The ETag of the source; sent back as If-Match. */
  version?: string
}

export interface JdbcDataCollectionMetadata {
  attributeTypes: string[]
  ifTypes: string[]
  resourceTypes: string[]
  defaultRrdRepository: string
  defaultRrdStep: number
  defaultRras: string[]
}

export interface JdbcSourceErrors {
  id?: string
  rrdRepository?: string
}

export interface JdbcCollectionErrors {
  name?: string
  rrdStep?: string
  rras?: string
}

export interface JdbcColumnErrors {
  name?: string
  type?: string
  alias?: string
}

export interface JdbcQueryErrors {
  name?: string
  recheckInterval?: string
  ifType?: string
  queryString?: string
  resourceType?: string
  instanceColumn?: string
  columns?: string
  /** Per column, by index; an entry without messages is an empty object. */
  columnErrors?: JdbcColumnErrors[]
}
