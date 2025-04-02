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

public class LocationBasedNavBarEntry implements NavBarEntry {
    private String m_locationMatch;
    private String m_url;
    private String m_name;

    /**
     * Optional.
     * A system property, from the opennms.properties file, that must match the systemPropertyValue.
     */
    private String m_systemProperty;
    private String m_systemPropertyValue;
    private List<NavBarEntry> m_entries;

    @Override
    public String getDisplayString() {
        return m_name;
    }

    @Override
    public String getUrl() {
        return m_url;
    }
    public void setUrl(String url) {
        m_url = url;
    }

    @Override
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }

    @Override
    public String getSystemProperty() {
        return m_systemProperty;
    }
    public void setSystemProperty(String systemProperty) {
        m_systemProperty = systemProperty;
    }
    @Override
    public String getSystemPropertyValue() {
        return m_systemPropertyValue;
    }
    public void setSystemPropertyValue(String systemPropertyValue) {
        m_systemPropertyValue = systemPropertyValue;
    }

    @Override
    public List<NavBarEntry> getEntries() {
        return m_entries;
    }
    public void setEntries(final List<NavBarEntry> entries) {
        m_entries = entries;
    }

    @Override
    public boolean hasEntries() {
        return m_entries != null && m_entries.size() > 0;
    }

    @Override
    public DisplayStatus evaluate(MenuContext context) {
        if (!evaluateSystemProperty()) {
            return DisplayStatus.NO_DISPLAY;
        }

        return isLinkMatches(context) ? DisplayStatus.DISPLAY_NO_LINK : DisplayStatus.DISPLAY_LINK;
    }

    /**
     * If a systemProperty and systemPropertyValue are specified in the NavBarEntry, the runtime
     * System property must match.
     */
    private boolean evaluateSystemProperty() {
        if (m_systemProperty != null && !m_systemProperty.isEmpty() &&
            m_systemPropertyValue != null && !m_systemPropertyValue.isEmpty()) {
            String value = System.getProperty(m_systemProperty);

            return value != null && !value.isEmpty() && value.equals(m_systemPropertyValue);
        }

        return true;
    }

    public String getLocationMatch() {
        return m_locationMatch;
    }
    public void setLocationMatch(String locationMatch) {
        m_locationMatch = locationMatch;
    }

    protected boolean isLinkMatches(MenuContext context) {
        return m_locationMatch.equals(context.getLocation());
    }
}
