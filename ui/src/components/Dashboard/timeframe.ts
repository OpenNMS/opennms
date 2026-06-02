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

import { TimeframePreset } from '@/types/dashboard'

export const timeframeOptions: { label: string; value: TimeframePreset }[] = [
  { label: 'Last 24 hours', value: TimeframePreset.Last24h },
  { label: 'Today', value: TimeframePreset.Today },
  { label: 'Yesterday', value: TimeframePreset.Yesterday },
  { label: 'This week', value: TimeframePreset.ThisWeek },
  { label: 'Last week', value: TimeframePreset.LastWeek },
  { label: 'Last 7 days', value: TimeframePreset.Last7Days },
  { label: 'Last 30 days', value: TimeframePreset.Last30Days },
  { label: 'This month', value: TimeframePreset.ThisMonth },
  { label: 'Last month', value: TimeframePreset.LastMonth },
  { label: 'Custom range', value: TimeframePreset.Custom }
]

export const refreshOptions: { label: string; value: number }[] = [
  { label: 'Off', value: 0 },
  { label: 'Every 30 seconds', value: 30 },
  { label: 'Every minute', value: 60 },
  { label: 'Every 2 minutes', value: 120 },
  { label: 'Every 5 minutes', value: 300 }
]
