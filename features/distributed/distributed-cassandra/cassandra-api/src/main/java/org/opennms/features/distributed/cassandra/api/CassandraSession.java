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

package org.opennms.features.distributed.cassandra.api;

import java.util.concurrent.Future;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.RegularStatement;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.ResultSetFuture;
import com.datastax.driver.core.Statement;

/**
 * A thin facade on top of Cassandra's {@link com.datastax.driver.core.Session session}.
 * 
 * The purpose of using this interface rather than Cassandra's {@link com.datastax.driver.core.Session} directly is
 * because Newts wraps the session in a similar interface and does not expose the session directly. So rather than
 * depending on the newts version of the interface in OpenNMS we have this one and proxy between them in
 * NewtsCassandraSessionFactory. The implication being that any future implementations that may expose a
 * {@link com.datastax.driver.core.Session} directly will have to wrap it with this interface, but that should be
 * trivial.
 */
public interface CassandraSession {
    PreparedStatement prepare(String statement);

    PreparedStatement prepare(RegularStatement statement);

    ResultSetFuture executeAsync(Statement statement);

    ResultSet execute(Statement statement);

    ResultSet execute(String statement);

    Future<Void> shutdown();
}
