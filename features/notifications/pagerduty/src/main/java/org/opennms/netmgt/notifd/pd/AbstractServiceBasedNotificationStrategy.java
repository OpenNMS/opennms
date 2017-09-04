/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd.pd;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.EventDao;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import java.util.List;

/**
 * Generic base class for {@link NotificationStrategy} implementations that handles
 * parsing the argument list, and retrieving the underlying event.
 */
public abstract class AbstractServiceBasedNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractServiceBasedNotificationStrategy.class);

    private EventDao eventDao;
    private TransactionOperations transactionTemplate;

    public abstract void send(int noticeId, String subject, String body, OnmsEvent event);

    @Override
    public int send(List<Argument> arguments) {
        Integer eventId = null;
        Integer noticeId = null;
        String subject = null;
        String body = null;

        LOG.debug("Called with arguments: {}", arguments);
        for (Argument arg : arguments) {
            if ("eventID".equalsIgnoreCase(arg.getSwitch())) {
                if (!StringUtils.isBlank(arg.getValue())) {
                    eventId = Integer.parseInt(arg.getValue());
                }
            } else if ("noticeid".equalsIgnoreCase(arg.getSwitch())) {
                if (!StringUtils.isBlank(arg.getValue())) {
                    noticeId = Integer.parseInt(arg.getValue());
                }
            } else if (NotificationManager.PARAM_SUBJECT.equals(arg.getSwitch())) {
                subject = arg.getValue();
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(arg.getSwitch())) {
                body = arg.getValue();
            }
        }

        // Make sure we have the arguments we need.
        if (noticeId == null) {
            throw new IllegalStateException("Missing required parameter: noticeid");
        } else if (eventId == null) {
            throw new IllegalStateException("Missing required parameter: eventID");
        }

        // Lookup the associated event
        final Integer effectiveEventId = eventId;
        final OnmsEvent event = transactionTemplate.execute(status -> {
            final OnmsEvent eventFromDao = eventDao.get(effectiveEventId);
            if (eventFromDao == null) {
                return null;
            }
            // Trigger lazy-loading of any properties we may need laters
            eventFromDao.getEventParameters();
            final OnmsNode nodeFromDao = eventFromDao.getNode();
            if (nodeFromDao != null) {
                nodeFromDao.getLabel();
                nodeFromDao.getCategories();
            }
            eventFromDao.getAlarm();
            return eventFromDao;
        });
        if (event == null) {
            throw new IllegalStateException("No event found with id: " + eventId);
        }

        send(noticeId, subject, body, event);
        return 0;
    }

    public void setEventDao(EventDao eventDao) {
        this.eventDao = eventDao;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }
}
