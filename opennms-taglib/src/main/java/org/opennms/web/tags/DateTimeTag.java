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

package org.opennms.web.tags;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.opennms.core.time.CentralizedDateTimeFormat;

/**
 * This class replaces the &lt;fmt:formatDate /&gt; tag.
 * Why do we need a new tag?
 * => fmt can't be configured via a System Property (without side effects)
 * => we want to support the new java.time classes
 *
 * It will output datetimes as ISO_8601 type style unless otherwise defined in opennms.properties.
 * See also:
 *   https://en.wikipedia.org/wiki/ISO_8601 and
 *   https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html
 */
public class DateTimeTag extends SimpleTagSupport {

    private Instant instant;

    @Override
    public void doTag() throws IOException {
        // Output an empty string for null values. I believe fmt:formatDate does the same
        String output = Optional.ofNullable(new CentralizedDateTimeFormat().format(instant, getZoneId())).orElse("");
        getJspContext().getOut().write(output);
    }

    public void setInstant(Instant instant) {
        this.instant = instant;
    }

    @Deprecated // please try to use the new Java Date API when possible: setInstant(Instant instant)
    public void setDate(Date date) {
        if (date != null) {
            this.instant = date.toInstant();
        }
    }

    private ZoneId getZoneId(){
        ZoneId timeZoneId = (ZoneId) this.getJspContext().getAttribute(CentralizedDateTimeFormat.SESSION_PROPERTY_TIMEZONE_ID, PageContext.SESSION_SCOPE);
        if(timeZoneId == null){
            timeZoneId = ZoneId.systemDefault();
        }
        return timeZoneId;
    }
}
