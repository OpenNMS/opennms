/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.netmgt.config.KSC_PerformanceReportFactory;
import org.opennms.netmgt.config.kscReports.Graph;
import org.opennms.netmgt.config.kscReports.Report;
import org.opennms.netmgt.vmmgr.ControllerUtils;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;

/**
 * The Class KSC Reports Migrator.
 * 
 * <p>1.12 always add the MAC Address to the snmpinterface table if exist, which
 * is different from the 1.10 behavior. For this reason, some interfaces are going
 * to appear twice, and the data must be merged.</p>
 * 
 * <p>For this reason, the KSC reports must be updated.</p>
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a> 
 */
public class KscReportsMigrator extends AbstractOnmsUpgrade {

    /** The KSC Reports configuration file. */
    private File configFile;

    /**
     * Instantiates a new KSC Reports Migrator.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public KscReportsMigrator() throws OnmsUpgradeException {
        super();
        try {
            configFile = ConfigFileConstants.getFile(ConfigFileConstants.KSC_REPORT_FILE_NAME);
        } catch (IOException e) {
            throw new OnmsUpgradeException("Can't find KSC Configuration file", e);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    public int getOrder() {
        return 6;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    public String getDescription() {
        return "Fixes the KSC Reports because of SNMP Interface directories changes";
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#requiresOnmsRunning()
     */
    public boolean requiresOnmsRunning() {
        // A trick to execute the task no matter if OpenNMS is running or not.
        return isOpennmsRunning() ? true : false;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#preExecute()
     */
    public void preExecute() throws OnmsUpgradeException {
        try {
            log("Backing up %s\n", configFile);
            zipFile(configFile);
            initializeDatasource();
            KSC_PerformanceReportFactory.init();
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't initialize ksc-performance-reports.xml because " + e.getMessage());
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    public void postExecute() throws OnmsUpgradeException {
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        if (zip.exists()) {
            log("Removing backup %s\n", zip);
            FileUtils.deleteQuietly(zip);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        log("Restoring backup %s\n", configFile);
        File zip = new File(configFile.getAbsolutePath() + ZIP_EXT);
        FileUtils.deleteQuietly(configFile);
        unzipFile(zip, zip.getParentFile());
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#execute()
     */
    @Override
    public void execute() throws OnmsUpgradeException {
        log("Fixing KSC Reports.\n");
        boolean changed = false;
        List<SnmpInterface> interfacesToMerge = getInterfacesToMerge();
        for (Integer reportId : KSC_PerformanceReportFactory.getInstance().getReportList().keySet()) {
            Report report = KSC_PerformanceReportFactory.getInstance().getReportByIndex(reportId);
            log("  Checking report %s\n", report.getTitle());
            for (Graph graph : report.getGraphs()) {
                for (SnmpInterface intf : interfacesToMerge) {
                    final String resourceId = graph.getResourceId().orElse(null);
                    if (intf.shouldUpdate(resourceId)) {
                        changed = true;
                        log("  replacing resource ID %s with %s for %s\n", graph.getResourceId(), intf.getNewResourceId(), graph.getTitle());
                        graph.setResourceId(intf.getNewResourceId().toString());
                    }
                }
            }
        }
        if (changed) {
            log("Updating the KSC reports configuration file.\n");
            try {
                KSC_PerformanceReportFactory.getInstance().saveCurrent();
            } catch (Exception e) {
                log("Warning: can't save KSC Reports because %s\n", e.getMessage());
            }
            if (isOpennmsRunning()) {
                log("In case the OpenNMS WebUI can't see the changes, go to Reports -> KSC Performance, Nodes, Domains and click on 'Request a Reload of KSC Reports Configuration'\n");
            }
        } else {
            log("No incomplete interface names detected.\n");
        }
    }

    /**
     * Gets the interfaces to merge.
     *
     * @return the list of interfaces to merge
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected List<SnmpInterface> getInterfacesToMerge() throws OnmsUpgradeException {
        List<SnmpInterface> interfacesToMerge = new ArrayList<>();
        Connection conn = getDbConnection();
        final DBUtils db = new DBUtils(getClass());
        db.watch(conn);
        try {
            Statement st = conn.createStatement();
            db.watch(st);
            String query = "SELECT n.nodeid, n.foreignsource, n.foreignid, i.snmpifdescr, i.snmpifname, i.snmpphysaddr from node n, snmpinterface i where n.nodeid = i.nodeid and i.snmpphysaddr is not null order by n.nodeid, i.snmpifdescr";
            ResultSet rs = st.executeQuery(query);
            db.watch(rs);
            while (rs.next()) {
                interfacesToMerge.add(new SnmpInterface(rs, isStoreByForeignSourceEnabled()));
            }
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
     * Checks if is OpenNMS running.
     *
     * @return true, if is OpenNMS running
     */
    protected boolean isOpennmsRunning() {
        try {
            return ControllerUtils.getController().status() == 0;
        } catch (Exception e) {
            log("Warning: can't retrieve OpeNNMS status (assuming it is not running).\n");
            return false;
        }
    }
}
