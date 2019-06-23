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

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.HOUR_OF_DAY;
import static java.time.temporal.ChronoField.MINUTE_OF_HOUR;
import static java.time.temporal.ChronoField.SECOND_OF_MINUTE;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.jsp.tagext.SimpleTagSupport;

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

    final static String SYSTEM_PROPERTY_DATE_FORMAT = "org.opennms.ui.datettimeformat";

    private final static Logger LOG = Logger.getLogger(DateTimeTag.class.getName());

    private final static DateTimeFormatter DEFAULT_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(ISO_LOCAL_DATE)
            .appendLiteral('T')
            .appendValue(HOUR_OF_DAY, 2)
            .appendLiteral(':')
            .appendValue(MINUTE_OF_HOUR, 2)
            .optionalStart()
            .appendLiteral(':')
            .appendValue(SECOND_OF_MINUTE, 2)
            .appendOffsetId().toFormatter()
            .withZone(ZoneId.systemDefault());

    private Instant instant;
    private DateTimeFormatter formatter;


    public DateTimeTag(){
        String format = System.getProperty(SYSTEM_PROPERTY_DATE_FORMAT);
        if(format == null) {
            this.formatter = DEFAULT_FORMATTER;
        } else {
            try {
                this.formatter = DateTimeFormatter.ofPattern(format).withZone(ZoneId.systemDefault());
            } catch (IllegalArgumentException e) {
                LOG.log(Level.WARNING,
                        String.format("Can not use System Property %s=%s as dateformat, will fall back to default." +
                                " Please see also java.time.format.DateTimeFormatter for the correct syntax",
                                SYSTEM_PROPERTY_DATE_FORMAT,
                                format)
                        , e);
                this.formatter = DEFAULT_FORMATTER;
            }
        }
    }

    @Override
    public void doTag() throws IOException {
        String output = this.formatter.format(instant);
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
}
