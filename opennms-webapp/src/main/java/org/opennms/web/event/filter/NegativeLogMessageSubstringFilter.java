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
package org.opennms.web.event.filter;

import org.opennms.web.filter.NoSubstringFilter;

public class NegativeLogMessageSubstringFilter extends NoSubstringFilter {
    /** Constant <code>TYPE="msgsub"</code> */
    public static final String TYPE = "msgsubNot";

    /**
     * <p>Constructor for NegativeLogMessageSubstringFilter.</p>
     *
     * @param substring a {@link String} object.
     */
    public NegativeLogMessageSubstringFilter(String substring) {
        super(TYPE, "EVENTLOGMSG", "eventLogMsg", substring);
    }

    /**
     * <p>getTextDescription</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String getTextDescription() {
        return ("description not containing \"" + getValue() + "\"");
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link String} object.
     */
    @Override
    public String toString() {
        return ("<NegativeLogMessageSubstringFilter: " + this.getDescription() + ">");
    }

    /**
     * <p>getSubstring</p>
     *
     * @return a {@link String} object.
     */
    public String getSubstring() {
        return getValue();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof NegativeLogMessageSubstringFilter)) return false;
        return (this.toString().equals(obj.toString()));
    }
}
