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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.jrobin.core.RrdDb;
import org.opennms.core.utils.StringUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.jrobin.AggregateTimeSeriesDataSource;
import org.opennms.jrobin.RrdDatabase;
import org.opennms.jrobin.RrdDatabaseWriter;
import org.opennms.jrobin.RrdEntry;
import org.opennms.jrobin.TimeSeriesDataSource;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.dao.support.DefaultResourceDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.rrdtool.RRD;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.springframework.util.FileCopyUtils;

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
     * @param oldDir the old directory
     * @param newDir the new directory
     * @throws OnmsUpgradeException the onms upgrade exception
     */
    protected void merge(File oldDir, File newDir) throws OnmsUpgradeException {
        log("Merging data from %s to %s\n", oldDir, newDir);
        if (newDir.exists()) {
            for (File source : getFiles(oldDir, getRrdExtension())) {
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
        } else {
            log("  renaming %s to %s\n", oldDir.getName(), newDir.getName());
            oldDir.renameTo(newDir);
        }
    }

    // https://bitbucket.org/ctheune/rrdmerge/
    // https://github.com/jbuchbinder/rrd-merge
    /**
     * Merge RRDs.
     *
     * @param source the source RRD
     * @param dest the destination RRD
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void mergeRrd(File source, File dest) throws OnmsUpgradeException {
        log("  merging RRD %s\n", source.getName());
        RRD rrdSrc = loadRrd(source);
        RRD rrdDst = loadRrd(dest);
        try {
            rrdDst.merge(rrdSrc);
            JaxbUtils.marshal(rrdDst, new FileWriter(dest));
        } catch (Exception e) {
            log("  Warning: ignoring merge because %s.\n", e.getMessage());
        }
    }

    /**
     * Merge JRBs.
     *
     * @param source the source JRB
     * @param dest the destination JRB
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private void mergeJrb(File source, File dest) throws OnmsUpgradeException {
        log("  merging JRB %s\n", source.getName());
        try {
            final List<RrdDatabase> rrds = new ArrayList<RrdDatabase>();
            rrds.add(new RrdDatabase(new RrdDb(source, true)));
            rrds.add(new RrdDatabase(new RrdDb(dest, true)));
            final TimeSeriesDataSource dataSource = new AggregateTimeSeriesDataSource(rrds);
            final File outputFile = new File(dest.getCanonicalPath() + ".merged");
            final RrdDb outputRrd = new RrdDb(outputFile);
            final RrdDatabaseWriter writer = new RrdDatabaseWriter(outputRrd);
            final long endTime = dataSource.getEndTime();
            final long startTime = dataSource.getStartTime();
            for (long time = startTime; time <= endTime; time += dataSource.getNativeStep()) {
                final RrdEntry entry = dataSource.getDataAt(time);
                writer.write(entry);
            }
            dataSource.close();
            outputRrd.close();
            dest.delete();
            outputFile.renameTo(dest);
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't merge JRBs because " + e.getMessage());
        }
    }

    /**
     * Loads the RRD.
     *
     * @param rrdFile the RRD file
     * @return the RRD
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    protected RRD loadRrd(File rrdFile) throws OnmsUpgradeException {
        String rrdBinary = System.getProperty("rrd.binary");
        if (rrdBinary == null) {
            throw new OnmsUpgradeException("rrd.binary property must be set");
        }
        String command = rrdBinary + " dump " + rrdFile.getAbsolutePath();
        String[] commandArray = StringUtils.createCommandArray(command, '@');
        RRD rrd = null;
        try {
            Process process = Runtime.getRuntime().exec(commandArray);
            byte[] byteArray = FileCopyUtils.copyToByteArray(process.getInputStream());             
            String errors = FileCopyUtils.copyToString(new InputStreamReader(process.getErrorStream()));
            if (errors.length() > 0) {
                throw new OnmsUpgradeException("RRDtool command fail: " + errors);
            }
            BufferedReader reader = null;
            try {
                InputStream is = new ByteArrayInputStream(byteArray);
                reader = new BufferedReader(new InputStreamReader(is));
                rrd = JaxbUtils.unmarshal(RRD.class, reader);
            } finally {
                reader.close();
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't execute command '" + command + "' because " + e.getMessage());
        }
        return rrd;
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
