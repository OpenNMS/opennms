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

const SERVICE_WORKER_PATH = 'notification-sw.js';
const STREAM_PATH = 'notification/stream';
const NOTICE_LIST_PATH = 'notification/browse?acktype=unack';
const RECONNECT_MIN_MS = 1000;
const RECONNECT_MAX_MS = 30000;
const ACTIVATION_TIMEOUT_MS = 5000;

let socket = null;
let serviceWorker = null;
let connectionWanted = false;
let reconnectDelayMs = RECONNECT_MIN_MS;
let promptSuppressed = false;

const supported = () => 'Notification' in window;

const permission = () => (supported() ? Notification.permission : 'unsupported');

const targetUrl = (message) => {
    if (message.noticeId) {
        return Util.getBaseHref() + 'notification/detail.jsp?notice=' + encodeURIComponent(message.noticeId);
    }
    return Util.getBaseHref() + NOTICE_LIST_PATH;
};

const show = (message) => {
    const options = {
        body: message.body,
        icon: Util.getBaseHref() + 'images/o-512.png',
        badge: Util.getBaseHref() + 'favicon.ico',
        // Collapses repeat deliveries of one notice, including across tabs.
        tag: 'opennms:notification:' + message.id,
        data: { url: targetUrl(message) }
    };

    if (serviceWorker !== null) {
        // Two-arg then() rather than catch(), and no reference to the Promise global anywhere in
        // this module: the corejs transform swaps Promise for a build that lacks resolve().
        serviceWorker.showNotification(message.head, options).then(null, (e) => {
            console.warn('unable to show notification', e); // eslint-disable-line no-console
        });
        return;
    }

    // Throws on Chrome for Android, so this is a fallback only.
    try {
        const notification = new Notification(message.head, options);
        notification.onclick = () => {
            window.focus();
            window.location.assign(options.data.url);
        };
    } catch (e) {
        console.warn('unable to show notification', e); // eslint-disable-line no-console
    }
};

const disconnect = () => {
    connectionWanted = false;
    if (socket !== null) {
        const closing = socket;
        socket = null;
        closing.close();
    }
};

const connect = () => {
    if (!connectionWanted) {
        return;
    }

    socket = new WebSocket((Util.getBaseHref() + STREAM_PATH).replace(/^http/, 'ws'));

    socket.onopen = () => {
        reconnectDelayMs = RECONNECT_MIN_MS;
    };

    socket.onmessage = (event) => {
        // Permission can be revoked while the socket is open.
        if (permission() !== 'granted') {
            disconnect();
            return;
        }
        show(JSON.parse(event.data));
    };

    socket.onclose = () => {
        socket = null;
        // Back off with jitter so that every open tab does not retry once a
        // second for the duration of an OpenNMS restart.
        const delay = reconnectDelayMs * (0.5 + Math.random());
        reconnectDelayMs = Math.min(reconnectDelayMs * 2, RECONNECT_MAX_MS);
        window.setTimeout(connect, delay);
    };
};

// Calls back with the registration once its worker is active, or null to use the page-scoped
// constructor instead. Not navigator.serviceWorker.ready, which never rejects and so would
// hang forever if the worker failed to activate.
const withServiceWorker = (callback) => {
    if (!('serviceWorker' in navigator)) {
        callback(null);
        return;
    }

    const base = Util.getBaseHref();
    if (base === '') {
        // Without <base href> the worker would take a scope narrower than the application.
        callback(null);
        return;
    }

    navigator.serviceWorker.register(base + SERVICE_WORKER_PATH).then((registration) => {
        if (registration.active) {
            callback(registration);
            return;
        }

        const pending = registration.installing || registration.waiting;
        if (!pending) {
            callback(null);
            return;
        }

        let settled = false;
        const finish = (value) => {
            if (!settled) {
                settled = true;
                callback(value);
            }
        };

        pending.addEventListener('statechange', () => {
            if (pending.state === 'activated') {
                finish(registration);
            } else if (pending.state === 'redundant') {
                finish(null);
            }
        });
        window.setTimeout(() => finish(null), ACTIVATION_TIMEOUT_MS);
    }, (e) => {
        console.warn('notification service worker registration failed', e); // eslint-disable-line no-console
        callback(null);
    });
};

const start = () => {
    if (connectionWanted || permission() !== 'granted') {
        return;
    }
    connectionWanted = true;

    withServiceWorker((registration) => {
        serviceWorker = registration;
        connect();
    });
};

// Firefox rejects requestPermission() outright without a user gesture since 72, so the
// prompt hangs off a click rather than page load.
const renderOptIn = ($container) => {
    // $(sel, ctx) rather than ctx.find(sel): the corejs transform rewrites .find() into an
    // Array.prototype.find helper, which throws on a jQuery object.
    const $status = $('.onms-notification-optin-status', $container);
    const $button = $('.onms-notification-optin-button', $container);

    if (!supported()) {
        $status.text(window.isSecureContext
            ? 'This browser does not support notifications.'
            : 'Browser notifications require that OpenNMS be served over HTTPS.');
        $button.addClass('d-none');
        return;
    }

    switch (Notification.permission) {
        case 'granted':
            $status.text('Enabled in this browser. Notices appear while an OpenNMS page is open.');
            $button.addClass('d-none');
            break;
        case 'denied':
            $status.text('Blocked in this browser. Allow notifications for this site in your browser settings to turn them back on.');
            $button.addClass('d-none');
            break;
        default:
            // Chromium and Edge resolve requestPermission() without asking when they
            // suppress the prompt; the only affordance left is in the address bar.
            $status.text(promptSuppressed
                ? 'This browser did not show the permission request. Check for a blocked notifications icon in the address bar, or allow notifications for this site in your browser settings.'
                : 'Not enabled in this browser.');
            $button.removeClass('d-none');
            break;
    }
};

// Older Safari only has the callback form and returns undefined, so calling .then() on the
// result would throw. Browsers that honour both would otherwise invoke the callback twice.
const requestPermission = (callback) => {
    let called = false;
    const once = (result) => {
        if (!called) {
            called = true;
            callback(result);
        }
    };

    const returned = Notification.requestPermission(once);
    if (returned && typeof returned.then === 'function') {
        returned.then(once);
    }
};

const wireOptIn = ($container) => {
    $('.onms-notification-optin-button', $container).on('click', () => {
        requestPermission((result) => {
            if (result === 'granted') {
                start();
            }
            promptSuppressed = result === 'default';
            renderOptIn($container);
        });
    });

    renderOptIn($container);
    $container.removeClass('d-none');
};

$(function() {
    const $optIn = $('#onms-notification-optin');
    if ($optIn.length > 0) {
        wireOptIn($optIn);
    }

    start();
});
