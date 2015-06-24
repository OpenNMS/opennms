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

package org.opennms.upgrade.api;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;

/**
 * The test implementation of AbstractOnmsUpgrade.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class MockOnmsUpgrade extends AbstractOnmsUpgrade {

    /** The Constant TEST_ZIP. */
    public final static File TEST_ZIP = new File("target/zip-test.zip");

    /**
     * Instantiates a new mock OpenNMS upgrade.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public MockOnmsUpgrade() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 1;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Mock Upgrade";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /**
     * Test Zip and Unzip directory.
     *
     * @throws Exception the exception
     */
    public void testZipAndUnzipDirectory() throws Exception {
        zipDir(TEST_ZIP, new File("src/main/java"));
        File output = new File("target/zip-test");
        unzipFile(TEST_ZIP, output);
        Assert.assertTrue(new File(output, "org/opennms/upgrade/api/OnmsUpgrade.java").exists());
        FileUtils.deleteDirectory(output);
    }

    /**
     * Test Zip and Unzip file.
     *
     * @throws Exception the exception
     */
    public void testZipAndUnzipFile() throws Exception {
        File output = new File("target/zip-test");
        File srcFile = new File("src/main/java/org/opennms/upgrade/api/OnmsUpgrade.java");
        File dstFile = new File(output, "/org/opennms/upgrade/api/OnmsUpgrade.java");
        FileUtils.copyFile(srcFile, dstFile);
        zipFile(dstFile);
        File zip = new File(dstFile.getAbsoluteFile() + ".zip");
        Assert.assertTrue(zip.exists());
        FileUtils.deleteQuietly(dstFile);
        unzipFile(zip, output);
        Assert.assertTrue(new File(output, "OnmsUpgrade.java").exists());
        FileUtils.deleteDirectory(output);;
    }

}
