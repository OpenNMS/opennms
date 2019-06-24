/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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
