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
    await Promise.resolve()
    await Promise.resolve()

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

  // Safari's requestPermission is callback-only (no Promise to .catch) and
  // Firefox ignores requests without a user gesture; the NMS-20200 opt-in owns
  // granting, so this composable must never request permission itself.
  it('never calls requestPermission', async () => {
    const requestPermission = vi.fn()
    const FakeNotification = { permission: 'default', requestPermission }
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))

    await start()

    expect(requestPermission).not.toHaveBeenCalled()
    expect(FakeWebSocket.instances).toHaveLength(1)
  })

  it('delivers through the NMS-20200 service worker once it is active', async () => {
    const showNotification = vi.fn().mockReturnValue({ then: vi.fn() })
    const register = vi.fn().mockResolvedValue({ active: {}, showNotification })
    const FakeNotification = { permission: 'granted' }
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))
    vi.stubGlobal('navigator', Object.assign(Object.create(globalThis.navigator ?? {}), { serviceWorker: { register }}))

    await start()
    FakeWebSocket.instances[0].onmessage?.({ data: JSON.stringify({ id: ['1'], head: ['Head'], body: ['Body'] }) })
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()

    expect(register).toHaveBeenCalledWith('http://localhost:8980/opennms/notification-sw.js')
    expect(showNotification).toHaveBeenCalledWith('Head', expect.objectContaining({ body: 'Body' }))
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  // the worker registered but never activated: showNotification would reject
  // with InvalidStateError, so activation is awaited with a bounded timeout
  // and the message falls back to the page-scoped constructor
  it('waits for worker activation before using showNotification', async () => {
    const showNotification = vi.fn().mockReturnValue({ then: vi.fn() })
    const listenerRef: { fn?: () => void } = {}
    const installing = {
      state: 'installing',
      addEventListener: (_: string, listener: () => void) => {
        listenerRef.fn = listener
      }
    }
    const registration = { active: null as object | null, installing, showNotification }
    const register = vi.fn().mockResolvedValue(registration)
    const FakeNotification = { permission: 'granted' }
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))
    vi.stubGlobal('navigator', Object.assign(Object.create(globalThis.navigator ?? {}), { serviceWorker: { register }}))

    await start()
    FakeWebSocket.instances[0].onmessage?.({ data: JSON.stringify({ id: ['1'], head: ['Head'], body: ['Body'] }) })
    await Promise.resolve()
    await Promise.resolve()
    expect(showNotification).not.toHaveBeenCalled()

    installing.state = 'activated'
    listenerRef.fn?.()
    await Promise.resolve()
    await Promise.resolve()
    expect(showNotification).toHaveBeenCalledWith('Head', expect.objectContaining({ body: 'Body' }))
  })

  // the opt-in lives on other pages: permission can flip to granted after
  // this page loaded, and the worker must still be picked up without a reload
  it('registers the worker on demand after a mid-session permission grant', async () => {
    const showNotification = vi.fn().mockReturnValue({ then: vi.fn() })
    const register = vi.fn().mockResolvedValue({ active: {}, showNotification })
    const FakeNotification = { permission: 'default' }
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))
    vi.stubGlobal('navigator', Object.assign(Object.create(globalThis.navigator ?? {}), { serviceWorker: { register }}))

    await start()
    expect(register).not.toHaveBeenCalled()

    FakeNotification.permission = 'granted'
    FakeWebSocket.instances[0].onmessage?.({ data: JSON.stringify({ id: ['9'], head: ['Head'], body: ['Body'] }) })
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()
    await Promise.resolve()

    expect(register).toHaveBeenCalledWith('http://localhost:8980/opennms/notification-sw.js')
    expect(showNotification).toHaveBeenCalledWith('Head', expect.objectContaining({ body: 'Body' }))
    expect(showSnackBar).not.toHaveBeenCalled()
  })

  // Chrome for Android: the page-scoped constructor throws; the message must
  // land in the snackbar instead of being swallowed.
  it('falls back to the snackbar when the Notification constructor throws', async () => {
    const FakeNotification = function () {
      throw new Error('Illegal constructor')
    } as unknown as { permission: string }
    FakeNotification.permission = 'granted'
    vi.stubGlobal('Notification', FakeNotification)
    vi.stubGlobal('window', Object.assign(Object.create(globalThis), { Notification: FakeNotification }))

    await start()
    FakeWebSocket.instances[0].onmessage?.({ data: JSON.stringify({ id: ['2'], head: ['Head'], body: ['Body'] }) })
    await Promise.resolve()
    await Promise.resolve()

    expect(showSnackBar).toHaveBeenCalledWith(expect.objectContaining({ msg: 'Head — Body' }))
  })
})
