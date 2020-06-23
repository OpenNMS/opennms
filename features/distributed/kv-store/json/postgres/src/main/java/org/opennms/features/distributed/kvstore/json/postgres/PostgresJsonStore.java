/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
