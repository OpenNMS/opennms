/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.support.menu;

import java.time.Instant;
import java.time.ZoneId;
import javax.servlet.http.HttpServletRequest;
import org.opennms.core.time.CentralizedDateTimeFormat;
import org.opennms.netmgt.config.NotifdConfigFactory;

public class HttpMenuRequestContext implements MenuRequestContext {
    final private HttpServletRequest request;
    final private CentralizedDateTimeFormat dateTimeFormat = new CentralizedDateTimeFormat();

    public HttpMenuRequestContext(HttpServletRequest request) {
        this.request = request;
    }

    public String getRemoteUser() {
        return request.getRemoteUser();
    }

    public String calculateUrlBase() {
        return org.opennms.web.api.Util.calculateUrlBase(this.request);
    }

    public boolean isUserInRole(final String role) {
        return this.request.isUserInRole(role);
    }

    public String getFormattedTime() {
        return this.dateTimeFormat.format(Instant.now(), extractUserTimeZone());
    }

    public String getNoticeStatus() {
        try {
            return NotifdConfigFactory.getPrettyStatus();
        } catch (final Throwable ignored) {
        }

        return "Unknown";
    }

    private ZoneId extractUserTimeZone() {
        ZoneId timeZoneId = (ZoneId) this.request.getSession().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID);

        if (timeZoneId == null) {
            timeZoneId = ZoneId.systemDefault();
        }

        return timeZoneId;
    }
}
