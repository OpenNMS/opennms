/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
