package org.opennms.acl.repository.ibatis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.acl.SpringFactory;
import org.opennms.acl.conf.dbunit.DBGroup;
import org.opennms.acl.conf.dbunit.DbGroupMemeber;

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
