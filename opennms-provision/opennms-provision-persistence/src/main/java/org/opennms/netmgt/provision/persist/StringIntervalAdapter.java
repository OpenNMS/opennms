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
package org.opennms.netmgt.provision.persist;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.joda.time.Duration;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

public class StringIntervalAdapter extends XmlAdapter<String, Duration> {
    /** Constant <code>DEFAULT_PERIOD_FORMATTER</code> */
    public static final PeriodFormatter DEFAULT_PERIOD_FORMATTER = new PeriodFormatterBuilder()
    .appendWeeks().appendSuffix("w").appendSeparator(" ")
    .appendDays().appendSuffix("d").appendSeparator(" ")
    .appendHours().appendSuffix("h").appendSeparator(" ")
    .appendMinutes().appendSuffix("m").appendSeparator(" ")
    .appendSeconds().appendSuffix("s").appendSeparator(" ")
    .appendMillis().appendSuffix("ms")
    .toFormatter();
    
    /** {@inheritDoc} */
    @Override
    public String marshal(final Duration v) {
        if (v.equals(Duration.ZERO)) {
            return "0";
        }
        return DEFAULT_PERIOD_FORMATTER.print(v.toPeriod().normalizedStandard());
    }

    /** {@inheritDoc} */
    @Override
    public Duration unmarshal(final String v) {
        if (v == null) {
            return null;
        }
        if ("0".equals(v.trim())) {
            return Duration.ZERO;
        }
        if ("-1".equals(v.trim())) {
            return Duration.ZERO.minus(1000);
        }
        return DEFAULT_PERIOD_FORMATTER.parsePeriod(v.trim()).toStandardDuration();
    }

}
