package org.opennms.acl.repository.ibatis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
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
import org.opennms.acl.conf.dbunit.DbGroupMemeber;
import org.opennms.acl.model.Pager;
import org.opennms.acl.model.UserAuthoritiesDTO;
import org.opennms.acl.model.UserDTO;
import org.opennms.acl.model.UserView;

@Ignore("test database is not thread-safe, port to opennms temporary database code")
public class UserRepositoryIbatisTest {

    @BeforeClass
    public static void setUp() throws Exception {
        repo = (UserRepositoryIbatis) SpringFactory.getXmlWebApplicationContext().getBean("userRepository");
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
        // dbAuthoritiesAuth.prepareDb();
    }

    @After
    public void cleanDb() {
        // dbAuthoritiesAuth.cleanDb();
        dbAuth.cleanDb();
        dbGroupMember.cleanDb();
        dbGroup.cleanDb();
        dbUser.cleanDb();
    }

    @Test
    public void getIdUser() {
        assertTrue(repo.getIdUser("max").toString().equals("1"));
    }

    @Test
    public void disableUser() {
        assertTrue(repo.disableUser("5"));
    }

    @Test
    public void getUser() {
        UserView user = repo.getUser("1");
        assertTrue(user.getUsername().equals("max"));
        assertTrue(user.isEnabled() == true);
        assertTrue(user.getId() == 1);
    }

    @Test
    public void getUserDisabled() {
        assertNull(repo.getUser("8"));
    }

    @Test
    public void getUserWithAuthorities() {
        UserAuthoritiesDTO user = repo.getUserWithAuthorities("max");
        assertTrue(user.getUsername().equals("max"));
        assertTrue(user.isEnabled() == true);
        assertTrue(user.getId() == 1);
        assertTrue(user.getAuthorities().size() == 5);
        assertFalse(user.isNew());
    }

    @Test
    public void getUserDisabldeWithAuthorities() {
        UserAuthoritiesDTO user = repo.getUserWithAuthorities("pluto");
        assertNull(user);
    }

    @Test
    public void getUserWithOutAuthorities() {
        UserAuthoritiesDTO user = repo.getUserWithAuthorities("pippo");
        assertTrue(user.getUsername().equals("pippo"));
        assertTrue(user.isEnabled() == true);
        assertTrue(user.getId() == 2);
        assertTrue(user.getAuthorities().size() == 0);
        assertFalse(user.isNew());
    }

    @Test
    public void getEnabledUsers() {
        assertTrue(repo.getEnabledUsers(new Pager(0, 1, 15)).size() == 3);
    }

    @Test
    public void getDisabledUsers() {
        assertTrue(repo.getDisabledUsers(new Pager(0, 1, 15)).size() == 1);
    }

    @Test
    public void getUsersNumber() {
        assertTrue(repo.getUsersNumber() == 4);
    }

    @Test
    public void insertUser() {
        UserDTO user = new UserDTO();
        user.setEnabled(true);
        user.setPassword("paperoga");
        user.setUsername("paperoga");
        assertTrue(repo.insertUser(user) > 0);
    }

    @Test
    public void updateUser() {
        UserDTO user = new UserDTO();
        user.setEnabled(true);
        user.setId(new Long(2));
        user.setPassword("pluto");
        user.setOldPassword("pippo");
        user.setUsername("pippo");
        assertTrue(repo.updatePassword(user) == 1);
    }

    private DBUser dbUser = new DBUser();
    private DBAuthority dbAuth = new DBAuthority();
    // private DBAuthoritiesAuth dbAuthoritiesAuth = new DBAuthoritiesAuth();
    private DBGroup dbGroup = new DBGroup();
    private DbGroupMemeber dbGroupMember = new DbGroupMemeber();
    private static UserRepositoryIbatis repo;
}
