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

import { RouteLocationRaw } from 'vue-router'
import { RouteQuery } from '@/components/AdhocGraphs/utils/adhocUrlState'
import { StartEndTime } from '@/types'

/**
 * An ad-hoc graph entry of a collection stores the custom graph builder's share link as its
 * query string, minus the link's time window: the entry's timespan gives the window, like
 * for any other graph of the collection. These helpers go between that string and the
 * route query the builder's encoder and decoder work with.
 */

/** The share link keys that carry the window. The server refuses an entry that still has them. */
export const ADHOC_TIME_KEYS = ['range', 'start', 'end', 'fmt']

/** The builder page, and the fragment path a stored entry's link opens. */
export const ADHOC_BUILDER_PATH = '/adhoc-graphs'

/** The stored string as the route query the builder's decoder reads; a repeated key becomes a list. */
export const adhocStateToQuery = (state: string): RouteQuery => {
  const query: RouteQuery = {}
  for (const [key, value] of new URLSearchParams(state)) {
    const current = query[key]
    if (current === undefined) {
      query[key] = value
    } else if (Array.isArray(current)) {
      current.push(value)
    } else {
      query[key] = [current, value]
    }
  }
  return query
}

/** The builder's encoded query as the string an entry stores, without the window. */
export const adhocStateFromQuery = (query: RouteQuery): string => {
  const params = new URLSearchParams()
  for (const [key, value] of Object.entries(query)) {
    if (ADHOC_TIME_KEYS.includes(key)) {
      continue
    }
    for (const entry of Array.isArray(value) ? value : [value]) {
      if (typeof entry === 'string') {
        params.append(key, entry)
      }
    }
  }
  return params.toString()
}

/** The graph title the builder stored in the state, if any; the entry's own title comes first. */
export const adhocStateTitle = (state: string | null | undefined): string =>
  state ? new URLSearchParams(state).get('title') ?? '' : ''

/**
 * Where the builder opens the stored graph. With a window, the builder opens on that same
 * absolute window instead of its default last 24 hours.
 */
export const adhocBuilderRoute = (state: string, window?: StartEndTime): RouteLocationRaw => {
  const query = adhocStateToQuery(state)
  if (window) {
    query.start = String(window.startTime)
    query.end = String(window.endTime)
    query.fmt = window.format
  }
  return { path: ADHOC_BUILDER_PATH, query: query as Record<string, string | string[]> }
}
