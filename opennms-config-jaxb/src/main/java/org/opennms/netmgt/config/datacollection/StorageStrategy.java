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
package org.opennms.netmgt.config.datacollection;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.collection.api.StrategyDefinition;

/**
 * Selects a StorageStrategy that decides where data is stored.
 */

@XmlRootElement(name="storageStrategy", namespace="http://xmlns.opennms.org/xsd/config/datacollection")
@XmlAccessorType(XmlAccessType.NONE)
public class StorageStrategy implements StrategyDefinition, Serializable {
    private static final long serialVersionUID = 1260293428590611897L;

    /**
     * Java class name of the class that implements the
     * StorageStrategy.
     */
    @XmlAttribute(name="class", required=true)
    private String m_clazz;

    /**
     * list of parameters to pass to the strategy
     *  for strategy-specific configuration information
     */
    @XmlElement(name="parameter", type=org.opennms.netmgt.config.datacollection.Parameter.class)
    private List<Parameter> m_parameters = new ArrayList<>();

    public StorageStrategy() {
        super();
    }

    public StorageStrategy(final String clazz) {
        if (clazz != null)
            m_clazz = clazz.intern();
    }

    /**
     * Java class name of the class that implements the StorageStrategy.
     */
    public String getClazz() {
        return m_clazz;
    }

    public void setClazz(final String clazz) {
        m_clazz = clazz.intern();
    }

    public List<Parameter> getParameters() {
        if (m_parameters == null) {
            return Collections.emptyList();
        } else {
            return Collections.unmodifiableList(m_parameters);
        }
    }

    public void setParameters(final List<Parameter> parameters) {
        m_parameters = new ArrayList<Parameter>(parameters);
    }

    public void addParameter(final Parameter parameter) throws IndexOutOfBoundsException {
        m_parameters.add(parameter);
    }

    public boolean removeParameter(final Parameter parameter) {
        return m_parameters.remove(parameter);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((m_clazz == null) ? 0 : m_clazz.hashCode());
        result = prime * result + ((m_parameters == null) ? 0 : m_parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof StorageStrategy)) {
            return false;
        }
        final StorageStrategy other = (StorageStrategy) obj;
        if (m_clazz == null) {
            if (other.m_clazz != null) {
                return false;
            }
        } else if (!m_clazz.equals(other.m_clazz)) {
            return false;
        }
        if (m_parameters == null) {
            if (other.m_parameters != null) {
                return false;
            }
        } else if (!m_parameters.equals(other.m_parameters)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "StorageStrategy [class=" + m_clazz + ", parameters=" + m_parameters + "]";
    }

}
