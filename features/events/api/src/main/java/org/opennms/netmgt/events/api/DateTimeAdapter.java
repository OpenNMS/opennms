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

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class DateTimeAdapter extends XmlAdapter<String, Date> {
    private static final EventDatetimeFormatter FORMATTER = EventConstants.getEventDatetimeFormatter();

    /** {@inheritDoc} */
    @Override
    public String marshal(final Date date) throws Exception {
        return date == null ? null : FORMATTER.format(date);
    }

    /** {@inheritDoc} */
    @Override
    public Date unmarshal(final String string) throws Exception {
        return (string == null || string.isEmpty()) ? null : FORMATTER.parse(string);
    }

}
