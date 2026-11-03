import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { defineComponent, h } from 'vue'
import { mount } from '@vue/test-utils'
import { useDashboardStore } from '@/stores/dashboardStore'
import { useDashboardRefresh } from '@/composables/useDashboardRefresh'

const Host = defineComponent({
  setup() {
    useDashboardRefresh()
    return () => h('div')
  }
})

describe('useDashboardRefresh', () => {
  let store: ReturnType<typeof useDashboardStore>

  beforeEach(() => {
    vi.useFakeTimers()
    setActivePinia(createPinia())
    store = useDashboardStore()
    store.layout.refresh.seconds = 30
    store.layout.refresh.paused = false
  })

  afterEach(() => {
    vi.useRealTimers()
  })

  it('ticks on the configured cadence', async () => {
    const wrapper = mount(Host)
    await wrapper.vm.$nextTick()
    expect(store.refreshTick).toBe(0)
    vi.advanceTimersByTime(30_000)
    expect(store.refreshTick).toBe(1)
    vi.advanceTimersByTime(60_000)
    expect(store.refreshTick).toBe(3)
    wrapper.unmount()
  })

  it('suspends while paused and resumes on unpause', async () => {
    const wrapper = mount(Host)
    store.togglePaused()
    await wrapper.vm.$nextTick()
    vi.advanceTimersByTime(120_000)
    expect(store.refreshTick).toBe(0)

    store.togglePaused()
    await wrapper.vm.$nextTick()
    vi.advanceTimersByTime(30_000)
    expect(store.refreshTick).toBe(1)
    wrapper.unmount()
  })

  it('suspends in edit mode and with the interval off', async () => {
    const wrapper = mount(Host)
    store.setEditMode(true)
    await wrapper.vm.$nextTick()
    vi.advanceTimersByTime(120_000)
    expect(store.refreshTick).toBe(0)

    store.setEditMode(false)
    store.setRefreshSeconds(0)
    await wrapper.vm.$nextTick()
    vi.advanceTimersByTime(120_000)
    expect(store.refreshTick).toBe(0)
    wrapper.unmount()
  })

  it('stops ticking after unmount', async () => {
    const wrapper = mount(Host)
    await wrapper.vm.$nextTick()
    wrapper.unmount()
    vi.advanceTimersByTime(120_000)
    expect(store.refreshTick).toBe(0)
  })
})
