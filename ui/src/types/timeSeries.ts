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

/**
 * Consolidation Function Type, corresponding to RRDtool CF types.
 * Note that we do not support all RRDtool CF types, only the "classic" ones that are currently supported by OpenNMS.
 * See org.opennms.netmgt.rrd.model.v3.CFType, but again we only support these: AVERAGE, MIN, MAX, LAST.
 * See also: https://oss.oetiker.ch/rrdtool/doc/rrdcreate.en.html#AEN201
 */
export enum ConsolidationFunctionType {
  AVERAGE = 'AVERAGE',
  MIN = 'MIN',
  MAX = 'MAX',
  LAST = 'LAST'
}

/**
 * Data Source Type for RRDtool data sources.
 * Mirrors org.opennms.netmgt.rrd.model.v3.DSType.
 */
export enum TimeseriesDataSourceType {
  GAUGE = 'GAUGE',
  COUNTER = 'COUNTER',
  DERIVE = 'DERIVE',
  ABSOLUTE = 'ABSOLUTE',
  COMPUTE = 'COMPUTE'
}

export interface RRA {
  cf: ConsolidationFunctionType
  xff: number
  steps: number
  rows: number
}

export interface TimeSeriesDataSource {
  name: string
  type: TimeseriesDataSourceType
  heartbeat: number
  min?: number
  max?: number
}
