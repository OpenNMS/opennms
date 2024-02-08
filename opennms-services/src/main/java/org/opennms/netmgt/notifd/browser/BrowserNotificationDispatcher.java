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
