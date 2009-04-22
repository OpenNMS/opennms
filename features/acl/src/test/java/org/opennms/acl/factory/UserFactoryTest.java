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
public class UserFactoryTest {

    @BeforeClass
    public static void setUp() throws Exception {
        factory = (AclUserFactory) SpringFactory.getXmlWebApplicationContext().getBean("aclUserFactory");
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
    public void getUserByIDWithAuthorities() {

        assertNotNull(factory.getAclUser(1));
        assertTrue(factory.getAclUser(1).getUsername().equals("max"));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void getUserDisabledByIDWithAuthorities() {

        factory.getAclUser(8);
    }

    @Test
    public void getUserByIDWithOutAuthorities() {

        assertNotNull(factory.getAclUser(2));
        assertTrue(factory.getAclUser(2).getUsername().equals("pippo"));
    }

    @Test
    public void getUserByUsernameWithAuthorities() {

        assertNotNull(factory.getAclUserByUsername("max"));
        assertTrue(factory.getAclUserByUsername("max").getUsername().equals("max"));
    }

    @Test(expected = java.lang.IllegalArgumentException.class)
    public void getUserDisabldeByUsernameWithAuthorities() {

        factory.getAclUserByUsername("pluto");
    }

    private DBUser dbUser = new DBUser();
    private DBAuthority dbAuth = new DBAuthority();
    private DBGroup dbGroup = new DBGroup();
    // private DBAuthoritiesAuth dbAuthoritiesAuth = new DBAuthoritiesAuth();
    private static AclUserFactory factory;
}
