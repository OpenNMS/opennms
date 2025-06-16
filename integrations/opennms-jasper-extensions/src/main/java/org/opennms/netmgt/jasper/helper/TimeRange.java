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
package org.opennms.netmgt.jasper.helper;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * JasperReports scriptlet to retrieve start and end dates from predefined
 * ranges: last year, last month, this year, this month
 *
 * @author ronny
 */
public enum TimeRange {

    LAST_SEVEN_DAYS {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), new GregorianCalendar().get(Calendar.DATE) - 7).getTimeInMillis());
        }

    },
    LAST_MONTH {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH) -1, 1).getTimeInMillis());
        }

        @Override
        public Date getEndDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), 0, 23, 59, 59).getTimeInMillis());
        }

    },
    LAST_YEAR {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR) - 1, Calendar.JANUARY, 1).getTimeInMillis());
        }

        @Override
        public Date getEndDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR) - 1, Calendar.UNDECIMBER, 0, 23, 59, 59).getTimeInMillis());
        }

    },
    THIS_MONTH {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), 1).getTimeInMillis());
        }

    },
    THIS_YEAR {
        @Override
        public Date getStartDate() {
            return new Date(new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), Calendar.JANUARY, 1).getTimeInMillis());
        }

    };

    public Date getEndDate() {
        return new Date( new GregorianCalendar(new GregorianCalendar().get(Calendar.YEAR), new GregorianCalendar().get(Calendar.MONTH), new GregorianCalendar().get(Calendar.DATE) ).getTimeInMillis() );
    }

    public abstract Date getStartDate();


    /**
     * <p>
     * getStartDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public static Date getStartDate(String range) {
        TimeRange timeRange = getTimeRange(range);
        return timeRange != null ? timeRange.getStartDate() : null;
    }

    /**
     * <p>
     * getEndDate
     * </p>
     * 
     * @param range
     *            a {@link java.lang.String} object
     * @return a {@link java.sql.Timestamp} object
     */
    public static Date getEndDate(String range) {
        TimeRange timeRange = getTimeRange(range);
        return timeRange != null ? timeRange.getEndDate() : null;
    }

    private static TimeRange getTimeRange(String range) {
        for(TimeRange eachTimeRange : values()) {
            if (eachTimeRange.name().equalsIgnoreCase(range)) {
                return eachTimeRange;
            }
        }
        return null;
    }
}
