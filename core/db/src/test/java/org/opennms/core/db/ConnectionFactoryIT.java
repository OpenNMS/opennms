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
package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.test.DaoTestConfigBean;

/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class ConnectionFactoryIT extends TestCase {
	public void testMarshalDataSourceFromConfig() throws Exception {
		DaoTestConfigBean bean = new DaoTestConfigBean();
		bean.afterPropertiesSet();

		AtomikosDataSourceFactory factory1 = null;
		AtomikosDataSourceFactory factory2 = null;

		try {
			factory1 = makeFactory("opennms");
			factory1.setUniqueResourceName("opennms");
			factory2 = makeFactory("opennms2");
			factory2.setUniqueResourceName("opennms2");

			Connection conn = null;
			Statement s = null;
			try {
				conn = factory2.getConnection();
				s = conn.createStatement();
				s.execute("select * from pg_proc");
			} finally {
				if (s != null) {
					s.close();
				}
				if (conn != null) {
					conn.close();
				}
			}
		} finally {
			Throwable t1 = null;
			Throwable t2 = null;

			if (factory1 != null) {
				try {
					factory1.close();
					factory1 = null;
				} catch (Throwable e1) {
					t1 = e1;
				}
			}

			if (factory2 != null) {
				try {
					factory2.close();
					factory2 = null;
				} catch (Throwable e2) {
					t2 = e2;
				}
			}

			if (t1 != null || t2 != null) {
				final StringBuilder message = new StringBuilder();
				message.append("Could not successfully close both C3P0 factories.  Future tests might fail.");

				Throwable choice;
				if (t1 != null) {
					message.append("  First factory failed with: " + t1.getMessage() + "; see stack back trace.");
					choice = t1;

					if (t2 != null) {
						System.err.println("  Both factories failed to close.  See stderr for second stack back trace.");
						t2.printStackTrace(System.err);
					}
				} else {
					choice = t2;
				}
				AssertionError e = new AssertionError(message.toString());
				e.initCause(choice);
				throw e;
			}
		}
	}

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

		// Spawn a bunch of threads that generate continuous SQLExceptions
		for (int i = 0; i < 2000; i++) {
			new Thread() {
				public void run() {
					Connection conn = null;
					try {
						assertEquals(50, factory2.poolTotalSize());

						conn = factory2.getConnection();

						// Make sure that the total size of the pool stays at 50
						assertEquals(50, factory2.poolTotalSize());
						assertTrue(factory2.poolAvailableSize() > 0);
						// Fetching the current connection will push the available connections below 50
						assertTrue(factory2.poolAvailableSize() < 50);

						Statement stmt = conn.createStatement();
						stmt.execute("BEGIN");
						stmt.execute("SELECT * FROM doesnt_exist_in_the_database");
					} catch (SQLException e) {
						e.printStackTrace();
					} finally {
						/*
						if (conn != null) {
							try {
								Statement stmt = conn.createStatement();
								stmt.execute("ROLLBACK");
								conn.close();
							} catch (SQLException e) {
								fail("Exception thrown when trying to close connection");
							}
						}
						*/
					}
				}
			}.start();

			// Only sleep for a bit after spawning 10 threads so that we force some
			// contention.
			if (i % 10 == 0) Thread.sleep(200);
		}
	}

	private AtomikosDataSourceFactory makeFactory(String database) throws PropertyVetoException, SQLException, IOException, ClassNotFoundException {
		InputStream stream1 = new ByteArrayInputStream(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, ConfigFileConstants.getFileName(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME)).getBytes());
		DataSourceFactory.setDataSourceConfigurationFactory(new DataSourceConfigurationFactory(stream1));
		InputStream stream2 = new ByteArrayInputStream(ConfigurationTestUtils.getConfigForResourceWithReplacements(this, ConfigFileConstants.getFileName(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME)).getBytes());
		XADataSourceFactory.setDataSourceConfigurationFactory(new DataSourceConfigurationFactory(stream2));
		try {
			return new AtomikosDataSourceFactory();
		} finally {
			IOUtils.closeQuietly(stream1);
			IOUtils.closeQuietly(stream2);
		}
	}
}
