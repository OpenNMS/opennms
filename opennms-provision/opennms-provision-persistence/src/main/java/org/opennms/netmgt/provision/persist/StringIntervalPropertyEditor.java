/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

import org.joda.time.Duration;
import org.joda.time.Period;

public class StringIntervalPropertyEditor extends PropertyEditorSupport implements PropertyEditor {
    
    /** {@inheritDoc} */
    @Override
    public void setAsText(final String text) throws IllegalArgumentException {
        if ("0".equals(text)) {
            setValue(Duration.ZERO);
        } else {
            setValue(StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.parsePeriod(text).toStandardDuration());
        }
    }

    /**
     * <p>getAsText</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getAsText() {
        final Duration value = (Duration)getValue();
        if (value.equals(Duration.ZERO)) {
            return "0";
        }
        Period p = value.toPeriod().normalizedStandard();
        return StringIntervalAdapter.DEFAULT_PERIOD_FORMATTER.print(p);
    } 
}
