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

import useSnackbar from '@/composables/useSnackbar'

// Consumes the notifd 'browser' notification method for the Vue app: the
// BrowserNotificationStrategy publishes {id, head, body} messages per user on
// the /notification/stream WebSocket. Legacy JSP pages have their own consumer
// (core/web-assets notifications app); this one covers all /ui pages. Desktop
// notifications are used when the user has granted permission, with an
// in-page snackbar as the fallback so the message is never silently dropped.

interface BrowserNotificationMessage {
  id?: string | string[]
  head?: string | string[]
  body?: string | string[]
}

const { showSnackBar } = useSnackbar()

let started = false

// the stream serializes each field as a single-element array
const unwrap = (value?: string | string[]): string | undefined => {
  if (Array.isArray(value)) {
    return value.length ? String(value[0]) : undefined
  }
  return value ?? undefined
}

// The service-worker path, mirroring core/web-assets (NMS-20200/#8769):
// registration.showNotification works everywhere including Chrome for
// Android, where the page-scoped Notification constructor throws.
const ACTIVATION_TIMEOUT_MS = 5000

let swReady: Promise<ServiceWorkerRegistration | null> | null = null

// showNotification rejects with InvalidStateError until the worker is ACTIVE,
// so resolve only on activation — bounded, because a worker stuck installing
// must not park messages — and with null on redundancy, as in core/web-assets'
// withServiceWorker. navigator.serviceWorker.ready is the wrong tool here: it
// never settles when registration failed.
const awaitActivation = (registration: ServiceWorkerRegistration): Promise<ServiceWorkerRegistration | null> =>
  new Promise((resolve) => {
    if (registration.active) {
      resolve(registration)
      return
    }
    const pending = registration.installing ?? registration.waiting
    if (!pending) {
      resolve(null)
      return
    }
    let settled = false
    const finish = (value: ServiceWorkerRegistration | null) => {
      if (!settled) {
        settled = true
        resolve(value)
      }
    }
    pending.addEventListener('statechange', () => {
      if (pending.state === 'activated') {
        finish(registration)
      } else if (pending.state === 'redundant') {
        finish(null)
      }
    })
    setTimeout(() => finish(null), ACTIVATION_TIMEOUT_MS)
  })

// notification-sw.js is served at the webapp root (shipped by NMS-20200);
// registration is idempotent and the promise is cached, so calling this per
// message is free once settled
const withServiceWorker = (baseHref: string): Promise<ServiceWorkerRegistration | null> => {
  if (!('serviceWorker' in navigator)) {
    return Promise.resolve(null)
  }
  if (!swReady) {
    swReady = navigator.serviceWorker.register(`${baseHref}notification-sw.js`)
      .then(awaitActivation, () => null)
  }
  return swReady
}

const display = (baseHref: string, message: BrowserNotificationMessage) => {
  const head = unwrap(message.head) ?? 'OpenNMS Notification'
  const body = unwrap(message.body)
  const fallbackToSnackbar = () => showSnackBar({ msg: body ? `${head} — ${body}` : head, timeout: 8000 })

  if (!('Notification' in window) || Notification.permission !== 'granted') {
    fallbackToSnackbar()
    return
  }
  const options = {
    body: body ?? '',
    tag: `opennms:notification:${unwrap(message.id) ?? head}`
  }
  // the worker registers on demand, so a permission granted mid-session (the
  // opt-in lives on other pages) reaches the worker without a reload
  withServiceWorker(baseHref).then((registration) => {
    if (registration) {
      // two-arg then, matching core/web-assets: a rejection must still land
      // the message in the snackbar
      registration.showNotification(head, options).then(null, fallbackToSnackbar)
      return
    }
    try {
      // throws on Chrome for Android; fallback only, as in core/web-assets
      new Notification(head, options)
    } catch {
      fallbackToSnackbar()
    }
  })
}

// Bounded backoff: a session that keeps being rejected (expired auth, a proxy
// that blocks WebSockets) must not retry every 5s forever. A successful open
// resets the counter, so ordinary server restarts keep reconnecting.
const MAX_RECONNECT_ATTEMPTS = 10
let reconnectAttempts = 0

const connect = (baseHref: string) => {
  let ws: WebSocket
  try {
    ws = new WebSocket(`${baseHref}notification/stream`.replace(/^http/, 'ws'))
  } catch {
    // a base-url without a scheme makes the URL invalid; there is nothing to
    // reconnect to, and throwing would escape the caller's watcher
    return
  }

  ws.onopen = () => {
    reconnectAttempts = 0
  }

  ws.onmessage = (event: MessageEvent) => {
    try {
      display(baseHref, JSON.parse(event.data))
    } catch {
      // not a notification payload; ignore
    }
  }

  ws.onclose = () => {
    if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
      return
    }
    reconnectAttempts++
    setTimeout(() => connect(baseHref), Math.min(5000 * reconnectAttempts, 30000))
  }
}

const startBrowserNotifications = (baseHref: string) => {
  if (started || !baseHref) {
    return
  }
  started = true
  // No permission request here: Safari's callback-only requestPermission has
  // no Promise to .catch and Firefox ignores requests without a user gesture.
  // The click-triggered opt-in from NMS-20200 (core/web-assets) owns granting;
  // permission is origin-wide, and until granted the snackbar carries every
  // message. Desktop delivery goes through the NMS-20200 service worker,
  // warmed up here when permission is already granted so the first message
  // doesn't pay the activation wait.
  if ('Notification' in window && Notification.permission === 'granted') {
    withServiceWorker(baseHref)
  }
  connect(baseHref)
}

export default startBrowserNotifications
