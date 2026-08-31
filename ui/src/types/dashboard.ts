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

// Type model for the configurable system-wide dashboard (NMS-19851).
// The persisted layout document is intentionally self-describing so that
// future per-user / multiple-named dashboards are additive (change `scope`),
// not a rewrite.

export enum TimeframePreset {
  Last24h = 'LAST_24H',
  Today = 'TODAY',
  Yesterday = 'YESTERDAY',
  ThisWeek = 'THIS_WEEK',
  LastWeek = 'LAST_WEEK',
  Last7Days = 'LAST_7D',
  Last30Days = 'LAST_30D',
  ThisMonth = 'THIS_MONTH',
  LastMonth = 'LAST_MONTH',
  Custom = 'CUSTOM'
}

export interface Timeframe {
  preset: TimeframePreset
  // ISO-8601 strings, only used when preset === Custom
  from: string | null
  to: string | null
}

export type FilterKey = 'surveillanceCategories' | 'ipMatch'

// Shared filter contract honored by every panel (NMS-10507).
export interface DashboardFilter {
  surveillanceCategories: string[]
  ipMatch: string | null
}

// Global auto-refresh with pause (NMS-4404). seconds <= 0 means "off".
export interface DashboardRefresh {
  seconds: number
  paused: boolean
}

export type PanelHeightMode = 'fixed' | 'auto'

export interface DashboardPanel {
  id: string
  type: string // matches a PanelDefinition.type in the registry
  // grid geometry (12-column grid)
  x: number
  y: number
  w: number
  h: number
  // 'fixed' = fixed grid height with scrollbar; 'auto' = grow to fit content.
  // undefined => use the registry default for the panel type.
  heightMode?: PanelHeightMode
  collapsed: boolean // NMS-11946
  titleOverride: string | null // user rename; null => registry default title
  filterOverride: DashboardFilter | null // null => use globalFilter
  timeframeOverride: Timeframe | null // null => use globalTimeframe
  refreshSeconds: number | null // null => use refresh.seconds
  options: Record<string, unknown> // panel-specific content options
}

// Future: 'USER' and named dashboards. Only 'SYSTEM' is used today.
export type DashboardScope = 'SYSTEM'

export interface DashboardLayout {
  scope: DashboardScope
  version: number
  refresh: DashboardRefresh
  globalFilter: DashboardFilter
  globalTimeframe: Timeframe
  // When true, panels squeeze up/down to pack against their neighbours (vertical
  // compaction + auto-height reflow). When false, panels stay where they are
  // placed (free-form). Default true for legacy-homepage-style packed columns.
  autoCompact: boolean
  panels: DashboardPanel[]
}

// Props every panel component receives from the PanelFrame.
export interface PanelComponentProps {
  options: Record<string, unknown>
  filter: DashboardFilter
  timeframe: Timeframe
  // increments on each global refresh tick; panels watch this to refetch
  refreshTick: number
}
