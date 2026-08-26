/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */

/*
 * Service worker backing browser notifications. Exists only because Chrome for Android
 * throws from the page-scoped Notification constructor.
 *
 * No 'fetch' handler on purpose: it must never intercept or cache application requests.
 * Served from the webapp root so its scope covers the whole context.
 */
'use strict';

self.addEventListener('install', function(event) {
    // Don't wait for every OpenNMS tab to close.
    event.waitUntil(self.skipWaiting());
});

self.addEventListener('activate', function(event) {
    event.waitUntil(self.clients.claim());
});

self.addEventListener('notificationclick', function(event) {
    event.notification.close();

    const target = event.notification.data && event.notification.data.url;
    if (!target) {
        return;
    }

    // includeUncontrolled: pages loaded before this worker activated are still worth focusing.
    event.waitUntil(self.clients.matchAll({type: 'window', includeUncontrolled: true}).then(function(clients) {
        for (const client of clients) {
            if (client.url === target && 'focus' in client) {
                return client.focus();
            }
        }
        return self.clients.openWindow(target);
    }));
});
