import { beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import PrimeVue from 'primevue/config'
import PanelOptionsDialog from '@/components/Dashboard/PanelOptionsDialog.vue'
import { listMetricEntities, type MetricEntity } from '@/services/metricChartService'
import { useDashboardStore } from '@/stores/dashboardStore'

vi.mock('@/services/metricChartService', () => ({
  DEFAULT_CHART_METRIC: 'response-time',
  listMetricEntities: vi.fn()
}))

vi.mock('@/services/topnService', async (importOriginal) => {
  const actual = await importOriginal<typeof import('@/services/topnService')>()
  return { ...actual, listAvailableKpis: vi.fn(async () => actual.TOPN_KPIS) }
})

const DialogStub = { name: 'Dialog', props: ['visible'], template: '<div v-if="visible"><slot /><slot name="footer" /></div>' }

const saved = { id: 'node[9].responseTime[9.9.9.9]', label: 'nine' }
const other = { id: 'node[1].responseTime[10.0.0.1]', label: 'one' }

const openDialog = async (options: Record<string, unknown>) => {
  const pinia = createTestingPinia({ createSpy: vi.fn, stubActions: false })
  const store = useDashboardStore(pinia)
  const setPanelOptions = vi.spyOn(store, 'setPanelOptions').mockImplementation(() => undefined)
  const wrapper = mount(PanelOptionsDialog, {
    props: { panel: { id: 'p1', type: 'metric-chart', options } as any, visible: false },
    global: { plugins: [PrimeVue, pinia], stubs: { Dialog: DialogStub }}
  })
  await wrapper.setProps({ visible: true })
  await flushPromises()
  const selects = () => wrapper.findAllComponents({ name: 'OnmsSelect' })
  return { wrapper, setPanelOptions, entitySelect: () => selects()[0], metricSelect: () => selects()[1] }
}

describe('PanelOptionsDialog.vue (metric chart)', () => {
  beforeEach(() => {
    vi.mocked(listMetricEntities).mockReset()
    // the saved entity has data for the default metric only
    vi.mocked(listMetricEntities).mockImplementation(async (metric: string) => (metric === 'response-time' ? [other, saved] : [other]))
  })

  it('keeps a saved entity on open even with a non-default metric, marking it as having no data', async () => {
    const { entitySelect, wrapper, setPanelOptions } = await openDialog({ metric: 'http-response-time', entity: saved.id, entityLabel: saved.label })
    expect(vi.mocked(listMetricEntities)).toHaveBeenCalledTimes(1)
    expect(entitySelect().props('modelValue')).toBe(saved.id)
    expect(entitySelect().props('options')[0].label).toBe('nine (no data for this metric)')

    await wrapper.findAll('button').find(b => b.text() === 'Apply')?.trigger('click')
    expect(setPanelOptions).toHaveBeenCalledWith('p1', expect.objectContaining({ entity: saved.id, entityLabel: 'nine', metric: 'http-response-time' }))
  })

  it('drops the saved entity once the user picks a metric it has no data for', async () => {
    const { entitySelect, metricSelect } = await openDialog({ metric: 'response-time', entity: saved.id, entityLabel: saved.label })
    expect(entitySelect().props('options').map((o: { label: string }) => o.label)).toEqual(['one', 'nine'])

    metricSelect().vm.$emit('update:modelValue', 'http-response-time')
    await flushPromises()
    expect(vi.mocked(listMetricEntities)).toHaveBeenLastCalledWith('http-response-time')
    expect(entitySelect().props('modelValue')).toBe('')
    expect(entitySelect().props('options').map((o: { label: string }) => o.label)).toEqual(['one'])
  })

  it('moves a panel saved with a label onto the resource id', async () => {
    const { entitySelect } = await openDialog({ metric: 'response-time', entity: 'nine' })
    expect(entitySelect().props('modelValue')).toBe(saved.id)
    expect(entitySelect().props('options').map((o: { label: string }) => o.label)).toEqual(['one', 'nine'])
  })

  it('lets the latest metric change win when two are in flight', async () => {
    const pending: Array<(v: MetricEntity[]) => void> = []
    vi.mocked(listMetricEntities).mockImplementation(() => new Promise<MetricEntity[]>(r => pending.push(r)))
    const { entitySelect, metricSelect } = await openDialog({ metric: 'response-time', entity: '' })
    pending.shift()?.([other, saved])
    await flushPromises()
    metricSelect().vm.$emit('update:modelValue', 'http-response-time')
    metricSelect().vm.$emit('update:modelValue', 'ssh-response-time')
    await flushPromises()
    pending[1]?.([saved])
    pending[0]?.([other])
    await flushPromises()
    expect(entitySelect().props('options').map((o: { label: string }) => o.label)).toEqual(['nine'])
  })
})
