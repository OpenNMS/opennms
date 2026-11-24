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
 * Custom HTML5 drag-and-drop MIME type for palette-to-canvas drags.
 * The palette sets this on dragstart; the canvas reads it on drop.
 * Used to discriminate our drags from arbitrary text/file drops.
 */
export const PALETTE_DRAG_MIME = 'application/x-opennms-topology-node'

export interface PaletteDragPayload {
  nodeId: string
  label: string
}
