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
 * Canvas element id conventions, shared by the canvas (which mints the ids)
 * and other components (the inspector) that need to classify a selected id
 * without reaching into the graphology graph.
 *
 *  - placed nodes:  `placed-<paletteId>` where paletteId is the OnmsNode id
 *  - free labels:   `label-<seq>`
 *  - shapes:        `shape-<seq>` (annotation frames/boxes)
 *  - edges:         `edge-<...>` (graphology edge keys)
 */

export const PLACED_PREFIX = 'placed-'
export const LABEL_PREFIX = 'label-'
export const SHAPE_PREFIX = 'shape-'

export const placedIdFor = (paletteId: string): string => `${PLACED_PREFIX}${paletteId}`

export const paletteIdFromPlacedId = (id: string): string | null =>
  id.startsWith(PLACED_PREFIX) ? id.slice(PLACED_PREFIX.length) : null

export const isLabelId = (id: string): boolean => id.startsWith(LABEL_PREFIX)

export const isShapeId = (id: string): boolean => id.startsWith(SHAPE_PREFIX)

/** The real OnmsNode id for a placed node id, or null if not a placed node. */
export const nodeIdFromPlacedId = (id: string): number | null => {
  const paletteId = paletteIdFromPlacedId(id)
  if (paletteId === null || !/^\d+$/.test(paletteId)) {
    return null
  }
  return Number(paletteId)
}
