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
  'pending-situations': {
    type: 'pending-situations',
    title: 'Pending Situations',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/SituationsPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 3, h: 2 },
    renamable: true,
    collapsible: true
  },
  'nodes-with-alarms': {
    type: 'nodes-with-alarms',
    title: 'Nodes with Pending Alarms',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/NodesWithAlarmsPanel.vue')),
    defaultSize: { w: 4, h: 5 },
    minSize: { w: 3, h: 3 },
    renamable: true,
    collapsible: true
  },
  'service-outages': {
    type: 'service-outages',
    title: 'Nodes with Service Outages',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/OutagesPanel.vue')),
    defaultSize: { w: 4, h: 4 },
    minSize: { w: 3, h: 3 },
    renamable: true,
    collapsible: true
  },
  'status-overview': {
    type: 'status-overview',
    title: 'Status Overview',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/StatusOverviewPanel.vue')),
    defaultSize: { w: 4, h: 6 },
    minSize: { w: 3, h: 4 },
    renamable: true,
    collapsible: true
  },
  'business-services': {
    type: 'business-services',
    title: 'Business Services with Pending Alarms',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/BusinessServicesPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 3, h: 2 },
    renamable: true,
    collapsible: true
  },
  applications: {
    type: 'applications',
    title: 'Applications with Pending Alarms',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/ApplicationsPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 3, h: 2 },
    renamable: true,
    collapsible: true
  },
  availability: {
    type: 'availability',
    title: 'Availability Over the Past 24 Hours',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/AvailabilityPanel.vue')),
    defaultSize: { w: 6, h: 4 },
    minSize: { w: 4, h: 3 },
    renamable: true,
    collapsible: true
  },
  'regional-map': {
    type: 'regional-map',
    title: 'Regional Status',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/RegionalMapPanel.vue')),
    defaultSize: { w: 8, h: 6 },
    minSize: { w: 4, h: 4 },
    renamable: true,
    collapsible: true
  },
  notifications: {
    type: 'notifications',
    title: 'Notifications',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/NotificationsPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  newsfeed: {
    type: 'newsfeed',
    title: 'OpenNMS News Feed',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/NewsFeedPanel.vue')),
    defaultSize: { w: 4, h: 5 },
    minSize: { w: 3, h: 3 },
    renamable: true,
    collapsible: true
  },
  'resource-graphs': {
    type: 'resource-graphs',
    title: 'Resource Graphs',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/ResourceGraphsPanel.vue')),
    defaultSize: { w: 3, h: 2 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  'ksc-reports': {
    type: 'ksc-reports',
    title: 'KSC Reports',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/KscReportsPanel.vue')),
    defaultSize: { w: 3, h: 2 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  'quick-search': {
    type: 'quick-search',
    title: 'Quick Search',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/QuickSearchPanel.vue')),
    defaultSize: { w: 3, h: 5 },
    minSize: { w: 2, h: 3 },
    renamable: true,
    collapsible: true
  },
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
