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
package org.opennms.netmgt.vaadin.core;

import org.opennms.web.navigate.PageNavEntry;

public class AdminPageNavEntry implements PageNavEntry {
    /**
     * name of this PageNavEntry
     */
    private String m_name;
    /**
     * URL of this PageNavEntry
     */
    private String m_url;

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return m_name;
    }

    /**
     * Sets the name of the PageNavEntry.
     *
     * @param name the name to be set
     */
    public void setName(final String name) {
        this.m_name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl() {
        return m_url;
    }

    /**
     * Sets the URL of this PageNavEntry.
     *
     * @param url the URL to be set
     */
    public void setUrl(final String url) {
        this.m_url = url;
    }
}
