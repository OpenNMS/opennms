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
 * Cross-links from a topology node to its data, mirroring the legacy map's
 * right-click "Node Info / Resource Graphs / Events / Alarms" operations.
 * These open the relevant existing pages (the new-UI node detail and the
 * established server pages) in a new tab so the topology stays put. URLs were
 * verified against a running 36.x instance.
 */

export interface NodeActionLink {
  label: string
  url: string
}

// OpenNMS webapp context root.
const ROOT = '/opennms'

export const nodeActionLinks = (nodeId: number): NodeActionLink[] => [
  { label: 'Node Details', url: `${ROOT}/ui/index.html#/node/${nodeId}` },
  { label: 'Resource Graphs', url: `${ROOT}/graph/chooseresource.jsp?node=${nodeId}` },
  { label: 'Events', url: `${ROOT}/event/list?filter=node%3D${nodeId}` },
  { label: 'Alarms', url: `${ROOT}/alarm/list.htm?filter=node%3D${nodeId}` }
]
