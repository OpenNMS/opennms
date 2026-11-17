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

import useActiveNodeId from '@/components/Nodes/hooks/useActiveNodeId'
import { enableAutoUnmount, mount } from '@vue/test-utils'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { defineComponent, h, KeepAlive, nextTick, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '1' }})

  return { useRoute: () => route }
})

enableAutoUnmount(afterEach)

const moveTo = async (id: string) => {
  ;(useRoute() as any).params.id = id
  await nextTick()
}

describe('useActiveNodeId', () => {
  let seen: string[]
  let current: () => string

  // Records every id its watcher sees, as a component fetching per node would.
  const Probe = defineComponent({
    setup() {
      const nodeId = useActiveNodeId()
      current = () => nodeId.value
      watch(nodeId, id => seen.push(id))

      return () => h('div', nodeId.value)
    }
  })

  beforeEach(() => {
    seen = []
    ;(useRoute() as any).params.id = '1'
  })

  it('follows the route outside a KeepAlive', async () => {
    mount(Probe)

    await moveTo('2')

    expect(current()).toBe('2')
    expect(seen).toEqual(['2'])
  })

  describe('in a KeepAlive', () => {
    const showProbe = ref(true)
    const Host = defineComponent({
      setup: () => () => h(KeepAlive, null, [showProbe.value ? h(Probe) : h('div')])
    })

    const hide = async () => {
      showProbe.value = false
      await nextTick()
    }

    const show = async () => {
      showProbe.value = true
      await nextTick()
    }

    beforeEach(() => {
      showProbe.value = true
    })

    it('follows the route while shown', async () => {
      mount(Host)

      await moveTo('2')

      expect(seen).toEqual(['2'])
    })

    it('holds still while hidden', async () => {
      mount(Host)
      await hide()

      await moveTo('2')
      await moveTo('3')

      expect(current()).toBe('1')
      expect(seen).toEqual([])
    })

    // One catch-up for the node on screen, not one per node passed through.
    it('catches up to the route once when shown again', async () => {
      mount(Host)
      await hide()
      await moveTo('2')
      await moveTo('3')

      await show()

      expect(current()).toBe('3')
      expect(seen).toEqual(['3'])
    })

    it('does not fire when shown again on the same node', async () => {
      mount(Host)
      await hide()

      await show()

      expect(seen).toEqual([])
    })
  })
})
