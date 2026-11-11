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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;

@XmlRootElement(name = "jdbc-collection")
@XmlType(name="jdbc-collection")
public class JdbcDataCollection implements Serializable, Comparable<JdbcDataCollection> {
    private static final long serialVersionUID = -7451959128852991463L;
    
    private static final JdbcQuery[] OF_QUERIES = new JdbcQuery[0];
    
    @XmlAttribute(name="name")
    @JsonProperty("name")
    private String m_name;
    
    @XmlElement(name="rrd")
    @JsonProperty("rrd")
    private JdbcRrd m_jdbcRrd;
    
    @XmlElementWrapper(name="queries")
    @XmlElement(name="query")
    @JsonIgnore // written inside the "queries" wrapper, see getQueriesWrapper()
    private List<JdbcQuery> m_jdbcQueries = new ArrayList<>();
    
    public JdbcDataCollection() {
        
    }
    
    @XmlTransient
    public JdbcRrd getJdbcRrd() {
        return m_jdbcRrd;
    }
    
    public void setJdbcRrd(JdbcRrd jdbcRrd) {
        m_jdbcRrd = jdbcRrd;
    }
    
    @XmlTransient
    public List<JdbcQuery> getQueries() {
        return m_jdbcQueries;
    }

    public void setQueries(List<JdbcQuery> jdbcQueries) {
        m_jdbcQueries = jdbcQueries;
    }

    @XmlTransient
    public String getName() {
        return m_name;
    }

    public void setName(String name) {
        m_name = name;
    }

    public void addQuery(JdbcQuery query) {
        m_jdbcQueries.add(query);
    }
    
    public void removeQuery(JdbcQuery query) {
        m_jdbcQueries.remove(query);
    }
    
    public void removeQueryByName(String name) {
        for (Iterator<JdbcQuery> itr = m_jdbcQueries.iterator(); itr.hasNext(); ) {
            JdbcQuery query = itr.next();
            if(query.getQueryName().equals(name)) {
                m_jdbcQueries.remove(query);
                return;
            }
        }
    }
    
    /**
     * The configuration manager stores the queries like the XML does: {@code "queries": {"query": [...]}}.
     */
    @JsonProperty("queries")
    private Queries getQueriesWrapper() {
        return m_jdbcQueries == null || m_jdbcQueries.isEmpty() ? null : new Queries(m_jdbcQueries);
    }

    @JsonProperty("queries")
    private void setQueriesWrapper(final Queries queries) {
        m_jdbcQueries = queries == null || queries.query == null ? new ArrayList<>() : queries.query;
    }

    private static class Queries {
        @JsonProperty("query")
        private List<JdbcQuery> query;

        private Queries() {
        }

        private Queries(final List<JdbcQuery> query) {
            this.query = query;
        }
    }

    @Override
    public int compareTo(JdbcDataCollection obj) {
        return new CompareToBuilder()
            .append(getName(), obj.getName())
            .append(getJdbcRrd(), obj.getJdbcRrd())
            .append(getQueries().toArray(OF_QUERIES), obj.getQueries().toArray(OF_QUERIES))
            .toComparison();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof JdbcDataCollection) {
            JdbcDataCollection other = (JdbcDataCollection) obj;
            return new EqualsBuilder()
                .append(getName(), other.getName())
                .append(getJdbcRrd(), other.getJdbcRrd())
                .append(getQueries().toArray(OF_QUERIES), other.getQueries().toArray(OF_QUERIES))
                .isEquals();
        }
        return false;
    }
    
}
