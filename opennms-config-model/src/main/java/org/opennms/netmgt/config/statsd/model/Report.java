/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.statsd.model;

import java.util.LinkedHashMap;

/**
 * Represents a configured report that can be applied to packages.
 *
 * @author <a href="mailto:dj@opennms.org">DJ Gregor</a>
 * @see PackageReport
 * @see StatsdPackage
 * @version $Id: $
 */
public class Report {
    private String m_name;
    private String m_className;
    private LinkedHashMap<String, String> m_parameters = new LinkedHashMap<String, String>();
    
    /**
     * <p>getClassName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getClassName() {
        return m_className;
    }
    /**
     * <p>setClassName</p>
     *
     * @param className a {@link java.lang.String} object.
     */
    public void setClassName(String className) {
        m_className = className;
    }
    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }
    /**
     * <p>setName</p>
     *
     * @param name a {@link java.lang.String} object.
     */
    public void setName(String name) {
        m_name = name;
    }
    /**
     * <p>getParameters</p>
     *
     * @return a {@link java.util.LinkedHashMap} object.
     */
    public LinkedHashMap<String, String> getParameters() {
        return m_parameters;
    }
    /**
     * <p>setParameters</p>
     *
     * @param parameters a {@link java.util.LinkedHashMap} object.
     */
    public void setParameters(LinkedHashMap<String, String> parameters) {
        m_parameters = parameters;
    }
    /**
     * <p>addParameter</p>
     *
     * @param key a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void addParameter(String key, String value) {
        m_parameters.put(key, value);
    }
}
