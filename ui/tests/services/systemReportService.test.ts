import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { generateSystemReport } from '@/services/systemReportService'

const mockGet = vi.hoisted(() => vi.fn())
vi.mock('@/services/axiosInstances', () => ({
  v2: { defaults: { baseURL: '/opennms/api/v2' }, get: mockGet }
}))

describe('generateSystemReport', () => {
  let submitted: HTMLFormElement[]

  beforeEach(() => {
    vi.useFakeTimers()
    mockGet.mockResolvedValue({ data: [] })
    submitted = []
    vi.spyOn(HTMLFormElement.prototype, 'submit').mockImplementation(function (this: HTMLFormElement) {
      submitted.push(this)
    })
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.restoreAllMocks()
    document.cookie = 'systemReportDownload=; Max-Age=0'
    document.body.innerHTML = ''
  })

  const fieldsOf = (form: HTMLFormElement) =>
    Array.from(form.querySelectorAll('input')).map(i => [i.name, i.value])

  it('posts the request as a form into a hidden frame', async () => {
    generateSystemReport({ formatter: 'zip', plugins: ['Java', 'OS'], output: 'my.zip' })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)

    expect(submitted).toHaveLength(1)
    const form = submitted[0]
    expect(form.method.toLowerCase()).toBe('post')
    expect(form.action).toMatch(/\/opennms\/api\/v2\/system-report\/generate$/)
    expect(document.querySelector(`iframe[name="${form.target}"]`)).not.toBeNull()
    const fields = fieldsOf(form)
    expect(fields).toEqual(expect.arrayContaining([
      ['formatter', 'zip'], ['plugins', 'Java'], ['plugins', 'OS'], ['output', 'my.zip']
    ]))
    expect(fields.find(([n]) => n === 'downloadToken')?.[1]).toMatch(/^[a-z0-9]+$/)
  })

  it('resolves once the server echoes the download token in a cookie', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    const token = fieldsOf(submitted[0]).find(([n]) => n === 'downloadToken')?.[1]
    // no output field when the name is left to the server
    expect(fieldsOf(submitted[0]).some(([n]) => n === 'output')).toBe(false)

    document.cookie = `systemReportDownload=${token}`
    await vi.advanceTimersByTimeAsync(500)

    await expect(pending).resolves.toEqual({ success: true })
  })

  it('reports an expired session from the XHR check instead of posting the form', async () => {
    mockGet.mockRejectedValue({ response: { status: 401 }})
    const result = await generateSystemReport({ formatter: 'zip', plugins: ['Java'] })

    expect(result).toEqual({ success: false, message: expect.stringMatching(/session has expired/) })
    expect(submitted).toHaveLength(0)
    expect(document.querySelector('iframe')).toBeNull()
    expect(mockGet).toHaveBeenCalledWith('/system-report/formatters', { headers: { 'X-Requested-With': 'XMLHttpRequest' }})
  })

  // jsdom cannot load a real response into the frame, so fake the document it would hold
  const loadInFrame = (doc: { URL: string, contentType?: string, text?: string } | null) => {
    const iframe = document.querySelector('iframe') as HTMLIFrameElement
    Object.defineProperty(iframe, 'contentDocument', {
      configurable: true,
      value: doc && { URL: doc.URL, contentType: doc.contentType, readyState: 'complete', body: { textContent: doc.text ?? '' }}
    })
    iframe.dispatchEvent(new Event('load'))
  }

  const expectCleanedUp = async () => {
    await vi.advanceTimersByTimeAsync(0)
    expect(document.querySelector('iframe')).toBeNull()
    expect(vi.getTimerCount()).toBe(0)
  }

  it('reports the short text/plain reason the server renders into the frame, then cleans up', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: [] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    loadInFrame({ URL: 'http://localhost/opennms/api/v2/system-report/generate', contentType: 'text/plain', text: 'Select at least one report plugin.' })

    await expect(pending).resolves.toEqual({ success: false, message: 'Select at least one report plugin.' })
    await expectCleanedUp()
  })

  it('fails without a reason for an HTML error or login page', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    loadInFrame({ URL: 'http://localhost/opennms/api/v2/system-report/generate', contentType: 'text/html', text: 'HTTP ERROR 401 Unauthorized' })

    await expect(pending).resolves.toEqual({ success: false, message: undefined })
  })

  it('fails on an error response with an empty body', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    loadInFrame({ URL: 'http://localhost/opennms/api/v2/system-report/generate', contentType: 'text/html' })

    await expect(pending).resolves.toEqual({ success: false, message: undefined })
    await expectCleanedUp()
  })

  it('notices a failed frame from the poll when no load event fires (Firefox)', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    await vi.advanceTimersByTimeAsync(0)
    const iframe = document.querySelector('iframe') as HTMLIFrameElement
    Object.defineProperty(iframe, 'contentDocument', { configurable: true, value: null })
    await vi.advanceTimersByTimeAsync(500)

    await expect(pending).resolves.toEqual({ success: false })
    await expectCleanedUp()
  })

  it('fails when the frame ends up cross-origin (SSO redirect, browser error page)', async () => {
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    loadInFrame(null)

    await expect(pending).resolves.toEqual({ success: false })
    await expectCleanedUp()
  })

  it('ignores the frame\'s blank document, then succeeds on the cookie', async () => {
    let settled = false
    const pending = generateSystemReport({ formatter: 'text', plugins: ['Java'] })
    // let the session check settle so the form is submitted
    await vi.advanceTimersByTimeAsync(0)
    pending.then(() => {
      settled = true
    })
    const token = fieldsOf(submitted[0]).find(([n]) => n === 'downloadToken')?.[1]
    loadInFrame({ URL: 'about:blank', contentType: 'text/html' })
    await vi.advanceTimersByTimeAsync(1000)
    expect(settled).toBe(false)

    document.cookie = `systemReportDownload=${token}`
    await vi.advanceTimersByTimeAsync(500)
    await expect(pending).resolves.toEqual({ success: true })
    // the frame is kept for a while so removing it cannot cancel the download
    expect(document.querySelector('iframe')).not.toBeNull()
    await vi.advanceTimersByTimeAsync(60_000)
    await expectCleanedUp()
  })
})
