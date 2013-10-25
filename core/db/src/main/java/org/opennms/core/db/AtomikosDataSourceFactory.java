/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.db;

import java.beans.PropertyVetoException;
import java.io.IOException;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

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

public class AtomikosDataSourceFactory extends AtomikosDataSourceBean {

	private static final long serialVersionUID = -6411281260947841402L;

	public AtomikosDataSourceFactory() throws MarshalException, ValidationException, ClassNotFoundException, IOException, PropertyVetoException, SQLException {
		XADataSourceFactory.init();
		super.setUniqueResourceName("opennms");
		super.setXaDataSource(XADataSourceFactory.getXADataSource());
		super.setPoolSize(30);
		super.setTestQuery("SELECT 1");
	}
}
