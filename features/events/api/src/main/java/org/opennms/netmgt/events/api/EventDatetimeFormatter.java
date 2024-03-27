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
package org.opennms.netmgt.events.api;

import java.text.ParseException;
import java.util.Date;

/**
 * An interface for handling the parsing and formatting of event
 * datetimes.
 */
public interface EventDatetimeFormatter {
    /**
     * A utility method to parse a string into a 'Date' instance.
     *
     * @param dateString a {@link java.lang.String} object
     * @return a {@link java.util.Date} object
     */
    public Date parse(String dateString) throws ParseException;

    /**
     * A utility method to format a date object into a string.
     *
     * @see java.text.DateFormat
     * @param date a {@link java.util.Date} object
     * @return a {@link java.lang.String} object
     */
    public String format(Date date);
}
