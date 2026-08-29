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

const display = (message: BrowserNotificationMessage) => {
  const head = unwrap(message.head) ?? 'OpenNMS Notification'
  const body = unwrap(message.body)
  if ('Notification' in window && Notification.permission === 'granted') {
    new Notification(head, {
      body: body ?? '',
      tag: `opennms:notification:${unwrap(message.id) ?? head}`
    })
  } else {
    showSnackBar({ msg: body ? `${head} — ${body}` : head, timeout: 8000 })
  }
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
      display(JSON.parse(event.data))
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
  if ('Notification' in window && Notification.permission === 'default') {
    // fire-and-forget; the snackbar fallback covers an undecided/denied state
    Notification.requestPermission().catch(() => undefined)
  }
  connect(baseHref)
}

export default startBrowserNotifications
