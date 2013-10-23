/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.hibernate;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.internal.SessionImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.AbstractDataSource;
import org.springframework.jdbc.datasource.ConnectionProxy;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.TransactionAwareDataSourceProxy;

/**
 * <p>To get SessionFactory connections and JdbcTemplate connections to use the
 * same underlaying database connections, we need to wrap the DataSource for
 * the JdbcTemplate with this class. It will fetch the current Hibernate Session
 * and use its database connection if available.</p>
 * 
 * <p>This is a workaround to the fact that Spring expects the J2EE JTA implementation
 * to automatically reuse database connections for an ongoing transaction but the
 * Atomikos DataSource does not implement this sort of transaction-aware connection 
 * tracking.</p>
 * 
 * @see https://jira.springsource.org/browse/SPR-1976
 */
public class Hibernate4SessionDataSource extends AbstractDataSource {

	private static final Logger LOG = LoggerFactory.getLogger(Hibernate4SessionDataSource.class);

	private DataSource targetDataSource;
	private SessionFactory sessionFactory;

	/**
	 * Default constructor.
	 */
	public Hibernate4SessionDataSource() {
	}

	public Hibernate4SessionDataSource(DataSource target, SessionFactory session) {
		targetDataSource = target;
		sessionFactory = session;
	}

	public DataSource getTargetDataSource() {
		return targetDataSource;
	}

	public void setTargetDataSource(DataSource targetDataSource) {
		this.targetDataSource = targetDataSource;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	@Override
	public Connection getConnection() throws SQLException {
		Session session = sessionFactory.getCurrentSession();
		if (session == null) {
			LOG.debug("No session bound to the current thread, returning new database connection");
			return targetDataSource.getConnection();
		} else {
			// Wrap the connection in the same code that {@link TransactionAwareDataSourceProxy} uses
			LOG.debug("Session bound to the current thread, reusing Hibernate database connection");
			Connection hibernateConnection = ((SessionImpl)session).connection();
			return (Connection) Proxy.newProxyInstance(
					ConnectionProxy.class.getClassLoader(),
					new Class[] {ConnectionProxy.class},
					new TransactionAwareInvocationHandler(hibernateConnection, targetDataSource));
		}
	}

	@Override
	public Connection getConnection(String username, String password) throws SQLException {
		return getConnection();
	}

	/**
	 * Invocation handler that delegates close calls on JDBC Connections
	 * to DataSourceUtils for being aware of thread-bound transactions.
	 * 
	 * @see TransactionAwareDataSourceProxy#TransactionAwareInvocationHandler
	 */
	private class TransactionAwareInvocationHandler implements InvocationHandler {

		private final DataSource targetDataSource;

		private Connection target;

		private boolean closed = false;

		public TransactionAwareInvocationHandler(Connection target, DataSource targetDataSource) {
			this.target = target;
			this.targetDataSource = targetDataSource;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			// Invocation on ConnectionProxy interface coming in...

			if (method.getName().equals("equals")) {
				// Only considered as equal when proxies are identical.
				return (proxy == args[0]);
			}
			else if (method.getName().equals("hashCode")) {
				// Use hashCode of Connection proxy.
				return System.identityHashCode(proxy);
			}
			else if (method.getName().equals("toString")) {
				// Allow for differentiating between the proxy and the raw Connection.
				StringBuilder sb = new StringBuilder("Transaction-aware proxy for target Connection ");
				if (this.target != null) {
					sb.append("[").append(this.target.toString()).append("]");
				}
				else {
					sb.append(" from DataSource [").append(this.targetDataSource).append("]");
				}
				return sb.toString();
			}
			else if (method.getName().equals("unwrap")) {
				if (((Class) args[0]).isInstance(proxy)) {
					return proxy;
				}
			}
			else if (method.getName().equals("isWrapperFor")) {
				if (((Class) args[0]).isInstance(proxy)) {
					return true;
				}
			}
			else if (method.getName().equals("close")) {
				// Handle close method: only close if not within a transaction.
				DataSourceUtils.doReleaseConnection(this.target, this.targetDataSource);
				this.closed = true;
				return null;
			}
			else if (method.getName().equals("isClosed")) {
				return this.closed;
			}

			/*
			We can comment this out since we're providing the Connection

			if (this.target == null) {
				if (this.closed) {
					throw new SQLException("Connection handle already closed");
				}
				if (shouldObtainFixedConnection(this.targetDataSource)) {
					this.target = DataSourceUtils.doGetConnection(this.targetDataSource);
				}
			}
			 */
			Connection actualTarget = this.target;
			if (actualTarget == null) {
				actualTarget = DataSourceUtils.doGetConnection(this.targetDataSource);
			}

			if (method.getName().equals("getTargetConnection")) {
				// Handle getTargetConnection method: return underlying Connection.
				return actualTarget;
			}

			// Invoke method on target Connection.
			try {
				Object retVal = method.invoke(actualTarget, args);

				// If return value is a Statement, apply transaction timeout.
				// Applies to createStatement, prepareStatement, prepareCall.
				if (retVal instanceof Statement) {
					DataSourceUtils.applyTransactionTimeout((Statement) retVal, this.targetDataSource);
				}

				return retVal;
			}
			catch (InvocationTargetException ex) {
				throw ex.getTargetException();
			}
			finally {
				if (actualTarget != this.target) {
					DataSourceUtils.doReleaseConnection(actualTarget, this.targetDataSource);
				}
			}
		}
	}
}
