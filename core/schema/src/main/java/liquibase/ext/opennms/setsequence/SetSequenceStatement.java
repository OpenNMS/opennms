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
package liquibase.ext.opennms.setsequence;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;

import liquibase.statement.SqlStatement;

public class SetSequenceStatement implements SqlStatement {
    private final String m_sequenceName;
    private final List<String> m_tables = new ArrayList<>();
    private Map<String, String> m_columns = new LinkedHashMap<String, String>();
    private Map<String, String> m_schemas = new LinkedHashMap<String, String>();
    private Integer m_value;

    public SetSequenceStatement(final String sequenceName) {
        m_sequenceName = sequenceName;
    }

    @Override
    public boolean skipOnUnsupported() {
        return true;
    }

    @Override
    public boolean continueOnError() {
        return false;
    }

    public String getSequenceName() {
        return m_sequenceName;
    }

    public List<String> getTables() {
        return m_tables;
    }

    public Map<String,String> getColumns() {
        return m_columns;
    }

    public Map<String,String> getSchemas() {
        return m_schemas;
    }

    public Integer getValue() {
        return m_value;
    }

    public SetSequenceStatement setValue(final Integer value) {
        m_value = value;
        return this;
    }

    SetSequenceStatement addTable(final String name, final String column) {
        getTables().add(name);
        getColumns().put(name, column);
        return this;
    }

    SetSequenceStatement addTable(final String name, final String schemaName, final String column) {
        getTables().add(name);
        getColumns().put(name, column);
        getSchemas().put(name, schemaName);
        return this;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("sequenceName", m_sequenceName)
                .append("value", m_value)
                .append("tables", m_tables)
                .append("columns", m_columns)
                .append("schemas", m_schemas)
                .toString();
    }
}
