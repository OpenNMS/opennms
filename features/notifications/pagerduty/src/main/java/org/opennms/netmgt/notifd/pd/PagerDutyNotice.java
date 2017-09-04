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

import org.opennms.netmgt.model.OnmsEvent;

import java.util.Objects;

/**
 * Contains all the information required to send a notice.
 *
 * @author jwhite
 */
public class PagerDutyNotice {
    private final OnmsEvent event;
    private final int noticeId;
    private final String subject;
    private final String body;
    private String routingKey;

    public PagerDutyNotice(OnmsEvent event, int noticeId, String subject, String body) {
        this.event = Objects.requireNonNull(event);
        this.noticeId = noticeId;
        this.subject = subject;
        this.body = body;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public OnmsEvent getEvent() {
        return event;
    }

    public int getNoticeId() {
        return noticeId;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    @Override
    public String toString() {
        return "PagerDutyNotice{" +
                "event=" + event +
                ", noticeId=" + noticeId +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                '}';
    }
}
