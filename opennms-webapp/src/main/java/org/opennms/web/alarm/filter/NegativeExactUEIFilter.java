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

import org.opennms.web.filter.NotEqualsFilter;
import org.opennms.web.filter.SQLType;

/**
 * Encapsulates filtering on exact unique event identifiers.
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class NegativeExactUEIFilter extends NotEqualsFilter<String> {
    /** Constant <code>TYPE="exactUeiNot"</code> */
    public static final String TYPE = "exactUeiNot";

    /**
     * <p>Constructor for NegativeExactUEIFilter.</p>
     *
     * @param uei a {@link java.lang.String} object.
     */
    public NegativeExactUEIFilter(String uei) {
        super(TYPE, SQLType.STRING, "EVENTUEI", "uei", uei);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        return "UEI is not " + getValue();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return ("<AlarmFactory.NegativeExactUEIFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getUEI</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getUEI() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeExactUEIFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
