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

package org.opennms.netmgt.notifd.browser;

import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

import com.google.common.collect.Sets;

public class BrowserNotificationDispatcher {

    public static class Handler {
        public final String user;
        public final Consumer<BrowserNotificationMessage> consumer;

        private Handler(final String user,
                        final Consumer<BrowserNotificationMessage> consumer) {
            this.user = Objects.requireNonNull(user);
            this.consumer = Objects.requireNonNull(consumer);
        }
    }

    private final Set<Handler> handlers = Sets.newConcurrentHashSet();

    public Handler subscribe(final String user, final Consumer<BrowserNotificationMessage> consumer) {
        final Handler handler = new Handler(user, consumer);
        this.handlers.add(handler);

        return handler;
    }

    public void unsubscribe(final Handler handler) {
        this.handlers.remove(handler);
    }

    public void notify(final String user, final BrowserNotificationMessage notification) {
        this.handlers.stream()
                .filter(handler -> handler.user.equals(user))
                .forEach(handler -> handler.consumer.accept(notification));
    }

    private final static BrowserNotificationDispatcher INSTANCE = new BrowserNotificationDispatcher();

    public static BrowserNotificationDispatcher getInstance() {
        return INSTANCE;
    }
}
