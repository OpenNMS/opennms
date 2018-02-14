/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.api;

import java.beans.PropertyEditorSupport;
import java.util.Date;

import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * PropertyEditor suitable for use by BeanWrapperImpl, so that we can accept xsd:datetime formatted dates
 * in query strings.
 *
 * Also handles "epoch" style dates, if they exist.  Could be extended to guess the date format and do something
 * useful with it
 *
 * @author miskellc
 */
public class ISO8601DateEditor extends PropertyEditorSupport {
    private static final DateTimeFormatter m_formatter = ISODateTimeFormat.dateTime().withZone(DateTimeZone.UTC);

    public ISO8601DateEditor() {
        super();
    }

    /** {@inheritDoc} */
    @Override
    public String getAsText() {
        return m_formatter.print(((Date)super.getValue()).getTime());
    }

    /** {@inheritDoc} */
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        super.setValue(ISO8601DateEditor.stringToDate(text));
    }

    /**
     * {@inheritDoc}
     *
     * No, we don't do GUIs.  Sod off
     */
    @Override
    public boolean isPaintable() {
        return false;
    }

    public static Date stringToDate(final String text) throws IllegalArgumentException, UnsupportedOperationException {
        if (text == null || "null".equals(text)) {
            return null;
        }

        Exception cause;
        try {
            // first, try parsing it as an epoch
            return new Date(Long.parseLong(text, 10));
        } catch (final NumberFormatException nfe) {
            cause = nfe;
            // if that fails, try parsing as a standard ISO8601 date
            try {
                return m_formatter.parseDateTime(text).toDate();
            } catch (final IllegalArgumentException|UnsupportedOperationException e) {
                cause = e;
            }
        }

        throw new IllegalArgumentException("Unable to parse value '" + text + "' as a date.", cause);
    }
}
