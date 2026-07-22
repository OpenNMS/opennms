import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const { emitMock } = vi.hoisted(() => ({ emitMock: vi.fn() }))

vi.mock('primevue/toasteventbus', () => ({
  default: { emit: emitMock }
}))

import useSnackbar from '@/composables/useSnackbar'

const addCalls = () => emitMock.mock.calls.filter(c => c[0] === 'add').map(c => c[1])

describe('useSnackbar', () => {
  beforeEach(() => {
    vi.useFakeTimers()
    emitMock.mockClear()
  })

  afterEach(() => {
    // Run pending dedup-expiry timers so module-level state is clean between tests.
    vi.runAllTimers()
    vi.useRealTimers()
  })

  it('suppresses identical toasts while one is still visible', () => {
    const { showSnackBar } = useSnackbar()
    showSnackBar({ msg: 'Please fix invalid values.', error: true })
    showSnackBar({ msg: 'Please fix invalid values.', error: true })
    showSnackBar({ msg: 'Please fix invalid values.', error: true })

    expect(addCalls()).toHaveLength(1)
  })

  it('stacks distinct messages', () => {
    const { showSnackBar } = useSnackbar()
    showSnackBar({ msg: 'First problem', error: true })
    showSnackBar({ msg: 'Second problem', error: true })

    expect(addCalls()).toHaveLength(2)
  })

  it('re-shows an identical message after the previous one expires', () => {
    const { showSnackBar } = useSnackbar()
    showSnackBar({ msg: 'Saved', timeout: 4000 })
    vi.advanceTimersByTime(4000)
    showSnackBar({ msg: 'Saved', timeout: 4000 })

    expect(addCalls()).toHaveLength(2)
  })

  it('maps error to severity and center to group', () => {
    const { showSnackBar } = useSnackbar()
    showSnackBar({ msg: 'err', error: true })
    showSnackBar({ msg: 'ok' })
    showSnackBar({ msg: 'left', center: false })

    const adds = addCalls()
    expect(adds[0]).toMatchObject({ severity: 'error', detail: 'err', group: 'snackbar-center' })
    expect(adds[1]).toMatchObject({ severity: 'success', detail: 'ok', group: 'snackbar-center' })
    expect(adds[2]).toMatchObject({ severity: 'success', detail: 'left', group: 'snackbar-start' })
  })

  it('hideSnackbar clears active toasts and allows an identical message to show again', () => {
    const { showSnackBar, hideSnackbar } = useSnackbar()
    showSnackBar({ msg: 'Saved' })
    hideSnackbar()
    showSnackBar({ msg: 'Saved' })

    expect(emitMock.mock.calls.some(c => c[0] === 'remove-all-groups')).toBe(true)
    expect(addCalls()).toHaveLength(2)
  })
})
