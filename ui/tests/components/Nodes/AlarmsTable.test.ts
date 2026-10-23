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
import API from '@/services'
import { useAlarmStore } from '@/stores/alarmStore'
import { useMenuStore } from '@/stores/menuStore'
import { createTestingPinia } from '@pinia/testing'
import { enableAutoUnmount, flushPromises, mount } from '@vue/test-utils'
import PrimeVue from 'primevue/config'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { useRoute } from 'vue-router'

vi.mock('vue-router', async () => {
  const { reactive } = await import('vue')
  const route = reactive({ params: { id: '144' }})

  return { useRoute: () => route }
})

const { showSnackBar } = vi.hoisted(() => ({ showSnackBar: vi.fn() }))
vi.mock('@/composables/useSnackbar', () => ({ default: () => ({ showSnackBar }) }))

// The download asks the API for every alarm itself, outside the store.
vi.mock('@/services', () => ({ default: { getAlarms: vi.fn() }}))

// DOMPurify misbehaves under happy-dom (it strips <p> yet keeps onerror), so the sanitizer is
// stood in for here: these tests check the message goes through it, not what DOMPurify removes.
vi.mock('@/lib/sanitizeHtml', () => ({
  sanitizeHtml: (html?: string | null) => `<span data-test="sanitized">${html ?? ''}</span>`
}))

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
  // What the server holds for each node; the fake below pages it as the v2 API would.
  let server: Record<string, typeof alarms>
  let failFetches: boolean

  const newestFirst = (list: typeof alarms) => [...list].sort((a, b) => b.lastEventTime - a.lastEventTime)

  const createPinia = () => {
    const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
    store = useAlarmStore(pinia)
    store.getNodeAlarms = vi.fn(async (nodeId: string, params: any = {}) => {
      if (failFetches) {
        store.nodeAlarmsFailedNodeId = nodeId

        return { success: false, message: 'nope' }
      }

      const all = newestFirst(server[nodeId] ?? [])
      const page = all.slice(params.offset ?? 0, (params.offset ?? 0) + (params.limit || all.length))
      store.nodeAlarms = page as never
      store.nodeAlarmsTotalCount = all.length
      store.nodeAlarmsNodeId = nodeId
      store.nodeAlarmsFailedNodeId = undefined

      return { success: true, message: '', payload: page as never }
    })
    useMenuStore(pinia).mainMenu = { baseHref: '/opennms/' } as never

    return pinia
  }

  const mountTable = async () => {
    const wrapper = mount(AlarmsTable, {
      global: {
        plugins: [createPinia(), PrimeVue],
        directives: { 'onms-tooltip': OnmsTooltip }
      }
    })
    await flushPromises()

    return wrapper
  }

  const ids = (wrapper: Awaited<ReturnType<typeof mountTable>>) => wrapper.findAll('tbody tr td:first-child a').map(a => a.text())

  const goToPage = async (wrapper: Awaited<ReturnType<typeof mountTable>>, index: number) => {
    await wrapper.findAll('.p-paginator-page')[index].trigger('click')
    await flushPromises()
  }

  beforeEach(() => {
    vi.clearAllMocks()
    vi.useFakeTimers()
    ;(useRoute() as any).params.id = '144'
    server = { '144': alarms, '152': [] }
    failFetches = false
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('has the four columns', async () => {
    expect((await mountTable()).findAll('th').map(h => h.text()))
      .toEqual(['ID', 'Severity', 'Last Event Time', 'Log Message'])
  })

  // The server sorts and pages: the table asks for one page, newest first.
  it('asks the server for the first 5 alarms, newest first', async () => {
    await mountTable()

    expect(store.getNodeAlarms).toHaveBeenCalledWith('144', { limit: 5, offset: 0, orderBy: 'lastEventTime', order: 'desc' })
  })

  it('shows the page with a paginator over the node\'s total', async () => {
    const wrapper = await mountTable()

    expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
    expect(wrapper.findAll('.p-paginator-page').map(p => p.text())).toEqual(['1', '2', '3'])
  })

  it('fetches each page from the server', async () => {
    const wrapper = await mountTable()

    await goToPage(wrapper, 2)

    expect(store.getNodeAlarms).toHaveBeenLastCalledWith('144', expect.objectContaining({ offset: 10, limit: 5 }))
    expect(ids(wrapper)).toEqual(['2', '1'])
  })

  it('links each id to its alarm and colours its severity', async () => {
    const wrapper = await mountTable()

    expect(wrapper.find('tbody tr td a').attributes('href')).toBe('/opennms/alarm/detail.htm?id=12')
    expect(wrapper.find('tbody tr .p-tag').classes().join(' ')).toContain('danger')
  })

  it('renders the log message as sanitized HTML, clamped, with the text in a tooltip', async () => {
    const message = (await mountTable()).find('[data-test="log-message"]')

    expect(message.find('[data-test="sanitized"]').html()).toContain('<p>alarm 12</p>')
    expect(message.classes()).toContain('log-message')
    expect((message.element as any).$_ptooltipValue).toBe('alarm 12')
    // Widened past the default; see the component's unscoped style.
    expect((message.element as any).$_ptooltipClass).toBe('alarm-message-tooltip')
  })

  it('shows the last event time as a date line and a time line', async () => {
    const lines = (await mountTable()).find('[data-test="last-event-time"]').findAll('span').map(s => s.text())

    expect(lines).toHaveLength(2)
    expect(lines[0]).toMatch(/^\d{4}-\d{2}-\d{2}$/)
    expect(lines[1]).toMatch(/^\d{2}:\d{2}:\d{2}[+-]\d{2}:\d{2}$/)
  })

  it('says so when the node has no alarms', async () => {
    server['144'] = []

    expect((await mountTable()).text()).toContain('No alarms for this node.')
  })

  describe('loading and failure', () => {
    // Nothing on hand for this node yet is not the same as no alarms.
    it('says it is loading, and shows nothing of another node, while the node\'s page is in flight', async () => {
      const wrapper = await mountTable()
      store.getNodeAlarms = vi.fn(() => new Promise(() => undefined)) as never

      ;(useRoute() as any).params.id = '152'
      await flushPromises()

      expect(ids(wrapper)).toEqual([])
      expect(wrapper.text()).toContain('Loading alarms…')
      expect(wrapper.text()).not.toContain('No alarms for this node.')
    })

    it('says the alarms could not be loaded when the node\'s fetch failed', async () => {
      failFetches = true
      const wrapper = await mountTable()

      expect(wrapper.text()).toContain('Unable to load alarms for this node.')
      expect(wrapper.text()).not.toContain('No alarms for this node.')
    })

    // A minute-old page says more than none.
    it('keeps showing the page when a refresh fails', async () => {
      const wrapper = await mountTable()

      failFetches = true
      await vi.advanceTimersByTimeAsync(60_000)

      expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
    })
  })

  describe('refresh', () => {
    it('refreshes the page every minute', async () => {
      await mountTable()
      vi.mocked(store.getNodeAlarms).mockClear()

      await vi.advanceTimersByTimeAsync(60_000)

      expect(store.getNodeAlarms).toHaveBeenCalledTimes(1)
    })

    // A refresh can clear alarms out from under the page being shown.
    it('steps back to the last page left when a refresh leaves fewer alarms', async () => {
      const wrapper = await mountTable()
      await goToPage(wrapper, 2)

      server['144'] = alarms.slice(0, 7)
      await vi.advanceTimersByTimeAsync(60_000)

      expect(store.getNodeAlarms).toHaveBeenLastCalledWith('144', expect.objectContaining({ offset: 5 }))
      expect(ids(wrapper)).toEqual(['2', '1'])
    })
  })

  it('goes back to the first page for a new node', async () => {
    server['152'] = alarms
    const wrapper = await mountTable()
    await goToPage(wrapper, 2)

    ;(useRoute() as any).params.id = '152'
    await flushPromises()

    expect(store.getNodeAlarms).toHaveBeenLastCalledWith('152', expect.objectContaining({ offset: 0 }))
    expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
  })

  it('links View Alarms to the node\'s alarm list', async () => {
    const assign = vi.fn()
    vi.stubGlobal('location', { assign } as any)
    const wrapper = await mountTable()

    await wrapper.find('[data-test="view-alarms-button"]').trigger('click')

    expect(assign).toHaveBeenCalledWith('/opennms/alarm/list.htm?filter=node%3D144')
    vi.unstubAllGlobals()
  })

  describe('download', () => {
    const download = async (wrapper: Awaited<ReturnType<typeof mountTable>>, label: string) => {
      const items = wrapper.findComponent(NodeDownloadDropdown).vm.items as Array<{ label: string, command: () => void }>
      await items.find(i => i.label === label)!.command()
      await flushPromises()
    }

    it('downloads every alarm, not just the page, without touching the table\'s page', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue({ alarm: alarms, totalCount: 12, count: 12, offset: 0 } as never)
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
      const wrapper = await mountTable()

      await download(wrapper, 'Download JSON...')

      expect(API.getAlarms).toHaveBeenCalledWith({ limit: 0, orderBy: 'lastEventTime', order: 'desc', _s: 'node.id==144' })
      expect(JSON.parse(await downloads[0].text())).toHaveLength(12)
      expect(ids(wrapper)).toEqual(['12', '11', '10', '9', '8'])
      vi.unstubAllGlobals()
      vi.restoreAllMocks()
    })

    it('reports an empty download', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue({ alarm: [], totalCount: 0, count: 0, offset: 0 } as never)
      const wrapper = await mountTable()

      await download(wrapper, 'Download CSV...')

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringContaining('No alarms found'), error: true }))
    })

    it('reports a failed download', async () => {
      vi.mocked(API.getAlarms).mockResolvedValue(false)
      const wrapper = await mountTable()

      await download(wrapper, 'Download CSV...')

      expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: expect.stringContaining('Unable to load'), error: true }))
    })
  })
})
