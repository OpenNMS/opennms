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

import { format } from 'date-fns'
import { StartEndTime } from '@/types'

const HOUR = 60 * 60 * 1000
const DAY = 24 * HOUR

/**
 * The Graph component's time window for a resolved graph. The server gives epoch milliseconds;
 * the graph wants seconds plus the axis-label granularity the resource graphs page derives from
 * its relative range, so pick the granularity from the window's length.
 */
export const graphTimeFor = (startMs: number, endMs: number): StartEndTime => {
  const span = endMs - startMs
  let unit: StartEndTime['format'] = 'days'
  if (span <= 2 * HOUR) {
    unit = 'minutes'
  } else if (span <= 2 * DAY) {
    unit = 'hours'
  } else if (span > 92 * DAY) {
    unit = 'months'
  }
  return {
    startTime: Math.floor(startMs / 1000),
    endTime: Math.floor(endMs / 1000),
    format: unit
  }
}

/** A window boundary as the legacy view showed it, in the browser's zone. */
export const formatGraphInstant = (ms: number): string => format(new Date(ms), 'yyyy-MM-dd HH:mm')

/** A legacy page link (relative to the webapp root) as an absolute href. */
export const legacyHref = (link: string): string => `/opennms/${link.replace(/^\/+/, '')}`

/** The SPA route for a legacy resource link when there is one, else null. */
export const spaRouteForLegacyLink = (link: string): string | null => {
  const node = /^\/?element\/node\.jsp\?node=(\d+)$/.exec(link)
  return node ? `/node/${node[1]}` : null
}
