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

export type OnmsTagSeverity = 'secondary' | 'success' | 'info' | 'warn' | 'danger'

// Menu model item in OpenNMS vocabulary (structurally compatible with
// PrimeVue's MenuItem). The index signature permits app-specific fields
// (e.g. iconComponent, statusClass) consumed by custom #item templates.
export interface OnmsMenuItem {
  label?: string
  key?: string
  separator?: boolean
  url?: string
  target?: string
  disabled?: boolean
  visible?: boolean
  items?: OnmsMenuItem[]
  command?: (event: { originalEvent: Event, item: OnmsMenuItem }) => void
  [custom: string]: unknown
}

// DataTable-shaped event payloads forwarded by OnmsTable (NMS-20081). Field
// names and types deliberately match PrimeVue's DataTablePageEvent /
// DataTableSortEvent / DataTableRowEditSaveEvent so existing handler bodies
// keep working after the seam sweep. `any` mirrors PrimeVue's own typing of
// row data (the app's row shapes are per-consumer).
export interface OnmsTablePageEvent {
  /** New page index (0-based) */
  page: number
  /** Index of the first row on the new page */
  first: number
  /** Rows per page */
  rows: number
  pageCount?: number
}

export interface OnmsTableSortEvent {
  sortField: string | ((item: any) => string) | null | undefined
  sortOrder: number | null | undefined
  first?: number
  rows?: number
}

export interface OnmsTableRowClickEvent {
  originalEvent?: Event
  /** Clicked row data */
  data: any
  /** Row index */
  index: number
}

export interface OnmsTableRowEditSaveEvent {
  originalEvent?: Event
  /** Row data before the edit */
  data: any
  /** Row data with the editor values applied */
  newData: any
  /** Row index */
  index: number
}
