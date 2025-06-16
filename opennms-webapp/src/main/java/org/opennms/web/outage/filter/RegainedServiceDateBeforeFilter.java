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
package org.opennms.web.outage.filter;

import java.util.Date;

import org.opennms.web.filter.LessThanFilter;
import org.opennms.web.filter.SQLType;

/**
 * <p>RegainedServiceDateBeforeFilter class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class RegainedServiceDateBeforeFilter extends LessThanFilter<Date> {
    /** Constant <code>TYPE="regainedbefore"</code> */
    public static final String TYPE = "regainedbefore";

    /**
     * <p>Constructor for RegainedServiceDateBeforeFilter.</p>
     *
     * @param date a java$util$Date object.
     */
    public RegainedServiceDateBeforeFilter(Date date) {
        super(TYPE, SQLType.DATE, "OUTAGES.IFREGAINEDSERVICE", "ifRegainedService", date);
    }

    /**
     * <p>Constructor for RegainedServiceDateBeforeFilter.</p>
     *
     * @param epochTime a long.
     */
    public RegainedServiceDateBeforeFilter(long epochTime) {
        this(new Date(epochTime));
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return ("regained service date before \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<Regained Service Date Before Filter: " + this.getDescription() + ">");
    }

    /**
     * <p>getDate</p>
     *
     * @return a java$util$Date object.
     */
    public Date getDate() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof RegainedServiceDateBeforeFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
