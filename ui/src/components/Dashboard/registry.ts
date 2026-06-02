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

import { type Component, defineAsyncComponent } from 'vue'
import type { FilterKey } from '@/types/dashboard'

export interface PanelGridSize {
  w: number
  h: number
}

// A panel is self-contained: registering one entry here is all that's needed to
// make it available in the "Add panel" picker. The registry is the single place
// that grows as panels are added (NMS-4433 etc).
export interface PanelDefinition {
  type: string
  title: string
  category: 'status' | 'inventory' | 'info'
  component: Component
  defaultSize: PanelGridSize
  minSize?: PanelGridSize
  supportsTimeframe?: boolean // show timeframe control for this panel (user-requested)
  supportedFilters?: FilterKey[] // filters this panel honors (NMS-10507)
  renamable?: boolean // default true
  collapsible?: boolean // default true (NMS-11946)
  roles?: string[] // future: restrict visibility via useRole()
}

export const panelRegistry: Record<string, PanelDefinition> = {
  sample: {
    type: 'sample',
    title: 'Sample Panel',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/SamplePanel.vue')),
    defaultSize: { w: 4, h: 4 },
    minSize: { w: 2, h: 2 },
    supportsTimeframe: true,
    supportedFilters: ['surveillanceCategories', 'ipMatch'],
    renamable: true,
    collapsible: true
  }
}

export const getPanelDefinition = (type: string): PanelDefinition | undefined => panelRegistry[type]

export const listPanelDefinitions = (): PanelDefinition[] => Object.values(panelRegistry)
