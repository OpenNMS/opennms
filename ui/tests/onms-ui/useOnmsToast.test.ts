import { beforeEach, describe, expect, it, vi } from 'vitest'

vi.mock('primevue/toasteventbus', () => ({ default: { emit: vi.fn() }}))

import { releaseActiveToast, useOnmsToast } from '@opennms/onms-ui'
// eslint-disable-next-line @typescript-eslint/ban-ts-comment
// @ts-ignore - no type declarations published for this entry point
import ToastEventBus from 'primevue/toasteventbus'

describe('useOnmsToast', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    vi.mocked(ToastEventBus.emit).mockClear()
    useOnmsToast().hideAllToasts()
    vi.mocked(ToastEventBus.emit).mockClear()
  })

  it('emits an add event with defaults (success, centered, 4s)', () => {
    useOnmsToast().showToast({ message: 'Saved' })
    expect(ToastEventBus.emit).toHaveBeenCalledWith('add', {
      severity: 'success',
      detail: 'Saved',
      life: 4000,
      closable: true,
      group: 'onms-toast-center'
    })
  })

  it('routes center: false to the start group and honors severity/timeout', () => {
    useOnmsToast().showToast({ message: 'Oops', severity: 'error', center: false, timeout: 1000 })
    expect(ToastEventBus.emit).toHaveBeenCalledWith('add', expect.objectContaining({
      severity: 'error',
      group: 'onms-toast-start',
      life: 1000
    }))
  })

  it('suppresses identical toasts while one is visible, then allows again after expiry', () => {
    const { showToast } = useOnmsToast()
    showToast({ message: 'dup' })
    showToast({ message: 'dup' })
    // PrimeVue-reality: ToastEventBus has no published types (see the @ts-ignore
    // above), so vi.mocked(...).mock.calls resolves under strict mode without an
    // inferable element type; annotate explicitly rather than weaken the check.
    expect(vi.mocked(ToastEventBus.emit).mock.calls.filter((c: unknown[]) => c[0] === 'add')).toHaveLength(1)
    vi.advanceTimersByTime(4001)
    showToast({ message: 'dup' })
    expect(vi.mocked(ToastEventBus.emit).mock.calls.filter((c: unknown[]) => c[0] === 'add')).toHaveLength(2)
  })

  it('hideAllToasts clears every group', () => {
    useOnmsToast().hideAllToasts()
    expect(ToastEventBus.emit).toHaveBeenCalledWith('remove-all-groups')
  })

  it('releaseActiveToast ends duplicate suppression immediately on dismissal', () => {
    const { showToast } = useOnmsToast()
    showToast({ message: 'dup' })
    releaseActiveToast({ severity: 'success', group: 'onms-toast-center', detail: 'dup' })
    showToast({ message: 'dup' })
    expect(vi.mocked(ToastEventBus.emit).mock.calls.filter((c: unknown[]) => c[0] === 'add')).toHaveLength(2)
  })

  it('releaseActiveToast is a no-op for an unknown key', () => {
    expect(() => releaseActiveToast({ severity: 'error', group: 'onms-toast-start', detail: 'never shown' })).not.toThrow()
  })
})
