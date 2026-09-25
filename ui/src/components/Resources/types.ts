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
import { Metric, Series } from '@/types'

// The graph model RrdGraphConverter produces from a graph definition.
export interface ForecastModel {
  title: string
  verticalLabel: string
  metrics: Metric[]
  series: Series[]
}

// Forecast controls; each maps to a parameter of the server measurements filters.
export interface ForecastOptions {
  trainingStart: number
  graphStart: number
  season: number
  forecasts: number
  outlierThreshold: number
  confidenceLevel: number
  trendOrder: number
}

export interface ForecastTemplate {
  id: string
  name: string
  options: ForecastOptions
}

export interface ForecastPoint {
  x: number
  y: number
}

// One Chart.js line dataset as the forecast chart draws it.
export interface ForecastLineDataset {
  label: string
  data: ForecastPoint[]
  borderColor: string
  backgroundColor: string
  borderDash: number[]
  fill: string | false
  radius: number
  hitRadius: number
  borderWidth: number
  tension: number
}

// The selected column of a measurements response, with its sample spacing.
export interface ForecastColumn {
  timestamps: number[]
  values: number[]
  stepMs: number
}
