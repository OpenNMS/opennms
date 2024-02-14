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
package org.opennms.core.utils.http;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HttpResponseRange {
    private static final Pattern RANGE_PATTERN = Pattern.compile("([1-5][0-9][0-9])(?:-([1-5][0-9][0-9]))?");
    private final int m_begin;
    private final int m_end;

    public HttpResponseRange(String rangeSpec) {
        Matcher matcher = RANGE_PATTERN.matcher(rangeSpec);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid range spec: " + rangeSpec);
        }

        String beginSpec = matcher.group(1);
        String endSpec = matcher.group(2);

        m_begin = Integer.parseInt(beginSpec);

        if (endSpec == null) {
            m_end = m_begin;
        } else {
            m_end = Integer.parseInt(endSpec);
        }
    }

    public boolean contains(int responseCode) {
        return (m_begin <= responseCode && responseCode <= m_end);
    }

    @Override
    public String toString() {
        if (m_begin == m_end) {
            return Integer.toString(m_begin);
        } else {
            return Integer.toString(m_begin) + '-' + Integer.toString(m_end);
        }
    }
}