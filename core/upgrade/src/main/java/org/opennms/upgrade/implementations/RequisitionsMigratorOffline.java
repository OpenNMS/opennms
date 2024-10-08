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
package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The Class Requisitions Migrator.
 * 
 * <p>The attributes <code>non-ip-snmp-primary</code> and <code>non-ip-interfaces</code> which are valid in 1.10 have been removed in 1.12.</p>
 * <p>This tool will parse the raw requisitions XML and remove those tags.</p>
 * 
 * <p>Issues fixed:</p>
 * <ul>
 * <li>NMS-5630</li>
 * <li>NMS-5571</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class RequisitionsMigratorOffline extends AbstractOnmsUpgrade {
	
	private static final Logger LOG = LoggerFactory.getLogger(RequisitionsMigratorOffline.class);

    /** The requisition directory. */
    private File requisitionDir;

    /**
     * Instantiates a new requisitions migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public RequisitionsMigratorOffline() throws OnmsUpgradeException {
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
        return "Remove non-ip-snmp-primary and non-ip-interfaces from requisitions: NMS-5630, NMS-5571";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    @Override
    public boolean requiresOnmsRunning() {
        return false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        log("Backing up: %s\n", getRequisitionDir());
        zipDir(getBackupFile(), getRequisitionDir());
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        File zip = getBackupFile();
        if (zip.exists()) {
            log("Removing backup %s\n", zip);
            if(!zip.delete()) {
            	LOG.warn("Could not delete file: {}",zip.getPath());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        File zip = getBackupFile();
        unzipFile(zip, getRequisitionDir());
        if(!zip.delete()) {
        	LOG.warn("Could not delete file: {}",zip.getPath());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        try {
            for (File req : FileUtils.listFiles(getRequisitionDir(), new String[]{"xml"}, true)) {
                log("Processing %s\n", req);
                String content = IOUtils.toString(new FileInputStream(req), StandardCharsets.UTF_8);
                String output = content.replaceAll(" non-ip-(snmp-primary|interfaces)=\"[^\"]+\"", "");
                if (content.length() != output.length()) {
                    log("  Updating and parsing the requisition\n", req);
                    IOUtils.write(output, new FileOutputStream(req), StandardCharsets.UTF_8);
                    Requisition requisition = JaxbUtils.unmarshal(Requisition.class, req, true);
                    if (requisition == null) {
                        throw new OnmsUpgradeException("Can't parse requisition " + req);
                    }
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't upgrade requisitions because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the requisition directory.
     *
     * @return the requisition directory
     */
    private File getRequisitionDir() {
        if (requisitionDir == null) {
            requisitionDir = new File(ConfigFileConstants.getFilePathString() + "imports");
        }
        return requisitionDir;
    }

    /**
     * Gets the backup file.
     *
     * @return the backup file
     */
    private File getBackupFile() {
        return new File(getRequisitionDir().getAbsoluteFile() + ZIP_EXT);
    }
}
