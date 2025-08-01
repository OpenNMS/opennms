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
package org.opennms.netmgt.notifd;

import java.util.List;
import java.util.UUID;

import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.opennms.netmgt.notifd.browser.BrowserNotificationDispatcher;
import org.opennms.netmgt.notifd.browser.BrowserNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Send notifications to the browser.
 */
public class BrowserNotificationStrategy implements NotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(BrowserNotificationStrategy.class);

    public BrowserNotificationStrategy() {
    }

    @Override
    public int send(final List<Argument> arguments) {
        String user = null;
        String head = null;
        String body = null;

        for (final Argument argument : arguments) {
            switch (argument.getSwitch()) {
                case NotificationManager.PARAM_DESTINATION:
                    user = argument.getValue();
                    break;

                case NotificationManager.PARAM_SUBJECT:
                    head = argument.getValue();
                    break;

                case NotificationManager.PARAM_TEXT_MSG:
                case NotificationManager.PARAM_NUM_MSG:
                    body = argument.getValue();
                    break;
            }
        }

        final BrowserNotificationMessage message = new BrowserNotificationMessage();
        message.setId(UUID.randomUUID().toString());
        message.setHead(head);
        message.setBody(body);

        BrowserNotificationDispatcher.getInstance().notify(user, message);

        return 0;
    }
}
