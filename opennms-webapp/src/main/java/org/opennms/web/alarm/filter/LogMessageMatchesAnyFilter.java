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

import org.opennms.web.alarm.filter.LogMessageSubstringFilter;
import org.opennms.web.filter.SubstringFilter;

/**
 * <p>LogMessageMatchesAnyFilter class.</p>
 *
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * 
 * @deprecated Replace calls to this with the identical {@link LogMessageSubstringFilter}
 */
public class LogMessageMatchesAnyFilter extends SubstringFilter {
    /** Constant <code>TYPE="msgmatchany"</code> */
    public static final String TYPE = "msgmatchany";

    /**
     * <p>Constructor for LogMessageMatchesAnyFilter.</p>
     *
     * @param substring a {@link java.lang.String} object.
     */
    public LogMessageMatchesAnyFilter(String substring) {
        super(TYPE, "logMsg", "logMsg", substring);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getTextDescription() {
        final StringBuilder buffer = new StringBuilder("message containing \"");
        buffer.append(getValue());
        buffer.append("\"");

        return buffer.toString();
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return "<LogMessageMatchesAnyFilter: " + this.getDescription() + ">";
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof LogMessageMatchesAnyFilter)) return false;
        return this.toString().equals(obj.toString());
    }

}
