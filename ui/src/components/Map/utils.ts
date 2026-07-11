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

import { PopupOptions } from 'leaflet'

const numericSeverityLevel = (severity: string | undefined) => {
  if (severity) {
    switch (severity.toUpperCase()) {
      case 'NORMAL':
        return 11
      case 'WARNING':
        return 22
      case 'MINOR':
        return 33
      case 'MAJOR':
        return 44
      case 'CRITICAL':
        return 55
      default:
        return 0
    }
  }
  return 0
}

const stringToFixedFloat = (floatAsString: string, decimalPoints: number): string => {
  if (floatAsString) {
    const num = parseFloat(floatAsString)

    if (!Number.isNaN(num)) {
      return num.toFixed(decimalPoints)
    }
  }

  return floatAsString
}

// Shared options for marker + cluster popups. The map is full-bleed under the
// fixed top menu bar, so Leaflet's default autoPan (which only keeps a popup
// inside the container — whose top edge is behind the menu) leaves the popup's
// top hidden. autoPanPaddingTopLeft pushes the auto-pan target just below the
// menu (matching the 70px control clearance) and clear of the left side-menu
// rail, so the popup's top always lands in view. No maxHeight: the cluster
// popup's node list is already a fixed-height inner scroll, so capping the
// whole popup only adds a redundant second (outer) scrollbar.
const mapPopupOptions: PopupOptions = {
  autoPan: true,
  autoPanPaddingTopLeft: [60, 72],
  autoPanPaddingBottomRight: [60, 40],
  keepInView: false
}

export { mapPopupOptions, numericSeverityLevel, stringToFixedFloat }
