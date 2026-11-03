import useActionFeedback from '@/composables/useActionFeedback'
import useSpinner from '@/composables/useSpinner'
import { createFailureResult, createSuccessResponse } from '@/types/validation'
import { beforeEach, describe, expect, it, vi } from 'vitest'

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

describe('useActionFeedback', () => {
  beforeEach(() => {
    showSnackBar.mockClear()
    useSpinner().stopSpinner()
  })

  describe('withSpinner', () => {
    it('shows the spinner for the whole action and returns its value', async () => {
      const { isActive } = useSpinner()
      const { withSpinner } = useActionFeedback()
      let activeDuring = false

      const value = await withSpinner(async () => {
        activeDuring = isActive.value
        return 42
      })

      expect(activeDuring).toBe(true)
      expect(value).toBe(42)
      expect(isActive.value).toBe(false)
    })

    it('stops the spinner even when the action throws', async () => {
      const { isActive } = useSpinner()
      const { withSpinner } = useActionFeedback()

      await expect(withSpinner(() => Promise.reject(new Error('boom')))).rejects.toThrow('boom')
      expect(isActive.value).toBe(false)
    })
  })

  describe('report', () => {
    it('shows a failure message as an error and returns false', () => {
      const { report } = useActionFeedback()

      expect(report(createFailureResult('Nope.'), 'Saved.')).toBe(false)

      expect(showSnackBar).toHaveBeenCalledTimes(1)
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Nope.', error: true })
    })

    it('shows the success message and returns true', () => {
      const { report } = useActionFeedback()

      expect(report(createSuccessResponse(), 'Saved.')).toBe(true)

      expect(showSnackBar).toHaveBeenCalledTimes(1)
      expect(showSnackBar).toHaveBeenCalledWith({ msg: 'Saved.' })
    })

    it('shows nothing for a success without a message', () => {
      const { report } = useActionFeedback()

      expect(report(createSuccessResponse())).toBe(true)
      expect(showSnackBar).not.toHaveBeenCalled()
    })

    it('shows follow-up errors (e.g. a failed refresh) after the success message', () => {
      const { report } = useActionFeedback()
      const result = { ...createSuccessResponse(), errors: ['Failed to load destination paths.'] }

      expect(report(result, 'Destination path added.')).toBe(true)

      expect(showSnackBar.mock.calls).toEqual([
        [{ msg: 'Destination path added.' }],
        [{ msg: 'Failed to load destination paths.', error: true }]
      ])
    })
  })

  it('showError always sets error: true; showSuccess never does', () => {
    const { showError, showSuccess } = useActionFeedback()

    showError('Bad.')
    showSuccess('Good.')

    expect(showSnackBar.mock.calls).toEqual([
      [{ msg: 'Bad.', error: true }],
      [{ msg: 'Good.' }]
    ])
  })
})
