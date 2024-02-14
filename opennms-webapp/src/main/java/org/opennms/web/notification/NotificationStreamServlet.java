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
package org.opennms.web.notification;

import java.io.IOException;
import java.util.Objects;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.json.JSONObject;
import org.opennms.netmgt.notifd.browser.BrowserNotificationDispatcher;
import org.opennms.netmgt.notifd.browser.BrowserNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONSerializer;

public class NotificationStreamServlet extends WebSocketServlet {
    private static final Logger LOG = LoggerFactory.getLogger(NotificationStreamServlet.class);

    public NotificationStreamServlet() {
    }

    @Override
    public void configure(final WebSocketServletFactory factory) {
        factory.setCreator((request, response) -> {
            final String user = request.getHttpServletRequest().getRemoteUser();

            return new NotificationStreamSocket(user);
        });
    }

    public class NotificationStreamSocket extends WebSocketAdapter {
        private final String user;

        private BrowserNotificationDispatcher.Handler handler;

        public NotificationStreamSocket(final String user) {
            this.user = Objects.requireNonNull(user);
        }

        @Override
        public void onWebSocketConnect(final Session session) {
            super.onWebSocketConnect(session);

            this.handler = BrowserNotificationDispatcher.getInstance().subscribe(this.user, this::sendNotification);
        }

        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            super.onWebSocketClose(statusCode, reason);
            BrowserNotificationDispatcher.getInstance().unsubscribe(this.handler);
        }

        @Override
        public void onWebSocketError(final Throwable cause) {
            super.onWebSocketError(cause);
            BrowserNotificationDispatcher.getInstance().unsubscribe(this.handler);
        }

        private void sendNotification(final BrowserNotificationMessage message) {
            final JSONObject json = new JSONObject();
            json.append("id", message.getId());
            json.append("head", message.getHead());
            json.append("body", message.getBody());

            try {
                this.getRemote().sendString(json.toString());
            } catch (final IOException e) {
                LOG.error("Failed to send out notification", e);
            }
        }
    }
}
