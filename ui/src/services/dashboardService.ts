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

import axios from 'axios'
import { v2 } from './axiosInstances'
import type { DashboardLayout, DashboardPanel } from '@/types/dashboard'
import { createDefaultLayout } from '@/components/Dashboard/defaultLayout'
import { getPanelDefinition } from '@/components/Dashboard/registry'

// A stored document is arbitrary JSON (any admin/REST client can PUT it), so
// coerce it into a complete, safe layout: fill missing top-level blocks from
// the defaults and drop panels that are malformed or reference an unknown type.
// This keeps one corrupt/partial document from crashing every user's homepage.
export const normalizeLayout = (raw: unknown): DashboardLayout => {
  const base = createDefaultLayout()
  if (!raw || typeof raw !== 'object') {
    return base
  }
  const doc = raw as Partial<DashboardLayout>
  const panels = Array.isArray(doc.panels)
    ? doc.panels.filter((p): p is DashboardPanel =>
        !!p && typeof p === 'object'
        && typeof (p as DashboardPanel).id === 'string'
        && typeof (p as DashboardPanel).type === 'string'
        && !!getPanelDefinition((p as DashboardPanel).type)
        && ['x', 'y', 'w', 'h'].every((k) => Number.isFinite((p as unknown as Record<string, unknown>)[k])))
      .map((p) => ({
        ...p,
        collapsed: !!p.collapsed,
        titleOverride: p.titleOverride ?? null,
        filterOverride: p.filterOverride ?? null,
        timeframeOverride: p.timeframeOverride ?? null,
        refreshSeconds: p.refreshSeconds ?? null,
        options: (p.options && typeof p.options === 'object') ? p.options : {}
      }))
    : base.panels
  return {
    scope: 'SYSTEM',
    version: typeof doc.version === 'number' ? doc.version : base.version,
    refresh: {
      seconds: Number.isFinite(doc.refresh?.seconds as number) ? (doc.refresh as DashboardLayout['refresh']).seconds : base.refresh.seconds,
      paused: !!doc.refresh?.paused
    },
    globalFilter: {
      surveillanceCategories: Array.isArray(doc.globalFilter?.surveillanceCategories) ? doc.globalFilter!.surveillanceCategories : [],
      ipMatch: doc.globalFilter?.ipMatch ?? null
    },
    globalTimeframe: {
      preset: doc.globalTimeframe?.preset ?? base.globalTimeframe.preset,
      from: doc.globalTimeframe?.from ?? null,
      to: doc.globalTimeframe?.to ?? null
    },
    panels
  }
}

// Single system-wide dashboard document, served by DashboardRestService at
// /api/v2/dashboard/system. GET 404s until a layout has been saved, in which
// case we fall back to the built-in default.
const endpoint = '/dashboard/system'

export const getSystemDashboard = async (): Promise<DashboardLayout> => {
  try {
    const response = await v2.get<unknown>(endpoint)
    return normalizeLayout(response.data)
  } catch (error) {
    if (axios.isAxiosError(error) && error.response?.status === 404) {
      // no layout saved yet — use the built-in default
      return createDefaultLayout()
    }
    // a real failure (500, network, timeout) must NOT masquerade as the default
    // layout: doing so lets a subsequent Save silently overwrite the stored one
    throw error
  }
}

export const saveSystemDashboard = async (layout: DashboardLayout): Promise<void> => {
  await v2.put(endpoint, layout)
}

export interface ServiceType {
  id: number
  name: string
}

// Monitored service types (id + name) for the Quick Search "Providing service" control.
export const getServiceTypes = async (): Promise<ServiceType[]> => {
  try {
    const resp = await v2.get('/dashboard/service-types', { headers: { Accept: 'application/json' } })
    return Array.isArray(resp.data) ? (resp.data as ServiceType[]) : []
  } catch (err) {
    return []
  }
}
