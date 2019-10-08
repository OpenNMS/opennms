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

package org.opennms.features.jest.client.index;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.TimeZone;

import org.opennms.features.jest.client.template.IndexSettings;

import com.google.common.base.Strings;

/**
 * Defines a strategy on how to define the index when persisting.
 *
 * This implementation is thread safe.
 */
public enum IndexStrategy {
    YEARLY("yyyy"),
    MONTHLY("yyyy-MM"),
    DAILY("yyyy-MM-dd"),
    HOURLY("yyyy-MM-dd-HH");

    /**
     * Use the {@link DateTimeFormatter} since its thread-safe.
     */
    private final DateTimeFormatter dateFormat;

    private final String pattern; // remember pattern since DateFormat doesn't provide access to it

    IndexStrategy(String pattern) {
        this.pattern = pattern;
        final ZoneId UTC = TimeZone.getTimeZone("UTC").toZoneId();
        dateFormat = DateTimeFormatter.ofPattern(pattern)
                .withZone(UTC);
    }

    public String getIndex(IndexSettings indexSettings, String indexName, TemporalAccessor temporal) {
        final StringBuilder sb = new StringBuilder();
        if (!Strings.isNullOrEmpty(indexSettings.getIndexPrefix())) {
            sb.append(indexSettings.getIndexPrefix());
        }
        sb.append(indexName);
        sb.append("-");
        sb.append(dateFormat.format(temporal));
        return sb.toString();
    }

    public String getPattern(){
        return pattern;
    }
}
