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

package org.opennms.features.distributed.cassandra.impl;

import org.opennms.features.distributed.cassandra.api.CassandraSchemaManager;
import org.opennms.features.distributed.cassandra.api.CassandraSchemaManagerFactory;
import org.opennms.netmgt.newts.proxy.SchemaManagerProxy;

/**
 * Proxies the schema manager from Newts and provides it wrapped by a {@link CassandraSchemaManager}.
 */
public class NewtsCassandraSchemaManagerFactory implements CassandraSchemaManagerFactory {
    private final CassandraSchemaManager schemaManagerProxy;

    public NewtsCassandraSchemaManagerFactory(SchemaManagerProxy newtsSchemaManager) {
        schemaManagerProxy = schema -> newtsSchemaManager.create(schema.getInputStream());
    }

    @Override
    public CassandraSchemaManager getSchemaManager() {
        return schemaManagerProxy;
    }
}
