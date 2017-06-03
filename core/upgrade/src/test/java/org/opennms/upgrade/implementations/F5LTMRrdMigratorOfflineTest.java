/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;

/**
 * The Test Class for F5LTMRrdMigratorOfflineTest.
 *
 * @author Ronald Roskens <roskens@opennms.org>
 */
public class F5LTMRrdMigratorOfflineTest {

    private static final List<String> s_f5Directories = Arrays.asList(
      "ltmPoolStatName",
      "ltmVSStatName",
      "f5ifName",
      "gtmWideipStatEntry",
      "f5trunkStat"
    );

    @Rule
    public TestName m_testName = new TestName();

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        System.out.println("---- Begin Test " + m_testName.getMethodName() + " ----");
        FileUtils.copyDirectory(new File("src/test/resources/etc"), new File("target/home/etc"));
        FileUtils.copyDirectory(new File("src/test/resources/jetty-webapps/opennms/WEB-INF"), new File("target/home/jetty-webapps/opennms/WEB-INF/"));
        System.setProperty("opennms.home", "target/home");
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        FileUtils.deleteDirectory(new File("target/home"));
        System.out.println("---- End Test " + m_testName.getMethodName() + " ----");
    }

    /**
     * Test upgrade (single-metric JRBs, i.e. storeByGroup=false).
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgrade() throws Exception {
        File rrdDir = new File("target/home/rrd");
        File nodeDir = new File(rrdDir, "1");
        File ltmPoolStatDir = new File("target/home/rrd/1/ltmPoolStatName");
        FileUtils.copyDirectory(new File("src/test/resources/rrd"), rrdDir);
        for (String ltmDirectory : s_f5Directories) {
            File ltmDir = new File(nodeDir, ltmDirectory);
            //System.out.println("mkdir " + ltmDir);
            ltmDir.mkdir();
            OctetString oString = new OctetString("eth0");
            OID oid = oString.toSubIndex(false);
            //System.out.printf("cp -pr %s %s/%s\n", "src/test/resources/rrd/1/eth0", ltmDir.toString(), oid.toDottedString());

            FileUtils.copyDirectory(new File("src/test/resources/rrd/1/eth0"), new File(ltmDir, oid.toDottedString()));
        }

        F5LTMRrdMigratorOffline ltmMigrator = executeMigrator();
    }

    /**
     * Executes the JMX Migrator.
     *
     * @return the JMX Migrator object
     * @throws Exception the exception
     */
    private F5LTMRrdMigratorOffline executeMigrator() throws Exception {
        F5LTMRrdMigratorOffline ltmMigrator = new F5LTMRrdMigratorOffline();
        ltmMigrator.preExecute();
        ltmMigrator.execute();
        ltmMigrator.postExecute();
        Assert.assertEquals(5, ltmMigrator.processedDirectories.size());

        return ltmMigrator;
    }

}
