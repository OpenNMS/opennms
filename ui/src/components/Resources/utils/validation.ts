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
import { ForecastOptions } from '@/components/Resources/types'

// The legacy forecast JSP's form validation, completed; keyed by option name.
export const forecastOptionProblems = (o: ForecastOptions): Record<string, string> => {
  const p: Record<string, string> = {}
  const intGe1 = (v: number) => Number.isInteger(v) && v >= 1
  if (!(o.trainingStart >= 1)) {
    p.trainingStart = 'Must be at least 1 day.'
  }
  if (!(o.graphStart >= 1)) {
    p.graphStart = 'Must be at least 1 day.'
  }
  if (!(o.season > 0)) {
    p.season = 'Must be greater than 0.'
  } else if (!(o.season * 2 < o.trainingStart)) {
    p.season = 'Season × 2 must be less than the training window.'
  }
  if (!intGe1(o.forecasts)) {
    p.forecasts = 'Must be a whole number ≥ 1.'
  }
  if (!(o.outlierThreshold > 0.5 && o.outlierThreshold < 1)) {
    p.outlierThreshold = 'Must be between 0.5 and 1.'
  }
  if (!(o.confidenceLevel > 0 && o.confidenceLevel < 1)) {
    p.confidenceLevel = 'Must be between 0 and 1.'
  }
  if (!intGe1(o.trendOrder)) {
    p.trendOrder = 'Must be a whole number ≥ 1.'
  }
  return p
}
