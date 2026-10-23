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
import { ref } from 'vue'
import { Router } from 'vue-router'

// When the server session expires, the REST APIs answer 401, and the code of a route that is not
// loaded yet cannot load. A full reload of the SPA page goes through the server's login redirect:
// the browser keeps the hash route on login.jsp, login.jsp posts it, and the user comes back to the
// same route after login (see LoginRedirects.java).
//
// A reload loses what the user changed on the page and did not save. So reload by itself only
// during a route change, when the user has not changed anything on the new page yet. At any other
// time, show SessionExpiredDialog: the user can log in in a new tab and keep the page.

export const SESSION_EXPIRED_RELOAD_KEY = 'onms.sessionExpiredReloadAt'

// If the page loads but the APIs still answer 401 (e.g. an unusual authentication setup), a reload
// does not help. Do not reload again within this time, so the page cannot reload in a loop.
export const RELOAD_GUARD_MS = 60_000

// A 401 this soon after a route change to another page started comes from loading the new page.
export const NAVIGATION_RELOAD_WINDOW_MS = 5_000

// If the page is still here this long after a reload started, a beforeunload prompt cancelled it.
export const RELOAD_CANCELLED_AFTER_MS = 10_000

// read by SessionExpiredDialog.vue
export const sessionExpiredDialogVisible = ref(false)

let reloading = false
let warned = false
let lastNavigationAt = 0
// the target of the route change in progress, if any
let navigationTarget: string | undefined

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

const showSessionExpiredDialog = () => {
  sessionExpiredDialogVisible.value = true
}

// Reload at the given route (or the current one), at most once per page load and not again within
// RELOAD_GUARD_MS. Returns true if a reload is running (started now or before): the login page comes
// next, so no dialog. Returns false if the guard stopped it: the caller then shows the dialog.
const reloadAt = (fullPath?: string): boolean => {
  if (reloading) {
    return true
  }

  if (Date.now() - lastReloadAt() < RELOAD_GUARD_MS) {
    if (!warned) {
      warned = true
      console.warn('Session expired or page code failed to load, but the page was reloaded for this recently; not reloading again.')
    }
    return false
  }

  reloading = true
  recordReload()
  if (fullPath) {
    window.history.replaceState(window.history.state, '', '#' + fullPath)
  }
  window.location.reload()
  // Still here later: a beforeunload prompt cancelled the reload. The next 401 then meets the guard
  // and shows the dialog, instead of waiting for a reload that does not come.
  window.setTimeout(() => {
    reloading = false
  }, RELOAD_CANCELLED_AFTER_MS)
  return true
}

export const handleSessionExpired = (): void => {
  // a reload is running: the other requests of the page fail too, but the login page comes next
  if (reloading) {
    return
  }
  const duringNavigation = Date.now() - lastNavigationAt < NAVIGATION_RELOAD_WINDOW_MS
  if (duringNavigation && reloadAt(navigationTarget)) {
    return
  }
  showSessionExpiredDialog()
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

// The messages for a failed dynamic import differ per browser; Vite adds its own for route CSS.
const CHUNK_LOAD_ERROR = /Failed to fetch dynamically imported module|error loading dynamically imported module|Importing a module script failed|Unable to preload CSS/

// Track route changes for handleSessionExpired. When the code of a route cannot load, reload at
// that route, not at the route the user is leaving.
export const installRouterSessionHandling = (router: Pick<Router, 'beforeEach' | 'afterEach' | 'onError'>): void => {
  router.beforeEach((to, from) => {
    // Only a change to another page counts (or the first navigation, from vue-router's START_LOCATION).
    // A change of the query only keeps the page, e.g. a filter or a tab, and maybe unsaved changes.
    if (from.matched.length === 0 || to.path !== from.path) {
      lastNavigationAt = Date.now()
      navigationTarget = to.fullPath
    }
  })
  router.afterEach(() => {
    navigationTarget = undefined
  })
  router.onError((error, to) => {
    if (CHUNK_LOAD_ERROR.test(String(error?.message ?? error)) && !reloadAt(to.fullPath)) {
      showSessionExpiredDialog()
    }
  })
}

// the last "/ui" path segment: a proxy prefix can contain "/ui" too, e.g. /uim/opennms/ui/
const SPA_PATH = /^(.*)\/ui(?:\/|$)/

// login.jsp is in the web app context, the parent of the SPA's /ui path
export const loginPageUrl = (): string => {
  const match = SPA_PATH.exec(window.location.pathname)
  return (match ? match[1] : '') + '/login.jsp'
}
