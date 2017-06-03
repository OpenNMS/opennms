/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.opennms.core.utils.AlphaNumeric;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.DefaultDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.upgrade.api.AbstractOnmsUpgrade;
import org.opennms.upgrade.api.OnmsUpgradeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.snmp4j.smi.OID;
import org.snmp4j.smi.OctetString;
import org.springframework.core.io.FileSystemResource;

/**
 * The Class RRD/JRB Migrator for F5 LTM pool & virtual-server names.
 *
 * <p>The fix for the following issues is going to break existing collected data specially for JRBs.
 * For this reason, these files must be updated too.</p>
 *
 * <p>Issues fixed:</p>
 * <ul>
 * <li>NMS-8587</li>
 * </ul>
 *
 * @author <a href="mailto:roskens@opennms.org">Ronald Roskens</a>
 */
public class F5LTMRrdMigratorOffline extends AbstractOnmsUpgrade {

    private static final Logger LOG = LoggerFactory.getLogger(F5LTMRrdMigratorOffline.class);

    private final DefaultDataCollectionConfigDao m_dataCollectionConfigDao = new DefaultDataCollectionConfigDao();

    /** The JMX resource directories. */
    private List<File> f5ResourceDirectories;

    /**
     * The JMX resource directories.
     */
    protected List<File> processedDirectories = new ArrayList<>();

    /**
     * Backup files.
     */
    protected List<File> backupFiles = new ArrayList<>();

