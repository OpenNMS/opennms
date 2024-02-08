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
package org.opennms.web.navigate;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;

public class NavBarModel {
    private final HttpServletRequest m_request;
    private final Map<NavBarEntry, DisplayStatus> m_entries;
    private final List<Map.Entry<NavBarEntry, DisplayStatus>> m_entryList;

    public NavBarModel(final HttpServletRequest request, final Map<NavBarEntry, DisplayStatus> entries) {
        m_request = request;
        m_entries = entries;
        m_entryList = Lists.newArrayList(m_entries.entrySet());
    }

    public HttpServletRequest getRequest() {
        return m_request;
    }

    public Map<NavBarEntry, DisplayStatus> getEntries() {
        return m_entries;
    }

    /** Allows the Freemarker template by the NavBarController to iterate
     *  over the map entries.
     */
    public List<Map.Entry<NavBarEntry, DisplayStatus>> getEntryList() {
        return m_entryList;
    }
}
