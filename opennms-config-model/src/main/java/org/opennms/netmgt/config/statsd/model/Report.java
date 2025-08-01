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
package org.opennms.netmgt.config.statsd.model;

import java.util.LinkedHashMap;
import java.util.Map;

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
    private Map<String, String> m_parameters = new LinkedHashMap<String, String>();
    
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
    public Map<String, String> getParameters() {
        return m_parameters;
    }
    /**
     * <p>setParameters</p>
     *
     * @param parameters a {@link java.util.LinkedHashMap} object.
     */
    public void setParameters(Map<String, String> parameters) {
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
