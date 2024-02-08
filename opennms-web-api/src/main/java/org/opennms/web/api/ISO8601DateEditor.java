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
