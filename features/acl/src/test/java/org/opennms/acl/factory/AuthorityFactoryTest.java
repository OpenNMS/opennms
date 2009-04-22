package org.opennms.acl.factory;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.acl.SpringFactory;
import org.opennms.acl.conf.dbunit.DBAuthority;
import org.opennms.acl.conf.dbunit.DBGroup;
import org.opennms.acl.conf.dbunit.DBUser;

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
