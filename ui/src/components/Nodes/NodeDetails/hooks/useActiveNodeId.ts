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

import { onActivated, onDeactivated, readonly, ref, watch, type Ref } from 'vue'
import { useRoute } from 'vue-router'

/**
 * The node id from the route, held still while a KeepAlive has switched the caller's tab away.
 *
 * Node Details keeps its tabs alive, and a hidden tab that followed the route would fetch for
 * every node the user moved through without ever showing it. With this, a hidden tab's watchers
 * stay quiet; coming back, the id catches up to the route once, and they fire for the node now
 * on screen. Outside a KeepAlive it simply follows the route.
 *
 * Call it from `setup`.
 */
const useActiveNodeId = (): Readonly<Ref<string>> => {
  const route = useRoute()
  const routeId = () => route.params.id as string

  const nodeId = ref(routeId())
  let active = true

  watch(routeId, (id) => {
    if (active) {
      nodeId.value = id
    }
  })

  onActivated(() => {
    active = true
    nodeId.value = routeId()
  })

  onDeactivated(() => {
    active = false
  })

  return readonly(nodeId)
}

export default useActiveNodeId
