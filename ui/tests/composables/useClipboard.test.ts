import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

import { copyToClipboard } from '@/composables/useClipboard'

// The reason this composable exists rather than a bare navigator.clipboard call:
// an OpenNMS server reached over plain HTTP on anything but localhost is not a
// secure context, so the modern API is absent and the textarea path is what runs.
describe('copyToClipboard', () => {
  const originalClipboard = Object.getOwnPropertyDescriptor(navigator, 'clipboard')

  const setContext = (secure: boolean, clipboard: unknown) => {
    Object.defineProperty(window, 'isSecureContext', { value: secure, configurable: true })
    Object.defineProperty(navigator, 'clipboard', { value: clipboard, configurable: true })
  }

  beforeEach(() => {
    document.body.innerHTML = ''
  })

  afterEach(() => {
    if (originalClipboard) {
      Object.defineProperty(navigator, 'clipboard', originalClipboard)
    }
    vi.restoreAllMocks()
  })

  it('uses the clipboard API in a secure context', async () => {
    const writeText = vi.fn().mockResolvedValue(undefined)
    setContext(true, { writeText })

    await copyToClipboard('https://example.com/#/adhoc-graphs?s=a')

    expect(writeText).toHaveBeenCalledWith('https://example.com/#/adhoc-graphs?s=a')
    expect(document.querySelector('textarea')).toBeNull()
  })

  it('falls back to a textarea when the context is not secure', async () => {
    const writeText = vi.fn()
    setContext(false, { writeText })
    const execCommand = vi.fn().mockReturnValue(true)
    document.execCommand = execCommand as never

    await copyToClipboard('plain-http-link')

    expect(writeText).not.toHaveBeenCalled()
    expect(execCommand).toHaveBeenCalledWith('copy')
  })

  it('falls back when the clipboard API is missing entirely', async () => {
    setContext(true, undefined)
    const execCommand = vi.fn().mockReturnValue(true)
    document.execCommand = execCommand as never

    await copyToClipboard('no-clipboard-api')

    expect(execCommand).toHaveBeenCalledWith('copy')
  })

  it('rejects, and leaves no textarea behind, when the copy command fails', async () => {
    setContext(false, undefined)
    document.execCommand = vi.fn().mockReturnValue(false) as never

    await expect(copyToClipboard('nope')).rejects.toThrow()
    expect(document.querySelector('textarea')).toBeNull()
  })

  it('cleans up the textarea even when execCommand throws', async () => {
    setContext(false, undefined)
    document.execCommand = vi.fn(() => {
      throw new Error('blocked')
    }) as never

    await expect(copyToClipboard('nope')).rejects.toThrow('blocked')
    expect(document.querySelector('textarea')).toBeNull()
  })
})
