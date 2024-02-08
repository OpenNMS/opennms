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
package org.opennms.features.distributed.kvstore.json.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.features.distributed.kvstore.pgshared.AbstractPostgresKeyValueStore;

public class PostgresJsonStore extends AbstractPostgresKeyValueStore<String, String> implements JsonStore {
    public PostgresJsonStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected String getTableName() {
        return "kvstore_jsonb";
    }

    @Override
    protected String getValueStatementPlaceholder() {
        return super.getValueStatementPlaceholder() + "::JSON";
    }

    @Override
    protected String getValueTypeFromSQLType(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getString(columnName);
    }

    @Override
    protected String getPkConstraintName() {
        return "pk_kvstore_jsonb";
    }
}
