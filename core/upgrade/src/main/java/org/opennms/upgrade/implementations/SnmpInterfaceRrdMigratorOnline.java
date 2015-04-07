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

package org.opennms.upgrade.implementations;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.rrd.model.RrdConvertUtils;
import org.opennms.netmgt.rrd.model.v1.RRDv1;
import org.opennms.netmgt.rrd.model.v3.RRDv3;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class RRD/JRB Migrator for SNMP Interfaces Data (Online Version)
 * 
 * <p>1.12 always add the MAC Address to the snmpinterface table if exist, which
 * is different from the 1.10 behavior. For this reason, some interfaces are going
 * to appear twice, and the data must be merged.</p>
 * 
 * <p>This tool requires that OpenNMS 1.12 is running for a while to be sure that
 * all the MAC addresses have been updated, and the directories already exist.</p>
 * 
 * <p>Issues fixed:</p>
 * <ul>
 * <li>NMS-6056</li>
 * </ul>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class SnmpInterfaceRrdMigratorOnline extends AbstractOnmsUpgrade {

    private static final Logger LOG = LoggerFactory.getLogger(SnmpInterfaceRrdMigratorOnline.class);

    /** The interfaces to merge. */
    private List<SnmpInterfaceUpgrade> interfacesToMerge;

    /**
     * Instantiates a new SNMP interface RRD migrator online.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public SnmpInterfaceRrdMigratorOnline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    public int getOrder() {
        return 2;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    public String getDescription() {
        return "Merge SNMP Interface directories (Online Version): NMS-6056";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    public boolean requiresOnmsRunning() {
        return true;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    public void preExecute() throws OnmsUpgradeException {
        printMainSettings();
        if (getRrdExtension() == null) {
            throw new OnmsUpgradeException("Can't find the configured extension for JRB/RRD.");
        }
        try {
            // Manually initialization of the DataCollectionConfigDao to avoid bootstrap Spring Framework and create a new connection pool.
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);
            DefaultDataCollectionConfigDao config = new DefaultDataCollectionConfigDao();
            config.setConfigResource(new FileSystemResource(cfgFile));
            config.afterPropertiesSet();
            config.getConfiguredResourceTypes();
            DataCollectionConfigFactory.setInstance(config);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't initialize datacollection-config.xml because " + e.getMessage());
        }
        interfacesToMerge = getInterfacesToMerge();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    public void postExecute() throws OnmsUpgradeException {
        // The idea is to remove the backups only if the current interface was updated. A global post-execute is not necessary.
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        // The idea is to roll back only if the current interface was not updated. A global roll back is not necessary.
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        for (SnmpInterfaceUpgrade intf : interfacesToMerge) {
            try {
                createBackup(intf);
                merge(intf.getOldInterfaceDir(), intf.getNewInterfaceDir());
                removeBackup(intf);
            } catch (Exception e) {
                StringWriter w = new StringWriter();
                PrintWriter p = new PrintWriter(w);
                e.printStackTrace(p);
                log("Error: Can't upgrade %s because %s: %s. Rolling back changes\n", intf, e.getMessage(), w.toString());
                restoreBackup(intf);
            }
        }
    }

    /**
     * Crates a backup of the SNMP interface files.
     *
     * @param intf the SNMP interface
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void createBackup(SnmpInterfaceUpgrade intf) throws OnmsUpgradeException {
        File[] targets = { intf.getOldInterfaceDir(), intf.getNewInterfaceDir() };
        for (File target : targets) {
            if (target.exists()) {
                log("Backing up: %s\n", target);
                zipDir(new File(target.getAbsolutePath() + ZIP_EXT), target);
            }
        }
    }

    /**
     * Restore the backup for a specific SNMP interface
     *
     * @param intf the SNMP interface
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void restoreBackup(SnmpInterfaceUpgrade intf) throws OnmsUpgradeException {
        File[] targets = { intf.getOldInterfaceDir(), intf.getNewInterfaceDir() };
        for (File target : targets) {
            File zip = new File(target.getAbsolutePath() + ZIP_EXT);
            try {
                FileUtils.deleteDirectory(target);
            } catch (IOException e1) {
                log("Warning: can't delete directory %s\n", target);
            }
            if (!target.mkdirs()) {
                LOG.warn("Could not make directory: {}", target.getPath());
            }
            if (zip.exists()) {
                unzipFile(zip, target);
                if (!zip.delete()) {
                    LOG.warn("Could not delete file: {}", zip.getPath());
                }
            }
        }
    }

    /**
     * Removes the backups for a specific SNMP interface
     *
     * @param intf the SNMP interface
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void removeBackup(SnmpInterfaceUpgrade intf) {
        File[] targets = { intf.getOldInterfaceDir(), intf.getNewInterfaceDir() };
        for (File target : targets) {
            File zip = new File(target.getAbsolutePath() + ZIP_EXT);
            if (zip.exists()) {
                log("Removing backup: %s\n", zip);
                if (!zip.delete()) {
                    LOG.warn("Could not delete file: {}", zip.getPath());
                }
            }
        }
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the list of interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<SnmpInterfaceUpgrade> getInterfacesToMerge() throws OnmsUpgradeException {
        List<SnmpInterfaceUpgrade> interfacesToMerge = new ArrayList<SnmpInterfaceUpgrade>();
        Connection conn = getDbConnection();
        final DBUtils db = new DBUtils(getClass());
        db.watch(conn);
        try {
            Statement st = conn.createStatement();
            db.watch(st);
            String query = "SELECT n.nodeid, n.foreignsource, n.foreignid, i.snmpifdescr, i.snmpifname, i.snmpphysaddr from node n, snmpinterface i where n.nodeid = i.nodeid and i.snmpphysaddr is not null order by n.nodeid, i.snmpifdescr";
            ResultSet rs = st.executeQuery(query);
            db.watch(rs);
            int count = 0;
            while (rs.next()) {
                count++;
                SnmpInterfaceUpgrade intf = new SnmpInterfaceUpgrade(rs, isStoreByForeignSourceEnabled());
                if (intf.shouldMerge()) {
                    interfacesToMerge.add(intf);
                }
                log("  Should merge %s ? %s\n", intf, intf.shouldMerge());
            }
            log("  Found %s of %s SNMP interfaces that require a merge.\n", interfacesToMerge.size(), count);
        } catch (Throwable t) {
            log("Error: can't retrieve the required data from the OpenNMS Database or there were problems while processing them.\n");
            String reason = t.getMessage();
            if (reason == null) {
                reason = "Unknown";
            }
            log("Reason(%s): %s\n", t.getClass().getName(), reason);
            t.printStackTrace(); // TODO This is not elegant, but it helps.
        } finally {
            db.cleanUp();
        }
        return interfacesToMerge;
    }

    /**
     * Merge.
     *
     * @param oldDir the old directory
     * @param newDir the new directory
     * @throws Exception the exception
     */
    protected void merge(File oldDir, File newDir) throws Exception {
        log("Merging data from %s to %s\n", oldDir, newDir);
        if (newDir.exists()) {
            File[] rrdFiles = getFiles(oldDir, getRrdExtension());
            if (rrdFiles == null) {
                log("Warning: there are no %s files on %s\n", getRrdExtension(), oldDir);
            } else {
                for (File source : rrdFiles) {
                    File dest = new File(newDir, source.getName());
                    if (dest.exists()) {
                        if (isRrdToolEnabled()) {
                            mergeRrd(source, dest);
                        } else {
                            mergeJrb(source, dest);
                        }
                    } else {
                        log("  Warning: %s doesn't exist\n", dest);
                    }
                }
            }
            try {
                log("  removing old directory %s\n", oldDir.getName());
                FileUtils.deleteDirectory(oldDir);
            } catch (Exception e) {
                log("  Warning: can't delete old directory because %s", e.getMessage());
            }
        } else {
            try {
                log("  moving %s to %s\n", oldDir.getName(), newDir.getName());
                FileUtils.moveDirectory(oldDir, newDir);
            } catch (Exception e) {
                log("  Warning: can't rename directory because %s", e.getMessage());
            }
        }
    }

    /**
     * Merge RRDs.
     *
     * @param source the source RRD
     * @param dest the destination RRD
     * @throws Exception the exception
     */
    protected void mergeRrd(File source, File dest) throws Exception {
        log("  merging RRD %s into %s\n", source, dest);
        RRDv3 rrdSrc = RrdConvertUtils.dumpRrd(source);
        RRDv3 rrdDst = RrdConvertUtils.dumpRrd(dest);
        if (rrdSrc == null || rrdDst == null) {
            log("  Warning: can't load RRDs (ingoring merge).\n");
            return;
        }
        rrdDst.merge(rrdSrc);
        final File outputFile = new File(dest.getCanonicalPath() + ".merged");
        RrdConvertUtils.restoreRrd(rrdDst, outputFile);
        if (dest.exists()) {
            FileUtils.deleteQuietly(dest);
        }
        FileUtils.moveFile(outputFile, dest);
    }

    /**
     * Merge JRBs.
     *
     * @param source the source JRB
     * @param dest the destination JRB
     * @throws Exception the exception
     */
    protected void mergeJrb(File source, File dest) throws Exception {
        log("  merging JRB %s into %s\n", source, dest);
        RRDv1 rrdSrc = RrdConvertUtils.dumpJrb(source);
        RRDv1 rrdDst = RrdConvertUtils.dumpJrb(dest);
        if (rrdSrc == null || rrdDst == null) {
            log("  Warning: can't load JRBs (ingoring merge).\n");
            return;
        }
        rrdDst.merge(rrdSrc);
        final File outputFile = new File(dest.getCanonicalPath() + ".merged");
        RrdConvertUtils.restoreJrb(rrdDst, outputFile);
        if (dest.exists()) {
            FileUtils.deleteQuietly(dest);
        }
        FileUtils.moveFile(outputFile, dest);
    }

    /**
     * Gets the node directory.
     *
     * @param nodeId the node id
     * @param foreignSource the foreign source
     * @param foreignId the foreign id
     * @return the node directory
     */
    protected File getNodeDirectory(int nodeId, String foreignSource, String foreignId) {
        File dir = new File(DataCollectionConfigFactory.getInstance().getRrdPath(), String.valueOf(nodeId));
        if (Boolean.getBoolean("org.opennms.rrd.storeByForeignSource") && !(foreignSource == null) && !(foreignId == null)) {
            File fsDir = new File(DataCollectionConfigFactory.getInstance().getRrdPath(), "fs" + File.separatorChar + foreignSource);
            dir = new File(fsDir, foreignId);
        }
        return dir;
    }

}
