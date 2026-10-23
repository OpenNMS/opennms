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

// a fresh module per test, so its once-per-page-load flag starts clear
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

describe('sessionExpiry', () => {
  let reload: ReturnType<typeof vi.fn>

  beforeEach(() => {
    vi.useFakeTimers()
    vi.setSystemTime(NOW)
    reload = vi.fn()
    vi.stubGlobal('location', { reload } as any)
    window.sessionStorage.clear()
  })

  afterEach(() => {
    vi.useRealTimers()
    vi.unstubAllGlobals()
    vi.restoreAllMocks()
  })

  it('reloads once on a 401 and still rejects with the original error', async () => {
    const { installSessionExpiryInterceptor, SESSION_EXPIRED_RELOAD_KEY } = await loadModule()
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    const error = await instance.get('/x').catch((e: unknown) => e)

    expect(error).toBeInstanceOf(AxiosError)
    expect((error as AxiosError).response?.status).toBe(401)
    expect(reload).toHaveBeenCalledTimes(1)
    expect(window.sessionStorage.getItem(SESSION_EXPIRED_RELOAD_KEY)).toBe(String(NOW))
  })

  it('reloads only once for concurrent 401s', async () => {
    const { installSessionExpiryInterceptor } = await loadModule()
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    await Promise.allSettled([instance.get('/a'), instance.get('/b')])

    expect(reload).toHaveBeenCalledTimes(1)
  })

  it('does not reload again within the guard window', async () => {
    const { installSessionExpiryInterceptor, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await loadModule()
    window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS + 1))
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    await instance.get('/x').catch(() => undefined)

    expect(reload).not.toHaveBeenCalled()
  })

  it('still reloads for a later expiry after a 401 inside the guard window', async () => {
    const { installSessionExpiryInterceptor, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await loadModule()
    window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - 1000))
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    // just after a reload: guarded, no reload
    await instance.get('/a').catch(() => undefined)
    expect(reload).not.toHaveBeenCalled()

    // much later, the session really expires
    vi.setSystemTime(NOW + RELOAD_GUARD_MS * 10)
    await instance.get('/b').catch(() => undefined)
    expect(reload).toHaveBeenCalledTimes(1)
  })

  it('reloads again after the guard window', async () => {
    const { installSessionExpiryInterceptor, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await loadModule()
    window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS - 1))
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    await instance.get('/x').catch(() => undefined)

    expect(reload).toHaveBeenCalledTimes(1)
  })

  it('ignores other statuses and network errors', async () => {
    const { installSessionExpiryInterceptor } = await loadModule()
    for (const status of [403, 500, undefined]) {
      const instance = failingInstance(status)
      installSessionExpiryInterceptor(instance)
      await instance.get('/x').catch(() => undefined)
    }

    expect(reload).not.toHaveBeenCalled()
  })

  it('reloads once when sessionStorage throws', async () => {
    const { installSessionExpiryInterceptor } = await loadModule()
    vi.spyOn(Storage.prototype, 'getItem').mockImplementation(() => {
      throw new Error('blocked')
    })
    vi.spyOn(Storage.prototype, 'setItem').mockImplementation(() => {
      throw new Error('blocked')
    })
    const instance = failingInstance(401)
    installSessionExpiryInterceptor(instance)

    await instance.get('/a').catch(() => undefined)
    await instance.get('/b').catch(() => undefined)

    expect(reload).toHaveBeenCalledTimes(1)
  })

  describe('lazy route chunks', () => {
    // a minimal router: installChunkLoadRecovery only registers an error handler
    const fakeRouter = () => {
      let handler: ((error: unknown, to: { fullPath: string }) => void) | undefined
      return {
        onError: (fn: (error: unknown, to: { fullPath: string }) => void) => {
          handler = fn
          return () => undefined
        },
        fail: (error: unknown, fullPath: string) => handler!(error, { fullPath })
      }
    }

    it('reloads at the target route when its chunk fails to load', async () => {
      const { installChunkLoadRecovery } = await loadModule()
      const replaceState = vi.spyOn(window.history, 'replaceState').mockImplementation(() => undefined)
      const router = fakeRouter()
      installChunkLoadRecovery(router as any)

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
        const { installChunkLoadRecovery } = await loadModule()
        vi.spyOn(window.history, 'replaceState').mockImplementation(() => undefined)
        window.sessionStorage.clear()
        reload.mockClear()
        const router = fakeRouter()
        installChunkLoadRecovery(router as any)

        router.fail(new Error(message), '/node/1')

        expect(reload, message).toHaveBeenCalledTimes(1)
      }
    })

    it('ignores other router errors', async () => {
      const { installChunkLoadRecovery } = await loadModule()
      const router = fakeRouter()
      installChunkLoadRecovery(router as any)

      router.fail(new Error('boom'), '/node/1')

      expect(reload).not.toHaveBeenCalled()
    })

    it('does not reload again within the guard window', async () => {
      const { installChunkLoadRecovery, SESSION_EXPIRED_RELOAD_KEY, RELOAD_GUARD_MS } = await loadModule()
      window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(NOW - RELOAD_GUARD_MS + 1))
      const replaceState = vi.spyOn(window.history, 'replaceState').mockImplementation(() => undefined)
      const router = fakeRouter()
      installChunkLoadRecovery(router as any)

      router.fail(new TypeError('Failed to fetch dynamically imported module: x'), '/node/1')

      expect(reload).not.toHaveBeenCalled()
      expect(replaceState).not.toHaveBeenCalled()
    })
  })
})
