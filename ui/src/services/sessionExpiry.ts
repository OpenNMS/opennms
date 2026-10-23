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


import { AxiosError, AxiosInstance } from 'axios'
import { Router } from 'vue-router'

// When the server session expires, the REST APIs answer 401, and the code of a route that is not
// loaded yet cannot load (the server sends the login page instead). A full reload of the SPA page then
// goes through the server's login redirect: the browser keeps the hash route on login.jsp, login.jsp
// posts it, and the user comes back to the same route after login (see LoginRedirects.java).

export const SESSION_EXPIRED_RELOAD_KEY = 'onms.sessionExpiredReloadAt'

// If the page loads but the APIs still answer 401 (e.g. an unusual authentication setup), the
// reload does not help. Do not reload again within this time, so the page cannot reload in a loop.
export const RELOAD_GUARD_MS = 60_000

let reloading = false
let warned = false

const lastReloadAt = (): number => {
  try {
    return Number(window.sessionStorage.getItem(SESSION_EXPIRED_RELOAD_KEY)) || 0
  } catch {
    return 0
  }
}

const recordReload = () => {
  try {
    window.sessionStorage.setItem(SESSION_EXPIRED_RELOAD_KEY, String(Date.now()))
  } catch {
    // storage blocked: the module flag still stops more reloads from this page
  }
}

// Reload at most once per page load, and not again within RELOAD_GUARD_MS.
// Returns false if the guard stopped the reload.
const reloadOnce = (beforeReload?: () => void): boolean => {
  if (reloading) {
    return false
  }

  // Only skip this one: a later expiry, after the guard window, must still reload.
  if (Date.now() - lastReloadAt() < RELOAD_GUARD_MS) {
    if (!warned) {
      warned = true
      console.warn('Session expired or page code failed to load, but the page was reloaded for this recently; not reloading again.')
    }
    return false
  }

  reloading = true
  recordReload()
  beforeReload?.()
  window.location.reload()
  return true
}

export const handleSessionExpired = (): void => {
  reloadOnce()
}

// The messages for a failed dynamic import differ per browser; Vite adds its own for route CSS.
const CHUNK_LOAD_ERROR = /Failed to fetch dynamically imported module|error loading dynamically imported module|Importing a module script failed|Unable to preload CSS/

// A route whose code cannot load: reload at that route, not at the route the user is leaving.
export const installChunkLoadRecovery = (router: Pick<Router, 'onError'>): void => {
  router.onError((error, to) => {
    if (CHUNK_LOAD_ERROR.test(String(error?.message ?? error))) {
      reloadOnce(() => window.history.replaceState(window.history.state, '', '#' + to.fullPath))
    }
  })
}

export const installSessionExpiryInterceptor = (...instances: AxiosInstance[]): void => {
  for (const instance of instances) {
    instance.interceptors.response.use(undefined, (error: AxiosError) => {
      if (error.response?.status === 401) {
        handleSessionExpired()
      }
      return Promise.reject(error)
    })
  }
}
