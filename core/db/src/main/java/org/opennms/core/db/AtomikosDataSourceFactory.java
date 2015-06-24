/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
