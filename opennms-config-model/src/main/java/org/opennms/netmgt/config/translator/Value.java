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
package org.opennms.netmgt.config.translator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * An element representing a value to be used in a
 *  translation. 
 */
@XmlRootElement(name = "value")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("translator-configuration.xsd")
public class Value implements Serializable {
    private static final long serialVersionUID = 2L;

    @XmlAttribute(name = "result", required = true)
    private String m_result;

    @XmlAttribute(name = "matches")
    private String m_matches;

    @XmlAttribute(name = "type", required = true)
    private String m_type;

    @XmlAttribute(name = "name")
    private String m_name;

    /**
     * An element representing a value to be used in a
     *  translation. 
     */
    @XmlElement(name = "value")
    private List<Value> m_values = new ArrayList<>();

    public void addValue(final Value value) {
        m_values.add(value);
    }

    @Override
    public boolean equals(final Object obj) {
        if ( this == obj ) {
            return true;
        }
        
        if (obj instanceof Value) {
            final Value that = (Value)obj;
            return Objects.equals(this.m_result, that.m_result)
                && Objects.equals(this.m_matches, that.m_matches)
                && Objects.equals(this.m_type, that.m_type)
                && Objects.equals(this.m_name, that.m_name)
                && Objects.equals(this.m_values, that.m_values);
        }
        return false;
    }

    public Optional<String> getMatches() {
        return Optional.ofNullable(m_matches);
    }

    public Optional<String> getName() {
        return Optional.ofNullable(m_name);
    }

    public String getResult() {
        return m_result;
    }

    public String getType() {
        return m_type;
    }

    public List<Value> getValues() {
        return m_values;
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_result, 
            m_matches, 
            m_type, 
            m_name, 
            m_values);
    }

    public boolean removeValue(final Value value) {
        return m_values.remove(value);
    }

    public void setMatches(final String matches) {
        m_matches = ConfigUtils.normalizeString(matches);
    }

    public void setName(final String name) {
        m_name = ConfigUtils.normalizeString(name);
    }

    public void setResult(final String result) {
        m_result = ConfigUtils.assertNotEmpty(result, "result");
    }

    public void setType(final String type) {
        m_type = ConfigUtils.assertNotEmpty(type, "type");
    }

    public void setValue(final List<Value> values) {
        if (values == m_values) return;
        m_values.clear();
        if (values != null) m_values.addAll(values);
    }

}
