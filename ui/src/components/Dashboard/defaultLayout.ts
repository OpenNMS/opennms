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

const panel = (id: string, type: string, x: number, y: number, w: number, h: number): DashboardPanel => ({
  id,
  type,
  x,
  y,
  w,
  h,
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
  // Three columns with a wider middle (3 / 6 / 3 of 12), matching the legacy
  // homepage. Left: situations/alarms/outages; center (wide): status + map;
  // right: notifications/news.
  panels: [
    panel('situations-1', 'pending-situations', 0, 0, 3, 3),
    panel('nodes-1', 'nodes-with-alarms', 0, 3, 3, 5),
    panel('outages-1', 'service-outages', 0, 8, 3, 4),
    panel('status-1', 'status-overview', 3, 0, 6, 5),
    panel('map-1', 'regional-map', 3, 5, 6, 7),
    panel('notif-1', 'notifications', 9, 0, 3, 3),
    panel('news-1', 'newsfeed', 9, 3, 3, 5)
  ]
})
