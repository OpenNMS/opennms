/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.upgrade.support;

import java.io.File;
import java.io.FileWriter;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.opennms.upgrade.tests.TestUpgradeA;
import org.opennms.upgrade.tests.TestUpgradeB;
import org.opennms.upgrade.tests.TestUpgradeExecuted;
import org.opennms.upgrade.tests.TestUpgradeNothing;
import org.opennms.upgrade.tests.bad.TestUpgradeWIthException;

/**
 * The Class UpgradeTest.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class UpgradeTest {

    /** The status file. */
    private File statusFile;

    /** The upgrade status. */
    private UpgradeStatus upgradeStatus;

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        statusFile = new File("target/status.properties");
        Properties p = new Properties();
        p.put(new TestUpgradeExecuted().getId(), new Date().toString());
        p.store(new FileWriter(statusFile), null);
        upgradeStatus = new UpgradeStatus(statusFile);
    }

    /**
     * Tear down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        statusFile.delete();
    }

    /**
     * Test upgrade.
     *
     * @throws Exception the exception
     */
    @Test
    public void testUpgrade() throws Exception {
        Assert.assertFalse(upgradeStatus.wasExecuted(new TestUpgradeNothing()));
        performUpgrade("org.opennms.upgrade.tests");
        Assert.assertTrue(upgradeStatus.wasExecuted(new TestUpgradeNothing()));
        Assert.assertEquals(3, UpgradeHelper.getExecutedList().size());
        Assert.assertEquals(TestUpgradeNothing.class.getName(), UpgradeHelper.getExecutedList().get(0));
        Assert.assertEquals(TestUpgradeA.class.getName(), UpgradeHelper.getExecutedList().get(1));
        Assert.assertEquals(TestUpgradeB.class.getName(), UpgradeHelper.getExecutedList().get(2));
        Assert.assertEquals(1, UpgradeHelper.getRolledBackList().size());
        Assert.assertEquals(TestUpgradeWIthException.class.getName(), UpgradeHelper.getRolledBackList().get(0));
    }

    /**
     * Perform upgrade.
     *
     * @param scope the scope
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void performUpgrade(String scope) throws OnmsUpgradeException {
        Upgrade upgrade = new Upgrade();
        upgrade.setClassScope(scope);
        upgrade.setUpgradeStatus(upgradeStatus);
        upgrade.execute();
    }
}
