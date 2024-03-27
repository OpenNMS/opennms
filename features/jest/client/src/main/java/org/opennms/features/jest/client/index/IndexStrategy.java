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
