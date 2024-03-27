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
package org.opennms.features.distributed.kvstore.blob.postgres;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.opennms.features.distributed.kvstore.api.BlobStore;
import org.opennms.features.distributed.kvstore.pgshared.AbstractPostgresKeyValueStore;

public class PostgresBlobStore extends AbstractPostgresKeyValueStore<byte[], byte[]> implements BlobStore {
    public PostgresBlobStore(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected byte[] getValueTypeFromSQLType(ResultSet resultSet, String columnName) throws SQLException {
        return resultSet.getBytes(columnName);
    }

    @Override
    protected String getTableName() {
        return "kvstore_bytea";
    }

    @Override
    protected String getPkConstraintName() {
        return "pk_kvstore_bytea";
    }
}
