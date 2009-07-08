/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 *
 * Copyright (C) 2009 Massimiliano Dess&igrave; (desmax74@yahoo.it)
 * Copyright (C) 2009 The OpenNMS Group, Inc.
 * All rights reserved.
 *
 * This program was developed and is maintained by Rocco RIONERO
 * ("the author") and is subject to dual-copyright according to
 * the terms set in "The OpenNMS Project Contributor Agreement".
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc.:
 *
 *      51 Franklin Street
 *      5th Floor
 *      Boston, MA 02110-1301
 *      USA
 *
 * For more information contact:
 *
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *
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
