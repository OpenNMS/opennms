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

package org.opennms.acl.factory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.acl.SpringFactory;
import org.opennms.acl.conf.dbunit.DBAuthority;
import org.opennms.acl.conf.dbunit.DBGroup;
import org.opennms.acl.conf.dbunit.DBUser;

@Ignore("test database is not thread-safe, port to opennms temporary database code")
public class AuthorityFactoryTest {

    @BeforeClass
    public static void setUp() throws Exception {
        factory = (AutorityFactory) SpringFactory.getXmlWebApplicationContext().getBean("authorityFactory");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        factory = null;
    }

    @Before
    public void prepareDb() {
        dbGroup.prepareDb();
        dbUser.prepareDb();
        dbAuth.prepareDb();
        // dbAuthoritiesAuth.prepareDb();
    }

    @After
    public void cleanDb() {
        // dbAuthoritiesAuth.cleanDb();

        dbAuth.cleanDb();
        dbUser.cleanDb();
        dbGroup.cleanDb();
    }

    @Test
    public void getAuthority() {

        assertNotNull(factory.getAuthority(20));
        assertTrue(factory.getAuthority(20).getName().equals("Lettura nodi"));
        assertTrue(factory.getAuthority(20).getDescription().equals("Questo ruolo permette la lettura dei nodi"));
    }

    private DBUser dbUser = new DBUser();
    private DBAuthority dbAuth = new DBAuthority();
    // private DBAuthoritiesAuth dbAuthoritiesAuth = new DBAuthoritiesAuth();
    private DBGroup dbGroup = new DBGroup();
    private static AutorityFactory factory;
}
