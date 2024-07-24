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
'use strict';

import Util from 'lib/util';
import $ from 'vendor/jquery-js';

$(function() {
    if ('Notification' in window) {
        let notificationSocket = null;

        let connect = function () {
            notificationSocket = new WebSocket((Util.getBaseHref() + 'notification/stream').replace(/^http/, 'ws'));

            notificationSocket.onclose = function (event) {
                notificationSocket.close();
                notificationSocket = null;

                setTimeout(connect, 1000);
            };

            notificationSocket.onmessage = function (event) {
                let message = JSON.parse(event.data);
                let notification = new Notification(message.head, {
                    body: message.body,
                    icon: Util.getBaseHref() + 'images/bluebird-512.png',
                    badge: Util.getBaseHref() + 'favicon.ico',
                    tag: 'opennms:notification:' + message.id
                });
            };
        };

        if (Notification.permission === 'granted') {
            connect();
        } else if (Notification.permission !== 'denied') {
            Notification.requestPermission()
                .then(function (permission) {
                    if (permission === 'granted') {
                        connect();
                    }
                });
        }
    }
});
