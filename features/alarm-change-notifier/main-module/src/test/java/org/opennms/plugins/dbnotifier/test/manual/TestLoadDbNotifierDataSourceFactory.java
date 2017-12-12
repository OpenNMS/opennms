/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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

package org.opennms.plugins.dbnotifier.test.manual;

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.plugins.dbnotifier.DbNotifierDataSourceFactory;
import com.impossibl.postgres.jdbc.PGDataSource;

public class TestLoadDbNotifierDataSourceFactory {

	
	@Test
	public void testLoadFromProperties() {
		
		System.out.println("start of testLoadFromProperties");

		DbNotifierDataSourceFactory dsConfig = new DbNotifierDataSourceFactory();
		
		dsConfig.setDataBaseName("testdataBaseName");
		dsConfig.setUserName("testuserName");
		dsConfig.setPassWord("testpassWord");
		dsConfig.setHostname("localhost");
		dsConfig.setPort("5432");

		dsConfig.init();
		
		assertEquals("testdataBaseName",dsConfig.getDataBaseName());
		assertEquals("testuserName",dsConfig.getUserName());
		assertEquals("testpassWord",dsConfig.getPassWord());
		assertEquals("localhost",dsConfig.getHostname());
		assertEquals("5432",dsConfig.getPort());
		
		PGDataSource pgds = dsConfig.getPGDataSource();
		assertNotNull(pgds);
		
		System.out.println("end of testLoadFromProperties");
		
	}
	
	@Test
	public void testLoadFromXML() {
		
		System.out.println("start of testLoadFromXML");

		DbNotifierDataSourceFactory dsConfig = new DbNotifierDataSourceFactory();
		
		String fileUri=null;
		fileUri="./src/test/resources/opennms-datasources.xml";
		dsConfig.setDataSourceFileUri(fileUri);
		dsConfig.init();
		
		assertEquals("opennms",dsConfig.getDataBaseName());
		assertEquals("opennms",dsConfig.getUserName());
		assertEquals("opennms",dsConfig.getPassWord());
		assertEquals("localhost",dsConfig.getHostname());
		assertEquals("5432",dsConfig.getPort());
		
		PGDataSource pgds = dsConfig.getPGDataSource();
		assertNotNull(pgds);
		
		System.out.println("end of testLoadFromXML");
		
	}

}
