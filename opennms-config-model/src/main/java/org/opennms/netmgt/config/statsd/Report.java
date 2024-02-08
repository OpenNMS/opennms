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
package org.opennms.netmgt.config.statsd;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * Collector for a service
 */
@XmlRootElement(name = "report")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("statistics-daemon-configuration.xsd")
public class Report implements Serializable {
    private static final long serialVersionUID = 2L;

    /**
     * The report name. This is used in packages to refer
     *  to this report class.
     */
    @XmlAttribute(name = "name", required = true)
    private String m_name;

    /**
     * The class used to create and view this
     *  report
     */
    @XmlAttribute(name = "class-name", required = true)
    private String m_className;

    /**
     * The parameters for generating this report
     */
    @XmlElement(name = "parameter")
    private List<Parameter> m_parameters = new ArrayList<>();

    public Report() {
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getClassName() {
        return m_className;
    }

    public void setClassName(final String className) {
        m_className = ConfigUtils.assertNotEmpty(className, "class-name");
    }

    public List<Parameter> getParameters() {
        return m_parameters;
    }

    public void setParameters(final List<Parameter> parameters) {
        if (parameters == m_parameters) return;
        m_parameters.clear();
        if (parameters != null) m_parameters.addAll(parameters);
    }

    public void addParameter(final Parameter parameter) {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_name, 
                            m_className, 
                            m_parameters);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }

        if (obj instanceof Report) {
            final Report that = (Report)obj;
            return Objects.equals(this.m_name, that.m_name)
                    && Objects.equals(this.m_className, that.m_className)
                    && Objects.equals(this.m_parameters, that.m_parameters);
        }
        return false;
    }

}
