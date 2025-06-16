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
package org.opennms.netmgt.config.jdbc;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name="rrd")
public class JdbcRrd implements Serializable, Comparable<JdbcRrd> {

    private static final long serialVersionUID = 143526958273169546L;
    
    @XmlAttribute(name="step")
    private Integer m_step;
    
    @XmlElement(name="rra")
    private List<String> m_jdbcRras = new ArrayList<>();
    
    public JdbcRrd() {
        
    }

    @XmlTransient
    public Integer getStep() {
        return m_step;
    }

    public void setStep(Integer step) {
        m_step = step;
    }

    @XmlTransient
    public List<String> getJdbcRras() {
        return m_jdbcRras;
    }

    public void setJdbcRras(List<String> jdbcRras) {
        m_jdbcRras = jdbcRras;
    }
    
    public void addRra(String rra) {
        m_jdbcRras.add(rra);
    }
    
    public void removeRra(String rra) {
        m_jdbcRras.remove(rra);
    }
    
    @Override
    public int compareTo(JdbcRrd obj) {
        return new CompareToBuilder()
            .append(getStep(), obj.getStep())
            .append(getJdbcRras().toArray(), obj.getJdbcRras().toArray())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcRrd) {
            JdbcRrd other = (JdbcRrd) obj;
            return new EqualsBuilder()
                .append(getStep(), other.getStep())
                .append(getJdbcRras().toArray(), other.getJdbcRras().toArray())
                .isEquals();
        }
        return false;
    }
}
