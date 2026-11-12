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

/** Field hints of the JDBC data collection editor, shown by FormField :info. */
export const JDBC_FIELD_HINTS = {
  sourceId: 'Identifies the source. The former jdbc-datacollection.d/<id>.xml files became sources of that id.',
  rrdRepository: 'Directory the collected values are written to. The collector uses the repository of the first source that sets one, normally \'default\', for all collections.',
  collectionName: 'Selected by the \'collection\' parameter of a JdbcCollector service in collectd. Must be unique across all sources.',
  rrdStep: 'Interval in seconds the RRD files expect values at; usually the collection interval of the service.',
  rras: 'Round robin archives of new RRD files, e.g. RRA:AVERAGE:0.5:1:2016. Existing files keep their archives.',
  queryName: 'Unique within the collection; also the attribute group the values are stored in.',
  recheckInterval: 'Milliseconds between checks whether the query can run; in between, the result of the last check is reused. 0 checks before every collection.',
  ifType: 'Interface types the query applies to; \'ignore\' for node-level values, \'all\' otherwise.',
  resourceType: 'When the query returns more than one row, each row becomes a resource of this type. Set it together with the instance column.',
  instanceColumn: 'Column whose value identifies the resource of a row, e.g. the database name.',
  queryString: 'The SQL statement. Its result columns are matched by name to the columns below.',
  columnName: 'Name of the column.',
  dataSourceName: 'Column label in the result set; leave empty to use the name.',
  columnType: 'gauge and counter are stored as numbers, string as a string attribute.',
  alias: 'Name the value is stored under; at most 19 characters.'
}

export const SOURCES_HELP = 'A source is one stored JDBC data collection configuration. The source \'default\' holds the former jdbc-datacollection-config.xml, and every former file of jdbc-datacollection.d became a source of the same name. The JdbcCollector sees all sources merged: \'default\' first, then the others sorted by id. A collectd service selects one collection with its \'collection\' parameter. Changes take effect with the next collection.'
