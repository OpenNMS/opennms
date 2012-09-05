/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
 *
 * From the original copyright headers:
 *
 * Copyright (c) 2009+ desmax74
 * Copyright (c) 2009+ The OpenNMS Group, Inc.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * The author can be contacted at the following email address:
 *
 *     Massimiliano Dess&igrave;
 *     desmax74@yahoo.it
 *******************************************************************************/

package org.opennms.acl;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.opennms.acl.conf.dbunit.DbUnit;
import org.opennms.acl.factory.AuthorityFactoryTest;
import org.opennms.acl.factory.UserFactoryTest;
import org.opennms.acl.repository.ibatis.AuthorityRepositoryIbatisTest;
import org.opennms.acl.repository.ibatis.GroupRepositoryIbatisTest;
import org.opennms.acl.repository.ibatis.UserRepositoryIbatisTest;
import org.opennms.acl.service.AuthoritiesNodeHelperTest;

@RunWith(Suite.class)
@SuiteClasses( { AuthoritiesNodeHelperTest.class,
		AuthorityRepositoryIbatisTest.class, GroupRepositoryIbatisTest.class,
		UserRepositoryIbatisTest.class, AuthorityFactoryTest.class,
		UserFactoryTest.class })
public class AllTest {

	@BeforeClass
	public static void setUp() throws Exception {
		SpringFactory.setUpXmlWebApplicationContext();
	}

	@AfterClass
	public static void tearDown() throws Exception {
		DbUnit.closeConnection();
		SpringFactory.destroyXmlWebApplicationContext();
	}
}