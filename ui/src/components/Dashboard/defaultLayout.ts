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

import { type DashboardLayout, type DashboardPanel, TimeframePreset } from '@/types/dashboard'

// h is authored in legacy grid rows for readability but stored in PIXELS (the
// canonical unit the grid/persistence use), so the default doc doesn't mix units
// with saved (pixel) layouts. The single source of truth for the row size —
// the grid's legacy-layout conversion and the store's addPanel import it.
export const ROW_PX = 56

// referenced by the layout fallback so an install with the news feed disabled
// (opennms.newsFeedPanel.show=false) doesn't ship a 9-row dead box
export const NEWSFEED_PANEL_TYPE = 'newsfeed'

const panel = (id: string, type: string, x: number, y: number, w: number, rows: number): DashboardPanel => ({
  id,
  type,
  x,
  y,
  w,
  h: rows * ROW_PX,
  collapsed: false,
  titleOverride: null,
  filterOverride: null,
  timeframeOverride: null,
  refreshSeconds: null,
  options: {}
})

// Built-in default used until the backend (NMS-19851) persists a layout, and
// as the "reset" target. A factory (not a constant) so each load gets a fresh,
// independently-mutable object.
export const createDefaultLayout = (): DashboardLayout => ({
  scope: 'SYSTEM',
  version: 1,
  refresh: { seconds: 120, paused: false },
  globalFilter: { surveillanceCategories: [], ipMatch: null },
  globalTimeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
  autoCompact: true,
  // Three columns with a wider middle (3 / 6 / 3 of 12), matching the legacy
  // homepage column order/positions for easy side-by-side comparison:
  //   Left   : Situations, Nodes w/ Alarms, Service Outages, Business Services,
  //            Applications, News Feed
  //   Center : Status Overview, Availability (24h), Regional Status map
  //   Right  : Notifications, Resource Graphs, Graph Collections, Quick Search
  panels: [
    panel('situations-1', 'pending-situations', 0, 0, 3, 3),
    panel('nodes-1', 'nodes-with-alarms', 0, 3, 3, 5),
    panel('outages-1', 'service-outages', 0, 8, 3, 3),
    panel('bsm-1', 'business-services', 0, 11, 3, 3),
    panel('apps-1', 'applications', 0, 14, 3, 3),
    panel('news-1', 'newsfeed', 0, 17, 3, 9),
    panel('status-1', 'status-overview', 3, 0, 6, 7),
    panel('availability-1', 'availability', 3, 7, 6, 7),
    panel('map-1', 'regional-map', 3, 14, 6, 8),
    panel('notif-1', 'notifications', 9, 0, 3, 3),
    panel('graphs-1', 'resource-graphs', 9, 3, 3, 2),
    panel('ksc-1', 'graph-collections', 9, 5, 3, 2),
    panel('quicksearch-1', 'quick-search', 9, 7, 3, 5)
  ]
})
