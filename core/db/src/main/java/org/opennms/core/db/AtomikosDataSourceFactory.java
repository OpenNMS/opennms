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

import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.springframework.beans.factory.InitializingBean;

import com.atomikos.jdbc.AtomikosDataSourceBean;


/**
<bean id="dataSource" class="com.atomikos.jdbc.AtomikosDataSourceBean" destroy-method="close">
<property name="uniqueResourceName" value="opennms"/>
<property name="xaDataSource">
  <bean class="org.opennms.core.db.XADataSourceFactoryBean" />
</property>
<property name="poolSize" value="30"/>
<!-- This test query assures that connections are refreshed following a database restart -->
<property name="testQuery" value="SELECT 1"/>
</bean>
*/

public class AtomikosDataSourceFactory extends AtomikosDataSourceBean implements InitializingBean, ClosableDataSource {

	private static final long serialVersionUID = -6411281260947841402L;

	public static final Logger LOG = LoggerFactory.getLogger(AtomikosDataSourceFactory.class);

	public AtomikosDataSourceFactory(JdbcDataSource ds) {
		this();
	}

	public AtomikosDataSourceFactory() {
		super.setUniqueResourceName("opennms");
		super.setXaDataSource(XADataSourceFactory.getInstance());
		super.setPoolSize(30);

		// Automatically rollback the connection on borrow to avoid a problem where
		// Atomikos will reuse database connections that contain aborted transactions, 
		// mark the connections as "erroneous", and recycle the connections. We want to
		// avoid database connection recycling to avoid lockups in PostgreSQL that occur
		// when creating new connections. This occurs on PostgreSQL 8.4 but may be fixed
		// in later versions.
		//
		// These aborted transactions shouldn't happen and are probably caused by errors 
		// in JDBC code. Atomikos may also only exhibit this behavior when running without 
		// a transaction manager (as is the case in the current OpenNMS code with 
		// Hibernate 3.6).
		//
		super.setTestQuery("ROLLBACK;SELECT 1;");

		/*
		// Disable pool maintenance (reaping and shrinking) by setting the interval
		// to the highest value possible. We want the connections to PostgreSQL to 
		// remain open forever without being recycled.
		super.setMaintenanceInterval(Integer.MAX_VALUE / 1000);
		*/
	}

	/**
	 * This call will initialize the {@link AtomikosDataSourceBean} after the properties
	 * have been set when this factory is used in a Spring context.
	 */
	@Override
	public void afterPropertiesSet() throws Exception {
		super.init();
	}

	@Override
	public java.util.logging.Logger getParentLogger() throws SQLFeatureNotSupportedException {
		throw new SQLFeatureNotSupportedException();
	}

	@Override
	public <T> T unwrap(Class<T> iface) throws SQLException {
		throw new SQLException();
	}

	@Override
	public boolean isWrapperFor(Class<?> iface) {
		return false;
	}

	@Override
	public void setIdleTimeout(int idleTimeout) {
		super.setMaxIdleTime(idleTimeout);
	}

	@Override
	public void setMinPool(int minPool) {
		super.setMinPoolSize(minPool);
	}

	@Override
	public void setMaxPool(int maxPool) {
		super.setMaxPoolSize(maxPool);
	}

	@Override
	public void setMaxSize(int maxSize) {
		LOG.debug("Atomikos has no equivalent to setMaxSize(). Ignoring.");
	}
}
