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

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

public class JdbcStatement implements Serializable, Comparable<JdbcStatement> {

    private static final long serialVersionUID = 883422287764280313L;
    
    @XmlElement(name="queryString",required=true)
    private String m_jdbcQuery;
    
    @XmlTransient
    public String getJdbcQuery() {
        return m_jdbcQuery;
    }
    
    public void setJdbcQuery(String jdbcQuery) {
        m_jdbcQuery = jdbcQuery;
    }
    
    @Override
    public int compareTo(JdbcStatement obj) {
        return new CompareToBuilder()
            .append(getJdbcQuery(), obj.getJdbcQuery())
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcStatement) {
            JdbcStatement other = (JdbcStatement) obj;
            return new EqualsBuilder()
                .append(getJdbcQuery(), other.getJdbcQuery())
                .isEquals();
        }
        return false;
    }
}
