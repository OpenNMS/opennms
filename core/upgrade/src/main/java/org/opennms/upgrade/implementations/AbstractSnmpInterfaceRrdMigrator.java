/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Abstract Class RRD/JRB Migrator for SNMP Interfaces Data
 * 
 * <p>1.12 always add the MAC Address to the snmpinterface table if exist, which
 * is different from the 1.10 behavior. For this reason, some interfaces are going
 * to appear twice, and the data must be merged.</p>
 * 
 * <ul>
 * <li>NMS-6056</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public abstract class AbstractSnmpInterfaceRrdMigrator extends AbstractOnmsUpgrade {

    /** The interfaces to merge. */
    private Map<File,File> interfacesToMerge;

    /**
     * Instantiates a new Abstract SNMP interface RRD migrator.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public AbstractSnmpInterfaceRrdMigrator() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    @Override
    public void preExecute() throws OnmsUpgradeException {
        try {
            DataCollectionConfigFactory.init();
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't initialize datacollection-config.xml because " + e.getMessage());
        }
        interfacesToMerge = getInterfacesToMerge();
        for (File target : interfacesToMerge.values()) {
            if (target.exists()) {
                log("Backing up %s\n", target);
                zipDir(target.getAbsolutePath() + ".zip", target);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        for (File target : interfacesToMerge.values()) {
            File zip = new File(target.getAbsolutePath() + ".zip");
            if (zip.exists()) {
                log("Removing backup %s\n", zip);
                zip.delete();
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        try {
            for (File target : interfacesToMerge.values()) {
                File zip = new File(target.getAbsolutePath() + ".zip");
                FileUtils.deleteDirectory(target);
                target.mkdirs();
                unzipDir(zip, target);
                zip.delete();
            }
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't restore the backup files because " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        for (Entry<File,File> entry : interfacesToMerge.entrySet()) {
            merge(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected abstract Map<File,File> getInterfacesToMerge() throws OnmsUpgradeException;    

    /**
     * Merge.
     *
     * @param oldFile the old file
     * @param newFile the new file
     */
    protected void merge(File oldFile, File newFile) {
        log("Merging data from %s to %s\n", oldFile, newFile);

        // FIXME Must be implemented
    }

    /**
     * Gets the node directory.
     *
     * @param node the node
     * @return the node directory
     */
    protected File getNodeDirectory(OnmsNode node) {
        File dir = new File(DataCollectionConfigFactory.getInstance().getRrdPath(), String.valueOf(node.getId()));
        if (Boolean.getBoolean("org.opennms.rrd.storeByForeignSource") && !(node.getForeignSource() == null) && !(node.getForeignId() == null)) {
            File fsDir = new File(DefaultResourceDao.FOREIGN_SOURCE_DIRECTORY, node.getForeignSource());
            dir = new File(fsDir, node.getForeignId());
        }
        return dir;
    }

}
