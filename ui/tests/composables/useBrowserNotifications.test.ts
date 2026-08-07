import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const showSnackBar = vi.fn()
vi.mock('@/composables/useSnackbar', () => ({
  default: () => ({ showSnackBar })
}))

class FakeWebSocket {
  static instances: FakeWebSocket[] = []
  url: string
  onmessage: ((event: { data: string }) => void) | null = null
  onclose: (() => void) | null = null
  constructor(url: string) {
    this.url = url
    FakeWebSocket.instances.push(this)
  }
}

const start = async (baseHref = 'http://localhost:8980/opennms/') => {
  const mod = await import('@/composables/useBrowserNotifications')
  mod.default(baseHref)
}

describe('useBrowserNotifications', () => {
  beforeEach(() => {
    vi.resetModules()
    vi.useFakeTimers()
    FakeWebSocket.instances = []
    vi.stubGlobal('WebSocket', FakeWebSocket)
    showSnackBar.mockClear()
  })

  afterEach(() => {
    vi.unstubAllGlobals()
    vi.useRealTimers()
  })

  it('connects with the ws scheme and ignores non-notification payloads', async () => {
    await start()
    expect(FakeWebSocket.instances).toHaveLength(1)
    expect(FakeWebSocket.instances[0].url).toBe('ws://localhost:8980/opennms/notification/stream')

    FakeWebSocket.instances[0].onmessage?.({ data: 'not json' })
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('unwraps single-element arrays and falls back to the snackbar', async () => {
    const FakeNotification = { permission: 'denied' }
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))
    await start()

    FakeWebSocket.instances[0].onmessage?.({
      data: JSON.stringify({ id: ['42'], head: ['Node down'], body: ['Core-Router-01 is down'] })
    })

    expect(showSnackBar).toHaveBeenCalledWith(
      expect.objectContaining({ msg: 'Node down — Core-Router-01 is down' })
    )
  })

  it('uses a desktop notification when permission is granted', async () => {
    const notificationSpy = vi.fn()
    const FakeNotification = function (this: unknown, title: string, options: unknown) {
      notificationSpy(title, options)
    } as unknown as { permission: string }
    FakeNotification.permission = 'granted'
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))

    await start()
    FakeWebSocket.instances[0].onmessage?.({
      data: JSON.stringify({ id: ['7'], head: ['Head'], body: ['Body'] })
    })

    expect(notificationSpy).toHaveBeenCalledWith('Head', expect.objectContaining({ body: 'Body' }))
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  it('reconnects five seconds after the socket closes', async () => {
    await start()
    expect(FakeWebSocket.instances).toHaveLength(1)

    FakeWebSocket.instances[0].onclose?.()
    expect(FakeWebSocket.instances).toHaveLength(1)

    vi.advanceTimersByTime(5000)
    expect(FakeWebSocket.instances).toHaveLength(2)
  })

  it('starts once and refuses an empty baseHref', async () => {
    const mod = await import('@/composables/useBrowserNotifications')
    mod.default('')
    expect(FakeWebSocket.instances).toHaveLength(0)
    mod.default('http://localhost:8980/opennms/')
    mod.default('http://localhost:8980/opennms/')
    expect(FakeWebSocket.instances).toHaveLength(1)
  })
})
