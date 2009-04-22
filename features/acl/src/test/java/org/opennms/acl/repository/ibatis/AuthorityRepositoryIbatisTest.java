package org.opennms.acl.repository.ibatis;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.acl.SpringFactory;
import org.opennms.acl.conf.dbunit.DBAuthority;
import org.opennms.acl.conf.dbunit.DBGroup;
import org.opennms.acl.conf.dbunit.DBUser;
import org.opennms.acl.conf.dbunit.DbGroupMemeber;
import org.opennms.acl.model.AuthorityDTO;
import org.opennms.acl.model.AuthorityView;
import org.opennms.acl.repository.AuthorityRepository;

public class AuthorityRepositoryIbatisTest {

    @BeforeClass
    public static void setUp() throws Exception {
        repo = (AuthorityRepository) SpringFactory.getXmlWebApplicationContext().getBean("authorityRepository");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        repo = null;
    }

    @Before
    public void prepareDb() {
        dbUser.prepareDb();
        dbGroup.prepareDb();
        dbGroupMember.prepareDb();
        dbAuth.prepareDb();
    }

    @After
    public void cleanDb() {
        dbAuth.cleanDb();
        dbGroupMember.cleanDb();
        dbGroup.cleanDb();
        dbUser.cleanDb();
    }

    @Test
    public void getAllAuthorities() {
        assertTrue(repo.getAuthorities().size() == 16);
    }

    @Test
    public void getUserAuthorities() {
        assertTrue(repo.getFreeAuthorities("paperone").size() == 16);
        assertTrue(repo.getUserAuthorities("max").size() == 5);
    }

    @Test
    public void getFreeAuthorities() {
        assertTrue(repo.getFreeAuthorities("paperone").size() == 16);
        assertTrue(repo.getFreeAuthorities("max").size() == 11);
    }

    @Test
    public void saveAuthority() {
        AuthorityDTO auth = new AuthorityDTO();
        auth.setName("testAuth");
        assertTrue(repo.save(auth));
    }

    @Test
    public void saveAuthorities() {
        List<Integer> authorities = new ArrayList<Integer>();
        authorities.add(14);
        authorities.add(16);
        repo.saveAuthorities(1, authorities);
    }

    @Test
    public void getAuthoritiesNumber() {
        assertTrue(repo.getAuthorities().size() == 16);
    }

    @Test
    public void getAuthority() {
        AuthorityView authority = repo.getAuthority(19);
        assertTrue(authority.getName().equals("quattro"));
        authority = repo.getAuthority(30);
        assertTrue(authority.getName().equals("quindici"));
    }

    @Test
    public void getAuthorities() {
        AuthorityView authority = repo.getAuthority(19);
        assertTrue(authority.getName().equals("quattro"));
        authority = repo.getAuthority(30);
        assertTrue(authority.getName().equals("quindici"));
    }

    @Test
    public void removeAuthority() {
        assertTrue(repo.removeAuthority(19));
    }

    @Test
    public void deleteUserGroups() {
        assertTrue(repo.deleteUserGroups("max"));
    }

    private DBUser dbUser = new DBUser();
    private DBAuthority dbAuth = new DBAuthority();
    private DBGroup dbGroup = new DBGroup();
    private DbGroupMemeber dbGroupMember = new DbGroupMemeber();
    private static AuthorityRepository repo;
}
