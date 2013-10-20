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
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.rrdtool.RRD;
import org.opennms.rrdtool.RrdtoolUtils;
import org.opennms.rrdtool.old.RrdOld;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class RRD/JRB Migrator for SNMP Interfaces Data (Online Version)
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
public class SnmpInterfaceRrdMigratorOnline extends AbstractOnmsUpgrade {

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
        try {
            DataCollectionConfigFactory.init();
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't initialize datacollection-config.xml because " + e.getMessage());
        }
        interfacesToMerge = getInterfacesToMerge();
        for (SnmpInterfaceUpgrade intf : interfacesToMerge) {
            File target = intf.getNewInterfaceDir();
            if (target.exists()) {
                log("Backing up %s\n", target);
                zipDir(target.getAbsolutePath() + ".zip", target);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    public void postExecute() throws OnmsUpgradeException {
        for (SnmpInterfaceUpgrade intf : interfacesToMerge) {
            File target = intf.getNewInterfaceDir();
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
            for (SnmpInterfaceUpgrade intf : interfacesToMerge) {
                File target = intf.getNewInterfaceDir();
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
        for (SnmpInterfaceUpgrade intf : interfacesToMerge) {
            merge(intf.getOldInterfaceDir(), intf.getNewInterfaceDir());
            fixKscReports(intf);
        }
    }

    /**
     * Fix KSC reports.
     *
     * @param intf the interface object
     */
    protected void fixKscReports(SnmpInterfaceUpgrade intf) {
        log("FIXME: Fixing the KSC Reports is not implemented yet (sorry about that).");
        // FIXME Auto-generated method stub

    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the list of interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<SnmpInterfaceUpgrade> getInterfacesToMerge() throws OnmsUpgradeException {
        Connection conn = getDbConnection();
        final DBUtils d = new DBUtils(getClass());
        List<SnmpInterfaceUpgrade> interfacesToMerge = new ArrayList<SnmpInterfaceUpgrade>();
        try {
            Statement st = conn.createStatement();
            d.watch(st);
            String query = "SELECT n.nodeid, n.foreignsource, n.foreignid, i.snmpifdescr, i.snmpifname, i.snmpphysaddr from node n, snmpinterface i where n.nodeid = i.nodeid and i.snmpphysaddr is not null";
            ResultSet rs = st.executeQuery(query);
            while (rs.next()) {
                SnmpInterfaceUpgrade intf = new SnmpInterfaceUpgrade(rs);
                if (intf.shouldMerge()) {
                    interfacesToMerge.add(intf);
                }
            }
            conn.close();
        } catch (Exception e) {
            d.cleanUp();
        }
        return interfacesToMerge;
    }

    /**
     * Merge.
     *
     * @param oldDir the old directory
     * @param newDir the new directory
     */
    protected void merge(File oldDir, File newDir) {
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
        } else {
            try {
                log("  moving %s to %s\n", oldDir.getName(), newDir.getName());
                FileUtils.moveFile(oldDir, newDir);
            } catch (IOException e) {
                log("  Warning: can't move file because %s", e.getMessage());
            }
        }
    }

    /**
     * Merge RRDs.
     *
     * @param source the source RRD
     * @param dest the destination RRD
     */
    protected void mergeRrd(File source, File dest) {
        log("  merging RRD %s into %s\n", source, dest);
        try {
            RRD rrdSrc = RrdtoolUtils.dumpRrd(source);
            RRD rrdDst = RrdtoolUtils.dumpRrd(dest);
            if (rrdSrc == null || rrdDst == null) {
                log("  Warning: can't load RRDs (ingoring merge).\n");
            }
            rrdDst.merge(rrdSrc);
            final File outputFile = new File(dest.getCanonicalPath() + ".merged");
            RrdtoolUtils.restoreRrd(rrdDst, outputFile);
            FileUtils.moveFile(outputFile, dest);
        } catch (Exception e) {
            log("  Warning: ignoring merge because %s.\n", e.getMessage());
        }
    }

    /**
     * Merge JRBs.
     *
     * @param source the source JRB
     * @param dest the destination JRB
     */
    protected void mergeJrb(File source, File dest) {
        log("  merging JRB %s into %s\n", source, dest);
        try {
            RrdOld rrdSrc = RrdtoolUtils.dumpJrb(source);
            RrdOld rrdDst = RrdtoolUtils.dumpJrb(dest);
            if (rrdSrc == null || rrdDst == null) {
                log("  Warning: can't load JRBs (ingoring merge).\n");
            }
            rrdDst.merge(rrdSrc);
            final File outputFile = new File(dest.getCanonicalPath() + ".merged");
            RrdtoolUtils.restoreJrb(rrdDst, outputFile);
            FileUtils.moveFile(outputFile, dest);
        } catch (Exception e) {
            log("  Warning: ignoring merge because %s.\n", e.getMessage());
        }
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

    /**
     * Gets the DB connection.
     *
     * @return the DB connection
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected Connection getDbConnection() throws OnmsUpgradeException {
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
            DataSourceConfiguration dsc = null;
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(cfgFile);
                dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, fileInputStream);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            } 
            for (JdbcDataSource ds : dsc.getJdbcDataSourceCollection()) {
                if (ds.getName().equals("opennms")) {
                    return DriverManager.getConnection(ds.getUrl(), ds.getUserName(), ds.getPassword());
                }
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't connect to OpenNMS Database");
        }
        throw new OnmsUpgradeException("Databaseconnection cannot be null");
    }

}
