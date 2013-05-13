/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
