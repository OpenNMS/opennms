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

import axios, { AxiosError, AxiosInstance, InternalAxiosRequestConfig } from 'axios'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'

const NOW = 1_000_000_000_000

// a fresh module per test, so its once-per-page-load flag and dialog state start clear
const loadModule = async () => {
  vi.resetModules()
  return await import('@/services/sessionExpiry')
}

// an axios instance whose every request fails with the given HTTP status (or a network error)
const failingInstance = (status?: number): AxiosInstance => {
  return axios.create({
    adapter: (config: InternalAxiosRequestConfig) => {
      const response = status === undefined
        ? undefined
        : { status, statusText: '', headers: {}, config, data: null }
      return Promise.reject(new AxiosError('failed', 'ERR', config, null, response))
    }
  })
}

// a minimal router: the module only registers navigation and error hooks
type To = { fullPath: string, path?: string }
type From = { path: string, matched: unknown[] }
const pathOf = (fullPath: string) => fullPath.split('?')[0]
const fakeRouter = () => {
  // the current route; matched is empty before the first navigation (vue-router START_LOCATION)
  let current: From = { path: '/', matched: [] }
  const before: ((to: To, from: From) => void)[] = []
  const after: (() => void)[] = []
  let onError: ((error: unknown, to: To) => void) | undefined
  return {
    beforeEach: (fn: (to: To, from: From) => void) => {
      before.push(fn)
      return () => undefined
    },
    afterEach: (fn: () => void) => {
      after.push(fn)
      return () => undefined
    },
    onError: (fn: (error: unknown, to: To) => void) => {
      onError = fn
      return () => undefined
    },
    navigate: (fullPath: string) => {
      before.forEach(fn => fn({ fullPath, path: pathOf(fullPath) }, current))
      current = { path: pathOf(fullPath), matched: [{}] }
    },
    arrive: () => after.forEach(fn => fn()),
    fail: (error: unknown, fullPath: string) => onError!(error, { fullPath })
  }
}

