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
import type { FilterKey, PanelHeightMode } from '@/types/dashboard'

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
  // default height behaviour; user can override per panel instance. 'fixed' =
  // fixed grid height with scrollbar, 'auto' = grow/shrink to content.
  defaultHeightMode?: PanelHeightMode
  supportsTimeframe?: boolean // show timeframe control for this panel (user-requested)
  supportedFilters?: FilterKey[] // filters this panel honors (NMS-10507)
  // legacy deep-link the panel title opens (matches the front-page box headers)
  titleHref?: string
  renamable?: boolean // default true
  collapsible?: boolean // default true (NMS-11946)
  roles?: string[] // future: restrict visibility via useRole()
  // not offered in the "Add panel" picker; still renders if a layout references
  // it (dev/debug panels like 'sample')
  hidden?: boolean
}

export const panelRegistry: Record<string, PanelDefinition> = {
  'pending-situations': {
    type: 'pending-situations',
    title: 'Pending Situations',
    titleHref: '/opennms/alarm/list.htm',
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
    titleHref: '/opennms/alarm/list.htm',
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
    titleHref: '/opennms/outage/list.htm',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/OutagesPanel.vue')),
    defaultSize: { w: 4, h: 4 },
    minSize: { w: 3, h: 3 },
    renamable: true,
    collapsible: true
  },
  'status-overview': {
    type: 'status-overview',
    defaultHeightMode: 'fixed',
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
    titleHref: '/opennms/topology?provider=Business%20Services&layout=Hierarchy%20Layout&szl=1',
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
    titleHref: '/opennms/application/index.jsp',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/ApplicationsPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 3, h: 2 },
    renamable: true,
    collapsible: true
  },
  availability: {
    type: 'availability',
    defaultHeightMode: 'fixed',
    title: 'Availability Over the Past 24 Hours',
    titleHref: '/opennms/rtc/index.jsp',
    category: 'status',
    component: defineAsyncComponent(() => import('./panels/AvailabilityPanel.vue')),
    defaultSize: { w: 6, h: 4 },
    minSize: { w: 4, h: 3 },
    renamable: true,
    collapsible: true
  },
  'regional-map': {
    type: 'regional-map',
    defaultHeightMode: 'fixed',
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
    titleHref: '/opennms/notification/index.jsp',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/NotificationsPanel.vue')),
    defaultSize: { w: 4, h: 3 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  newsfeed: {
    type: 'newsfeed',
    defaultHeightMode: 'fixed',
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
    titleHref: '/opennms/graph/index.jsp',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/ResourceGraphsPanel.vue')),
    defaultSize: { w: 3, h: 2 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  'ksc-reports': {
    type: 'ksc-reports',
    // the legacy dashboard labels this KSC-report search box "Graph Collections"
    title: 'Graph Collections',
    titleHref: '/opennms/KSC/index.jsp',
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
  notes: {
    type: 'notes',
    title: 'Notes',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/NotesPanel.vue')),
    defaultSize: { w: 3, h: 3 },
    minSize: { w: 2, h: 2 },
    renamable: true,
    collapsible: true
  },
  'html-content': {
    type: 'html-content',
    defaultHeightMode: 'fixed',
    title: 'HTML Content',
    category: 'info',
    component: defineAsyncComponent(() => import('./panels/HtmlContentPanel.vue')),
    defaultSize: { w: 4, h: 5 },
    minSize: { w: 2, h: 3 },
    renamable: true,
    collapsible: true
  },
  sample: {
    type: 'sample',
    hidden: true, // framework wiring check — keep for debugging, not for production
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

export const listPanelDefinitions = (): PanelDefinition[] =>
  Object.values(panelRegistry).filter(d => !d.hidden)
