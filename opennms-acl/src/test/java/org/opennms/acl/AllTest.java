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
import org.opennms.acl.repository.ibatis.UserRepositoryIbatisTest;
import org.opennms.acl.service.AuthoritiesNodeHelperTest;

@RunWith(Suite.class)
@SuiteClasses( { AuthoritiesNodeHelperTest.class, AuthorityRepositoryIbatisTest.class, UserRepositoryIbatisTest.class, AuthorityFactoryTest.class, UserFactoryTest.class })
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