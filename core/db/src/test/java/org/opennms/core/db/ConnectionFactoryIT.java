/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.test.DaoTestConfigBean;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class ConnectionFactoryIT {
    @Test
    public void testMarshalDataSourceFromConfig() throws Exception {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        AtomikosDataSourceFactory factory = makeFactory("opennms");

        try (
            final Connection conn = factory.getConnection();
                final Statement s = conn.createStatement();
        ) {
            assertTrue("execute should pass and return a result set", s.execute("select * from pg_proc"));
        }

        factory.close();
    }

    @Test
    @SuppressWarnings("java:S2925")
    public void testPoolWithSqlExceptions() throws Exception {
        DaoTestConfigBean bean = new DaoTestConfigBean();
        bean.afterPropertiesSet();

        AtomikosDataSourceFactory factory = makeFactory("opennms");
        factory.afterPropertiesSet();

        // Verify the default values
        assertEquals(30, factory.poolAvailableSize());
        assertEquals(30, factory.poolTotalSize());

        // Close the factory so that we can reregister another factory with the same name
        factory.close();

        final AtomikosDataSourceFactory factory2 = makeFactory("opennms");
        factory2.setPoolSize(50);
        factory2.afterPropertiesSet();

        // Verify the altered values
        assertEquals(50, factory2.poolAvailableSize());
        assertEquals(50, factory2.poolTotalSize());

        final ExecutorService executor = Executors.newFixedThreadPool(50);
        final List<Future<Boolean>> futures = new ArrayList<>();

        // Spawn a bunch of threads that generate continuous SQLExceptions
        for (int i = 0; i < 2000; i++) {
            final var future = executor.submit(() -> {
                assertEquals(50, factory2.poolTotalSize());

                try (
                    final Connection conn = factory2.getConnection();
                    final Statement stmt = conn.createStatement();
                ) {
                    // Make sure that the total size of the pool stays at 50
                    assertEquals(50, factory2.poolTotalSize());
                    assertTrue(factory2.poolAvailableSize() > 0);
                    // Fetching the current connection will push the available connections below 50
                    assertTrue(factory2.poolAvailableSize() < 50);

                    stmt.execute("BEGIN");
                    stmt.execute("SELECT * FROM doesnt_exist_in_the_database");
                } catch (final Exception e) {
                    assertTrue(e.getMessage().contains("relation \"doesnt_exist_in_the_database\" does not exist"));
                }

                return true;
            });

            // Only sleep for a bit after spawning 10 threads so that we force some
            // contention.
            if (i % 10 == 0) Thread.sleep(50);

            futures.add(future);
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (final InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private AtomikosDataSourceFactory makeFactory(final String database) throws PropertyVetoException, SQLException, IOException, ClassNotFoundException {
        try (
            final InputStream stream1 = new ByteArrayInputStream(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, ConfigFileConstants.getFileName(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME)).getBytes());
            final InputStream stream2 = new ByteArrayInputStream(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, ConfigFileConstants.getFileName(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME)).getBytes());
        ) {
            DataSourceFactory.setDataSourceConfigurationFactory(new DataSourceConfigurationFactory(stream1));
            XADataSourceFactory.setDataSourceConfigurationFactory(new DataSourceConfigurationFactory(stream2));
            final var factory = new AtomikosDataSourceFactory();
            factory.setUniqueResourceName(database);
            return factory;
        }
    }
}
