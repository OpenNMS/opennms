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

import { computed, ref, watch } from 'vue'
import { RouteLocationNamedRaw, useRoute, useRouter } from 'vue-router'
import { parseNodeRouteId } from '@/lib/nodeDetailsRoute'
import API from '@/services'
import { useNodeStore } from '@/stores/nodeStore'

/**
 * What a node page shows for its route id: the node, a foreign id still being looked up, or why
 * there is no node.
 */
export type NodePageState = 'node' | 'resolving' | 'invalid' | 'notFound' | 'error'

/**
 * Follows a node page's route id, as the legacy element/node.jsp?node= did: a canonical numeric id
 * is loaded; a non-canonical numeric id or a foreignSource:foreignId is replaced (keeping the query)
 * with the canonical id on the same page, and this runs again for it.
 *
 * Content that reads route.params.id mounts only once pageState is 'node', so it never sees a
 * foreign id.
 *
 * @param id the route id, as the route gives it
 * @param routeFor the page's own route for a node id; an object, so the query can be added to it
 * @param load fetches the node into the node store, whose nodeLoadError then says if it failed
 */
export const useNodeRouteResolution = (
  id: () => string,
  routeFor: (id: string | number) => RouteLocationNamedRaw,
  load: (id: string) => Promise<void>
) => {
  const nodeStore = useNodeStore()
  const route = useRoute()
  const router = useRouter()

  const resolveState = ref<'resolving' | 'notFound' | 'error' | null>(null)
  const routeId = computed(() => parseNodeRouteId(id()))
  const lookupValue = computed(() => (routeId.value.kind === 'numeric' ? routeId.value.id : routeId.value.value))

  const pageState = computed<NodePageState>(() => {
    const rid = routeId.value
    if (rid.kind === 'invalid') {
      return 'invalid'
    }
    if (rid.kind === 'foreign' || rid.id !== id()) {
      return resolveState.value ?? 'resolving'
    }
    if (nodeStore.nodeLoadError === 'notFound') {
      return 'notFound'
    }
    return nodeStore.nodeLoadError === 'failed' ? 'error' : 'node'
  })

  const replaceWith = (nodeId: string | number) => {
    router.replace({ ...routeFor(nodeId), query: route.query })
  }

  // Monotonic per call: a lookup answered after the route has moved on is dropped.
  let resolveRequest = 0

  // Canonical numeric id: load. Anything else: replace the route with the canonical id and let the
  // watcher below run again for it. Also the retry after a failed load or lookup.
  const followRoute = async () => {
    const rid = routeId.value
    const request = ++resolveRequest
    resolveState.value = null

    if (rid.kind === 'invalid') {
      return
    }
    if (rid.kind === 'numeric') {
      if (rid.id !== id()) {
        replaceWith(rid.id)
        return
      }
      await load(rid.id)
      return
    }

    resolveState.value = 'resolving'
    const result = await API.resolveNodeId(rid.value)
    if (request !== resolveRequest) {
      return
    }
    if (!result.success) {
      resolveState.value = 'error'
    } else if (result.payload === null || result.payload === undefined) {
      resolveState.value = 'notFound'
    } else {
      replaceWith(result.payload)
    }
  }

  // Immediate, so the load starts (and the store clears an earlier visit's failure) before the
  // first render.
  watch(id, followRoute, { immediate: true })

  return { routeId, lookupValue, pageState, followRoute }
}
