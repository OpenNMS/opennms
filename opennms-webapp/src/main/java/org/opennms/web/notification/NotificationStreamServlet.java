/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
