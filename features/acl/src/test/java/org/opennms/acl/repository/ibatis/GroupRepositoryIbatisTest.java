/*******************************************************************************
 * This file is part of the OpenNMS(R) Application.
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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

package org.opennms.acl.repository.ibatis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.acl.SpringFactory;
import org.opennms.acl.conf.dbunit.DBGroup;
import org.opennms.acl.conf.dbunit.DbGroupMemeber;

@Ignore("test database is not thread-safe, port to opennms temporary database code")
public class GroupRepositoryIbatisTest {

    @BeforeClass
    public static void setUp() throws Exception {
        repo = (GroupRepositoryIbatis) SpringFactory.getXmlWebApplicationContext().getBean("groupRepository");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        repo = null;
    }

    @Before
    public void prepareDb() {
        dbGroup.prepareDb();
        dbGroupMember.prepareDb();
    }

    @After
    public void cleanDb() {
        dbGroupMember.cleanDb();
        dbGroup.cleanDb();
    }

    @Test
    public void saveGroups() {
        dbGroupMember.cleanDb();

        List<Integer> groups = new ArrayList<Integer>();
        groups.add(1);
        groups.add(2);
        repo.saveGroups("paperone", groups);
    }

    @Test
    public void updateGroup() {
        dbGroupMember.cleanDb();

        List<Integer> groups = new ArrayList<Integer>();
        groups.add(1);
        groups.add(2);
        repo.saveGroups("paperone", groups);
    }

    @Test
    public void getGroup() {
        assertNotNull(repo.getGroup(1));
    }

    @Test
    public void getGroupNumber() {
        assertNotNull(repo.getGroupsNumber() == 2);
    }

    @Test
    public void getUserGroups() {
        assertNotNull(repo.getUserGroups("max"));
        assertTrue(repo.getUserGroups("max").size() == 2);
    }

    @Test
    public void getFreeGroups() {
        assertNotNull(repo.getFreeGroups("paperone"));
        assertTrue(repo.getFreeGroups("paperone").size() == 3);
    }

    @Test
    public void deleteGroup() {
        assertTrue(repo.removeGroup(3));
    }

    private DBGroup dbGroup = new DBGroup();
    private DbGroupMemeber dbGroupMember = new DbGroupMemeber();
    private static GroupRepositoryIbatis repo;
}
