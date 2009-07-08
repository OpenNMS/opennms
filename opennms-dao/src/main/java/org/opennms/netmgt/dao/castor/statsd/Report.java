/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2007-2008 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
 *******************************************************************************/

package org.opennms.netmgt.dao.castor.statsd;

import java.util.LinkedHashMap;

/**
 * Represents a configured report that can be applied to packages.
 * 
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see PackageReport
 * @see StatsdPackage
 */
public class Report {
    private String m_name;
    private String m_className;
    private LinkedHashMap<String, String> m_parameters = new LinkedHashMap<String, String>();
    
    public String getClassName() {
        return m_className;
    }
    public void setClassName(String className) {
        m_className = className;
    }
    public String getName() {
        return m_name;
    }
    public void setName(String name) {
        m_name = name;
    }
    public LinkedHashMap<String, String> getParameters() {
        return m_parameters;
    }
    public void setParameters(LinkedHashMap<String, String> parameters) {
        m_parameters = parameters;
    }
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }
}
