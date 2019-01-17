/**
 * @copyright 2016-2019 The OpenNMS Group, Inc.
 */

'use strict';

import Util from 'lib/util';
import $ from 'vendor/jquery-js';

$(function() {
    if ('Notification' in window) {
        let notificationSocket = null;

        let connect = function () {
            notificationSocket = new WebSocket((Util.getBaseHref() + 'notification/stream').replace(/^http/, 'ws'));

            notificationSocket.onclose = function () {
                setTimeout(connect, 1000);
            };
            notificationSocket.onerror = function () {
                setTimeout(connect, 5000);
            };
            notificationSocket.onmessage = function (event) {
                let message = JSON.parse(event.data);
                let notification = new Notification(message.head, {
                    body: message.body,
                    icon: Util.getBaseHref() + 'images/o-512.png',
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
