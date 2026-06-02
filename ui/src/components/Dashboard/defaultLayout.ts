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

import { type DashboardLayout, TimeframePreset } from '@/types/dashboard'

// Built-in default used until the backend (NMS-19851) persists a layout, and
// as the "reset" target. A factory (not a constant) so each load gets a fresh,
// independently-mutable object.
export const createDefaultLayout = (): DashboardLayout => ({
  scope: 'SYSTEM',
  version: 1,
  refresh: { seconds: 120, paused: false },
  globalFilter: { surveillanceCategories: [], ipMatch: null },
  globalTimeframe: { preset: TimeframePreset.Last24h, from: null, to: null },
  panels: [
    {
      id: 'sample-1',
      type: 'sample',
      x: 0,
      y: 0,
      w: 4,
      h: 4,
      collapsed: false,
      titleOverride: null,
      filterOverride: null,
      timeframeOverride: null,
      refreshSeconds: null,
      options: {}
    }
  ]
})
