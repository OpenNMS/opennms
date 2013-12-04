/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

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
            zip.delete();
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        File zip = getBackupFile();
        unzipFile(zip, getRequisitionDir());
        zip.delete();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        try {
            for (File req : FileUtils.listFiles(getRequisitionDir(), new String[]{"xml"}, true)) {
                log("Processing %s\n", req);
                String content = IOUtils.toString(new FileInputStream(req), "UTF-8");
                String output = content.replaceAll(" non-ip-(snmp-primary|interfaces)=\"[^\"]+\"", "");
                if (content.length() != output.length()) {
                    log("  Updating and parsing the requisition\n", req);
                    IOUtils.write(output, new FileOutputStream(req), "UTF-8");
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
