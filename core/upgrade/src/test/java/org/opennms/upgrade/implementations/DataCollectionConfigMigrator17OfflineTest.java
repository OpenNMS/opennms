/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class DataCollectionConfigMigrator17OfflineTest.
 */
public class DataCollectionConfigMigrator17OfflineTest {

    /** The temporary folder. */
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    /**
     * Sets the up.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        FileUtils.copyDirectory(new File("src/test/resources/etc2"), tempFolder.newFolder("etc"));
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());
    }

    /**
     * Can fix.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     * @throws IOException Signals that an I/O exception has occurred.
     */
    @Test
    public void canFix() throws OnmsUpgradeException, IOException {
        DataCollectionConfigMigrator17Offline task = new DataCollectionConfigMigrator17Offline();
        task.preExecute();
        task.execute();
        task.postExecute();

        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, new File(tempFolder.getRoot(), "etc/datacollection-config.xml"));
        Assert.assertNotNull(config);
        config.getSnmpCollections().forEach(s -> {
            Assert.assertTrue(s.getIncludeCollections().stream().filter(i -> i.getDataCollectionGroup().equals("SNMP-Informant")).findFirst().isPresent());
        });
    }
}