describe('sessionExpiry', () => {
  let reload: ReturnType<typeof vi.fn>
  let replaceState: ReturnType<typeof vi.spyOn>

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
    reload = vi.fn()
    vi.stubGlobal('location', { reload, pathname: '/opennms/ui/' } as any)
    replaceState = vi.spyOn(window.history, 'replaceState').mockImplementation(() => undefined)
    window.sessionStorage.clear()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  // the module, a router that has just started a navigation, and an instance failing with a 401
  const setup = async (status = 401) => {
    const module = await loadModule()
    const router = fakeRouter()
    module.installRouterSessionHandling(router as any)
    const instance = failingInstance(status)
    module.installSessionExpiryInterceptor(instance)
    return { ...module, router, instance }
  }

  describe('on a 401 just after a route change', () => {
    it('reloads at the target route and still rejects with the original error', async () => {
      const { router, instance, SESSION_EXPIRED_RELOAD_KEY, sessionExpiredDialogVisible } = await setup()
      router.navigate('/node/2')

      const error = await instance.get('/x').catch((e: unknown) => e)

      expect((error as AxiosError).response?.status).toBe(401)
      expect(replaceState).toHaveBeenCalledWith(window.history.state, '', '#/node/2')
      expect(reload).toHaveBeenCalledTimes(1)
      expect(window.sessionStorage.getItem(SESSION_EXPIRED_RELOAD_KEY)).toBe(String(NOW))
      expect(sessionExpiredDialogVisible.value).toBe(false)
    })

    it('reloads at the current route once the navigation is done', async () => {
      const { router, instance } = await setup()
      router.navigate('/node/2')
      router.arrive()

      await instance.get('/x').catch(() => undefined)

      expect(replaceState).not.toHaveBeenCalled()
      expect(reload).toHaveBeenCalledTimes(1)
    })

    it('reloads only once for concurrent 401s, and shows no dialog while the page reloads', async () => {
      const { router, instance, sessionExpiredDialogVisible } = await setup()
      router.navigate('/node/2')

      await Promise.allSettled([instance.get('/a'), instance.get('/b'), instance.get('/c')])

      expect(reload).toHaveBeenCalledTimes(1)
      expect(sessionExpiredDialogVisible.value).toBe(false)
    })

    it('shows no dialog for 401s at any time while the page reloads', async () => {
      const { router, instance, NAVIGATION_RELOAD_WINDOW_MS, sessionExpiredDialogVisible } = await setup()
      router.navigate('/node/2')
      await instance.get('/a').catch(() => undefined)
      expect(reload).toHaveBeenCalledTimes(1)

      // the reload has started; a later 401 (outside the navigation window) arrives before login.jsp does
      vi.setSystemTime(NOW + NAVIGATION_RELOAD_WINDOW_MS + 1)
      await instance.get('/b').catch(() => undefined)

      expect(sessionExpiredDialogVisible.value).toBe(false)
    })

    it('shows the dialog instead when the page was reloaded for this recently', async () => {
      const { router, instance, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS, sessionExpiredDialogVisible } = await setup()
      window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS + 1))
      router.navigate('/node/2')

      await instance.get('/x').catch(() => undefined)

      expect(reload).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })

    it('reloads again after the guard window', async () => {
      const { router, instance, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await setup()
      window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS - 1))
      router.navigate('/node/2')

      await instance.get('/x').catch(() => undefined)

      expect(reload).toHaveBeenCalledTimes(1)
    })

    it('still reloads for a later expiry after a 401 inside the guard window', async () => {
      const { router, instance, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await setup()
      window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - 1000))
      router.navigate('/node/2')
      await instance.get('/a').catch(() => undefined)
      expect(reload).not.toHaveBeenCalled()

      // much later, the session really expires during another route change
      vi.setSystemTime(NOW + RELOAD_GUARD_MS * 10)
      router.navigate('/node/3')
      await instance.get('/b').catch(() => undefined)

      expect(reload).toHaveBeenCalledTimes(1)
    })

    it('shows the dialog when the reload did not happen (e.g. cancelled by a beforeunload prompt)', async () => {
      const { router, instance, RELOAD_CANCELLED_AFTER_MS, sessionExpiredDialogVisible } = await setup()
      router.navigate('/node/2')
      await instance.get('/a').catch(() => undefined)
      expect(reload).toHaveBeenCalledTimes(1)

      // the page is still here long after the reload started
      vi.advanceTimersByTime(RELOAD_CANCELLED_AFTER_MS + 1)
      router.navigate('/node/3')
      await instance.get('/b').catch(() => undefined)

      expect(reload).toHaveBeenCalledTimes(1)
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })

    it('reloads once when sessionStorage throws', async () => {
      vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
        throw new Error('blocked')
      })
      vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
        throw new Error('blocked')
      })
      const { router, instance } = await setup()
      router.navigate('/node/2')

      await instance.get('/a').catch(() => undefined)
      await instance.get('/b').catch(() => undefined)

      expect(reload).toHaveBeenCalledTimes(1)
    })
  })

  describe('on a 401 at any other time (the user may have unsaved changes)', () => {
    it('shows the dialog after a change of the query only (e.g. a filter or a tab on the same page)', async () => {
      const { router, instance, NAVIGATION_RELOAD_WINDOW_MS, sessionExpiredDialogVisible } = await setup()
      router.navigate('/snmp-config')
      router.arrive()
      vi.setSystemTime(NOW + NAVIGATION_RELOAD_WINDOW_MS + 1)
      router.navigate('/snmp-config?tab=lookup')
      router.arrive()

      await instance.get('/x').catch(() => undefined)

      expect(reload).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })

    it('shows the dialog and does not reload', async () => {
      const { router, instance, NAVIGATION_RELOAD_WINDOW_MS, sessionExpiredDialogVisible } = await setup()
      router.navigate('/node/2')
      router.arrive()
      vi.setSystemTime(NOW + NAVIGATION_RELOAD_WINDOW_MS + 1)

      const error = await instance.get('/x').catch((e: unknown) => e)

      expect((error as AxiosError).response?.status).toBe(401)
      expect(reload).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })

    it('shows the dialog when no route change was seen', async () => {
      const { instance, sessionExpiredDialogVisible } = await setup()

      await instance.get('/x').catch(() => undefined)

      expect(reload).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })
  })

  it('ignores other statuses and network errors', async () => {
    const { router, installSessionExpiryInterceptor, sessionExpiredDialogVisible } = await setup()
    router.navigate('/node/2')
    for (const status of [403, 500, undefined]) {
      const instance = failingInstance(status)
      installSessionExpiryInterceptor(instance)
      await instance.get('/x').catch(() => undefined)
    }

    expect(reload).not.toHaveBeenCalled()
    expect(sessionExpiredDialogVisible.value).toBe(false)
  })

  describe('lazy route chunks', () => {
    it('reloads at the target route when its chunk fails to load', async () => {
      const { router } = await setup()

      router.fail(new TypeError('Failed to fetch dynamically imported module: http://x/assets/NodeDetails.js'), '/node/1?x=a:b')

      expect(replaceState).toHaveBeenCalledWith(window.history.state, '', '#/node/1?x=a:b')
      expect(reload).toHaveBeenCalledTimes(1)
    })

    it('recognises the chunk errors of each browser and of Vite', async () => {
      const messages = [
        'Failed to fetch dynamically imported module: x',
        'error loading dynamically imported module: x',
        'Importing a module script failed.',
        'Unable to preload CSS for /opennms/ui/assets/NodeDetails.css'
      ]
      for (const message of messages) {
        window.sessionStorage.clear()
        reload.mockClear()
        const { router } = await setup()

        router.fail(new Error(message), '/node/1')

        expect(reload, message).toHaveBeenCalledTimes(1)
      }
    })

    it('ignores other router errors', async () => {
      const { router, sessionExpiredDialogVisible } = await setup()

      router.fail(new Error('boom'), '/node/1')

      expect(reload).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(false)
    })

    it('shows the dialog instead when the page was reloaded for this recently', async () => {
      const { router, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS, sessionExpiredDialogVisible } = await setup()
      window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS + 1))

      router.fail(new TypeError('Failed to fetch dynamically imported module: x'), '/node/1')

      expect(reload).not.toHaveBeenCalled()
      expect(replaceState).not.toHaveBeenCalled()
      expect(sessionExpiredDialogVisible.value).toBe(true)
    })
  })

  describe('loginPageUrl', () => {
    it('is login.jsp in the web app context of the SPA', async () => {
      const { loginPageUrl } = await loadModule()
      for (const [pathname, expected] of [
        ['/opennms/ui/', '/opennms/login.jsp'],
        ['/opennms/ui/index.html', '/opennms/login.jsp'],
        ['/opennms/ui', '/opennms/login.jsp'],
        ['/nms/ui/', '/nms/login.jsp'],
        // a proxy prefix that also contains "/ui"
        ['/uim/opennms/ui/', '/uim/opennms/login.jsp'],
        ['/ui/opennms/ui/index.html', '/ui/opennms/login.jsp']
      ]) {
        vi.stubGlobal('location', { reload, pathname } as any)
        expect(loginPageUrl(), pathname).toBe(expected)
      }
    })
  })
})
