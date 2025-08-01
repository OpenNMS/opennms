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
package org.opennms.web.controller.admin.support;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.systemreport.SystemReportFormatter;
import org.opennms.systemreport.SystemReportPlugin;

public class SystemReportInfo implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 4803853030354121419L;
    private final List<SystemReportPlugin> m_plugins;
    private final List<SystemReportFormatter> m_formatters;


    public SystemReportInfo(final List<SystemReportFormatter> formatters, final List<SystemReportPlugin> plugins) {
        m_formatters = formatters;
        m_plugins = plugins;
    }

    public SystemReportFormatter getFormatter() {
        if (m_formatters != null && m_formatters.size() > 0) {
            return m_formatters.get(0);
        }
        return null;
    }
    
    public List<SystemReportFormatter> getFormatters() {
        return m_formatters;
    }
    
    public List<SystemReportPlugin> getPlugins() {
        return m_plugins;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("formatters", m_formatters)
            .append("plugins", m_plugins)
            .toString();
    }
}
