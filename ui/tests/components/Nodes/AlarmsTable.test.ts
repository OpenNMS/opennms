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

import AlarmsTable from '@/components/Nodes/AlarmsTable.vue'
import { OnmsTooltip } from '@opennms/onms-ui'
import NodeDownloadDropdown from '@/components/Nodes/NodeDownloadDropdown.vue'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { nextTick } from 'vue'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '144' }})

  return { useRoute: () => route }
})

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

enableAutoUnmount(afterEach)

// 12 alarms, lastEventTime rising with the id, so the newest is id 12.
const alarms = Array.from({ length: 12 }, (_, i) => ({
  id: String(i + 1),
  severity: i % 2 ? 'MAJOR' : 'MINOR',
  count: i + 1,
  lastEventTime: 1_700_000_000_000 + i * 60_000,
  logMessage: `<p>alarm ${i + 1}</p>`,
  uei: 'uei.opennms.org/test',
  nodeId: 144,
  nodeLabel: 'n144'
}))

describe('AlarmsTable.vue', () => {
  let store: ReturnType<typeof useAlarmStore>

  const mountTable = (nodeAlarms: unknown[] = alarms, nodeId = '144') => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    store = useAlarmStore(pinia)
    store.getNodeAlarms = vi.fn()
    store.nodeAlarms = nodeAlarms as never
    store.nodeAlarmsNodeId = nodeId
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as never

    return mount(AlarmsTable, {
      global: {
        plugins: [pinia, PrimeVue],
        directives: { 'onms-tooltip': OnmsTooltip }
      }
    })
  }

  const ids = (wrapper: ReturnType<typeof mountTable>) => wrapper.findAll('tbody tr td:first-child a').map(a => a.text())

  beforeEach(() => {
    vi.clearAllMocks()
    ;(useRoute() as any).params.id = '144'
  })

  it('has the five columns', () => {
    expect(mountTable().findAll('th').map(h => h.text()))
      .toEqual(['ID', 'Severity', 'Last Event Time', 'Log Message'])
  })

  it('shows 5 rows at a time, most recent first, with a paginator', () => {
    const wrapper = mountTable()

    expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
    expect(wrapper.findAll('.p-paginator-page').map(p => p.text())).toEqual(['1', '2', '3'])
  })

  it('pages through the alarms on hand without asking for more', async () => {
    const wrapper = mountTable()

    await wrapper.findAll('.p-paginator-page')[2].trigger('click')

    expect(ids(wrapper)).toEqual(['2', '1'])
    expect(store.getNodeAlarms).not.toHaveBeenCalled()
  })

  it('links each id to its alarm and colours its severity', () => {
    const wrapper = mountTable()

    expect(wrapper.find('tbody tr td a').attributes('href')).toBe('/opennms/alarm/detail.htm?id=12')
    expect(wrapper.find('tbody tr .p-tag').classes().join(' ')).toContain('danger')
  })

  it('renders the log message as HTML, clamped, with the text in a tooltip', () => {
    const message = mountTable().find('[data-test="log-message"]')

    expect(message.html()).toContain('<p>alarm 12</p>')
    expect(message.classes()).toContain('log-message')
    expect((message.element as any).$_ptooltipValue).toBe('alarm 12')
    // Widened past the default; see the component's unscoped style.
    expect((message.element as any).$_ptooltipClass).toBe('alarm-message-tooltip')
  })

  it('shows the last event time as a date line and a time line', () => {
    const lines = mountTable().find('[data-test="last-event-time"]').findAll('span').map(s => s.text())

    expect(lines).toHaveLength(2)
    expect(lines[0]).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    expect(lines[1]).toMatch(/^\d{2}:\d{2}:\d{2}[+-]\d{2}:\d{2}$/)
  })

  // The store's slice may still be the previous node's while this one's is in flight.
  it('shows nothing that belongs to another node', () => {
    const wrapper = mountTable(alarms, '152')

    expect(ids(wrapper)).toEqual([])
    expect(wrapper.find('[data-test="empty-list"]').exists()).toBe(true)
  })

  it('says so when the node has no alarms', () => {
    expect(mountTable([]).text()).toContain('No alarms for this node.')
  })

  it('goes back to the first page for a new node', async () => {
    const wrapper = mountTable()
    await wrapper.findAll('.p-paginator-page')[2].trigger('click')

    ;(useRoute() as any).params.id = '152'
    store.nodeAlarmsNodeId = '152'
    await flushPromises()

    expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
  })

  // A minute's refresh can clear alarms out from under the page being shown.
  it('steps back to the last page left when a refresh leaves fewer alarms', async () => {
    const wrapper = mountTable()
    await wrapper.findAll('.p-paginator-page')[2].trigger('click')

    store.nodeAlarms = alarms.slice(0, 7) as never
    await nextTick()
    await nextTick()

    expect(ids(wrapper)).toEqual(['2', '1'])
  })

  it('links View Alarms to the node\'s alarm list', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = mountTable()

    await wrapper.find('[data-test="view-alarms-button"]').trigger('click')

    expect(assign).toHaveBeenCalledWith('/opennms/alarm/list.htm?filter=node%3D144')
    vi.unstubAllGlobals()
  })

  it('downloads every alarm, not just the page', async () => {
    const downloads: Blob[] = []
    vi.stubGlobal('URL', { createObjectURL: (blob: Blob) => {
      downloads.push(blob)
      return 'blob:x'
    } } as any)
    // The download clicks an anchor; keep happy-dom from trying to navigate to it.
    const realCreateElement = document.createElement.bind(document)
    vi.spyOn(document, 'createElement').mockImplementation(((tag: string, options?: any) => {
      const el = realCreateElement(tag, options)
      if (tag === 'a') {
        Object.defineProperty(el, 'click', { value: () => undefined })
      }
      return el
    }) as any)
    const wrapper = mountTable()

    const items = wrapper.findComponent(NodeDownloadDropdown).vm.items as Array<{ label: string, command: () => void }>
    await items.find(i => i.label === 'Download JSON...')!.command()

    expect(JSON.parse(await downloads[0].text())).toHaveLength(12)
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('reports an empty download', async () => {
    const wrapper = mountTable([])

    const items = wrapper.findComponent(NodeDownloadDropdown).vm.items as Array<{ label: string, command: () => void }>
    await items.find(i => i.label === 'Download CSV...')!.command()

    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ error: true }))
  })
})