    /**
     * Instantiates a new JMX RRD migrator offline.
     *
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    public F5LTMRrdMigratorOffline() throws OnmsUpgradeException {
        super();
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getOrder()
     */
    @Override
    public int getOrder() {
        return 4;
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#getDescription()
     */
    @Override
    public String getDescription() {
        return "Fix the directory index names for the F5 LTM pools and virtual-servers: NMS-8587";
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
        printMainSettings();
        try {
            File sourceFile = Paths.get(ConfigFileConstants.getHome(), "etc", "datacollection-config.xml").toFile();
            File configDirectory = new File(sourceFile.getParentFile().getAbsolutePath(), "datacollection");
            m_dataCollectionConfigDao.setConfigDirectory(configDirectory.getAbsolutePath());
            m_dataCollectionConfigDao.setConfigResource(new FileSystemResource(sourceFile));
            m_dataCollectionConfigDao.setReloadCheckInterval(new Long(0));
            m_dataCollectionConfigDao.afterPropertiesSet();
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't initialize datacollection-config.xml because " + e.getMessage());
        }
        for (File f5ResourceDir : getF5ResourceDirectories()) {
            log("Backing up %s\n", f5ResourceDir);
            zipDir(new File(f5ResourceDir.getAbsolutePath() + ZIP_EXT), f5ResourceDir);
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#postExecute()
     */
    @Override
    public void postExecute() throws OnmsUpgradeException {
        for (File f5ResourceDir : getF5ResourceDirectories()) {
            File zip = new File(f5ResourceDir.getAbsolutePath() + ZIP_EXT);
            if (zip.exists()) {
                log("Removing backup %s\n", zip);
                FileUtils.deleteQuietly(zip);
            }
        }
        for (File backupFile : backupFiles) {
            log("Removing backup %s\n", backupFile);
            FileUtils.deleteQuietly(backupFile);
            FileUtils.deleteQuietly(new File(backupFile.getAbsolutePath().replaceFirst(ZIP_EXT, ".temp")));
        }
    }

    /* (non-Javadoc)
     * @see org.opennms.upgrade.api.OnmsUpgrade#rollback()
     */
    @Override
    public void rollback() throws OnmsUpgradeException {
        try {
            for (File f5ResourceDir : processedDirectories) {
                LOG.debug("Removing processed directory {}", f5ResourceDir);
                FileUtils.deleteDirectory(f5ResourceDir);
                OctetString oString = new OctetString(f5ResourceDir.getName());
                OID oid = oString.toSubIndex(false);
                File origResourceDir = new File(f5ResourceDir.getParentFile(), oid.toDottedString());
                File zip = new File(origResourceDir.getAbsolutePath() + ZIP_EXT);
                if (!origResourceDir.mkdirs()) {
                    LOG.warn("Could not make directory: {}", origResourceDir.getPath());
                }
                unzipFile(zip, origResourceDir);
                if(!zip.delete()) {
                	LOG.warn("Could not delete file: {}", zip.getPath());
                }
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
        try {
            // Fixing JRB/RRD files
            for (File f5ResourceDir : getF5ResourceDirectories()) {
                processF5Directories(f5ResourceDir);
            }
        } catch (Exception e) {
            throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
        }
    }

    /**
     * Gets the JMX resource directories.
     *
     * @return the JMX resource directories
     * @throws OnmsUpgradeException the OpenNMS upgrade exception
     */
    private List<File> getF5ResourceDirectories() throws OnmsUpgradeException {
        if (f5ResourceDirectories == null) {
            f5ResourceDirectories = new ArrayList<>();
            CollectdConfiguration config;
            try {
                config = new CollectdConfigFactory().getCollectdConfig();
            } catch (Exception e) {
                throw new OnmsUpgradeException("Can't upgrade the JRBs because " + e.getMessage(), e);
            }
            File rrdDir = new File(m_dataCollectionConfigDao.getRrdPath());
            findF5LocalTMDirectories(rrdDir, f5ResourceDirectories);
            if (f5ResourceDirectories.isEmpty()) {
                log("Warning: no F5 LocalTM directories found on %s\n", rrdDir);
            }
        }
        return f5ResourceDirectories;
    }

    /**
     * Find F5 LocalTM directories.
     *
     * @param rrdDir the RRD directory
     * @param f5Directories the target list for JMX directories
     */
    private void findF5LocalTMDirectories(final File rrdDir, final List<File> f5Directories) {
        OctetString oString = new OctetString();
        File[] files = rrdDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                if (file.getName().matches("^\\d+(\\.\\d+)+$")) {
                    try {
                        OID oid = new OID(file.getName());
                        oString.fromSubIndex(oid, false);
                        f5Directories.add(file);
                    } catch (UnsupportedOperationException e) {
                        // Ignore directories that can't be converted from an OID subindex.
                    }
                }
                findF5LocalTMDirectories(file, f5Directories);
            }
        }
    }

    /**
     * Process the directories that should be renamed.
     *
     * @param resourceDir the resource directory
     * @throws Exception the exception
     */
    private void processF5Directories(File resourceDir) throws Exception {
        try {
            OctetString oString = new OctetString();
            OID oid = new OID(resourceDir.getName());
            oString.fromSubIndex(oid, false);
            log("Processing %s\n", resourceDir);
            File newResourceDir = new File(resourceDir.getParentFile(), oString.toString());
            log("Renaming %s to %s\n", resourceDir, newResourceDir);
            FileUtils.moveDirectory(resourceDir, newResourceDir);
            processedDirectories.add(newResourceDir);
        } catch (IOException e) {
            log("Warning: Can't move directory because: %s\n", e.getMessage());
        }
    }

    /**
     * Gets the fixed DS name.
     *
     * @param dsName the DS name
     * @return the fixed DS name
     */
    protected String getFixedDsName(String dsName) {
        if (dsName.contains(".")) {
            String[] parts = dsName.split("\\.");
            return parts[0] +  parts[1].substring(0, 1).toUpperCase() + parts[1].substring(1);
        }
        return dsName;
    }

    /**
     * Gets the fixed file name.
     *
     * @param oldFile the old file
     * @return the fixed file name
     */
    protected String getFixedFileName(String oldFile) {
        return AlphaNumeric.parseAndReplace(oldFile, '_');
    }

}
