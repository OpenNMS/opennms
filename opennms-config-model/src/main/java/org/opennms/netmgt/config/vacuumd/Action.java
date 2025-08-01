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
package org.opennms.netmgt.config.vacuumd;

import java.io.Serializable;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.xml.ValidateUsing;
import org.opennms.netmgt.config.utils.ConfigUtils;

/**
 * actions modify the database based on results of a trigger
 */
@XmlRootElement(name = "action")
@XmlAccessorType(XmlAccessType.FIELD)
@ValidateUsing("vacuumd-configuration.xsd")
public class Action implements Serializable {
    private static final long serialVersionUID = 2L;

    private static final String DEFAULT_DATA_SOURCE = "opennms";

    @XmlAttribute(name = "name", required = true)
    private String m_name;

    @XmlAttribute(name = "data-source")
    private String m_dataSource;

    /**
     * Just a generic string used for SQL statements
     */
    @XmlElement(name = "statement", required = true)
    private Statement m_statement;

    public Action() {
    }

    public Action(final String name, final String dataSource, final Statement statement) {
        setName(name);
        setDataSource(dataSource);
        setStatement(statement);
    }

    public String getName() {
        return m_name;
    }

    public void setName(final String name) {
        m_name = ConfigUtils.assertNotEmpty(name, "name");
    }

    public String getDataSource() {
        return m_dataSource == null ? DEFAULT_DATA_SOURCE : m_dataSource;
    }

    public void setDataSource(final String dataSource) {
        m_dataSource = ConfigUtils.normalizeString(dataSource);
    }

    public Statement getStatement() {
        return m_statement;
    }

    public void setStatement(final Statement statement) {
        m_statement = ConfigUtils.assertNotNull(statement, "statement");
    }

    public int hashCode() {
        return Objects.hash(m_name, m_dataSource, m_statement);
    }

    @Override()
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof Action) {
            final Action that = (Action) obj;
            return Objects.equals(this.m_name, that.m_name) &&
                    Objects.equals(this.m_dataSource, that.m_dataSource) &&
                    Objects.equals(this.m_statement, that.m_statement);
        }
        return false;
    }
}
