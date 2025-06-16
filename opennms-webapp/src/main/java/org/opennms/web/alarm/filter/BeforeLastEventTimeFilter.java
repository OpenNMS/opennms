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
package org.opennms.web.alarm.filter;

import java.util.Date;

import org.opennms.web.filter.LessThanFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>BeforeLastEventTimeFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class BeforeLastEventTimeFilter extends LessThanFilter<Date> {
    /** Constant <code>TYPE="beforelasteventtime"</code> */
    public static final String TYPE = "beforelasteventtime";

    /**
     * <p>Constructor for BeforeLastEventTimeFilter.</p>
     *
     * @param date a {@link java.util.Date} object.
     */
    public BeforeLastEventTimeFilter(Date date) {
        super(TYPE, SQLType.DATE, "LASTEVENTTIME", "lastEventTime", date);
    }

    /**
     * <p>Constructor for BeforeLastEventTimeFilter.</p>
     *
     * @param epochTime a long.
     */
    public BeforeLastEventTimeFilter(long epochTime) {
        this(new Date(epochTime));
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return ("time of last event before \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<BeforeLastEventTimeFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getDate</p>
     *
     * @return a {@link java.util.Date} object.
     */
    public Date getDate() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof BeforeLastEventTimeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
