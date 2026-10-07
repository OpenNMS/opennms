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

// One graph within a KSC report. Field names mirror the rest/ksc JSON exactly
// (KscRestService.KscGraph) so a fetched report round-trips back unchanged on save.
export interface KscGraph {
  title: string
  timespan: string
  graphtype: string
  resourceId?: string | null
  nodeId?: string | null
  nodeSource?: string | null
  domain?: string | null
  interfaceId?: string | null
  extlink?: string | null
}

// A KSC ("Graph Collections") report. `id` is null only for a not-yet-created
// report; the server assigns it. The list endpoint returns the same shape with
// an empty kscGraph array.
export interface KscReport {
  id: number | null
  label: string
  show_timespan_button?: boolean | null
  show_graphtype_button?: boolean | null
  graphs_per_line?: number | null
  kscGraph: KscGraph[]
}
