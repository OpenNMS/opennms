/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.Set;
import java.util.TreeSet;

import org.apache.regexp.RE;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.DBUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LogUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.vulnscand.Excludes;
import org.opennms.netmgt.config.vulnscand.Range;
import org.opennms.netmgt.config.vulnscand.ScanLevel;
import org.opennms.netmgt.config.vulnscand.VulnscandConfiguration;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Vulnscand service from the vulnscand-configuration xml file.
 *
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 *
 * @author <a href="mailto:seth@opennms.org">Seth Leger </a>
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 */
public final class VulnscandConfigFactory {
    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * The singleton instance of this factory
     */
    private static VulnscandConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private static VulnscandConfiguration m_config;

    /**
     * Cached value of the plugin lists for each scan level
     */
    private static String[] m_pluginLists = null;

    /**
     * Cached value of the "safe checks" values for each scan level
     */
    private static boolean[] m_safeChecks = null;

    /**
     * Cached set of the excluded IP addresses
     */
    private static Set<Serializable> m_excludes = null;

    /**
     * Whitespace regex
     */
    private static RE m_space = null;

    /**
     * The SQL statement used to determine if an IP address is already in the
     * ipInterface table and there is known.
     */
    private static final String RETRIEVE_IPADDR_SQL = "SELECT ipaddr FROM ipinterface WHERE ipaddr=? AND ismanaged!='D'";

    /**
     * The SQL statement used to determine if an IP address is already in the
     * ipInterface table and if so what its parent nodeid is.
     */
    private static final String RETRIEVE_IPADDR_NODEID_SQL = "SELECT nodeid FROM ipinterface WHERE ipaddr=? AND ismanaged!='D'";

    /**
     * Constructs a new VulnscandConfigFactory object for access to the
     * Vulnscand configuration information.
     * 
     * @param configFile
     *            The configuration file to load.
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private VulnscandConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(VulnscandConfiguration.class, new FileSystemResource(configFile));
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.VULNSCAND_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(VulnscandConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new VulnscandConfigFactory(cfgFile.getPath());

        try {
            m_space = new RE("[:space:]+");
        } catch (org.apache.regexp.RESyntaxException ex) {
            ThreadCategory.getInstance(VulnscandConfigFactory.class).error("UNEXPECTED CONDITION: Regex in config factory is incorrect. Check the code.", ex);
        }

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        // Destroy all cached values
        m_pluginLists = null;
        m_safeChecks = null;
        m_excludes = null;

        init();
    }

    /**
     * Saves the current settings to disk
     *
     * @throws java.lang.Exception if any.
     */
    public static synchronized void saveCurrent() throws Exception {
        // Marshal to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the XML from the marshal is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        if (stringWriter.toString() != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.VULNSCAND_CONFIG_FILE_NAME)), "UTF-8");
            fileWriter.write(stringWriter.toString());
            fileWriter.flush();
            fileWriter.close();
        }

        reload();
    }

    /**
     * Return the singleton instance of this factory.
     *
     * @return The current factory instance.
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized VulnscandConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the Vulnscand configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.vulnscand.VulnscandConfiguration} object.
     */
    public static VulnscandConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * This method is used to convert the passed IP address to a
     * <code>long</code> value. The address is converted in network byte order
     * (big endian). This is compatible with the number format of the JVM, and
     * thus the return longs can be compared with other converted IP Addresses
     * to determine inclusion.
     *
     * @param addr
     *            The IP address to convert.
     * @return The converted IP address.
     * @deprecated See
     *             org.opennms.core.utils.InetAddressCollection.toLong(InetAddress
     *             addr)
     */
    public static long toLong(InetAddress addr) {
        byte[] baddr = addr.getAddress();

        return ((((long) baddr[0] & 0xffL) << 24) | (((long) baddr[1] & 0xffL) << 16) | (((long) baddr[2] & 0xffL) << 8) | ((long) baddr[3] & 0xffL));
    }

    /**
     * <P>
     * Converts a 64-bit unsigned quantity to a IPv4 dotted decimal string
     * address.
     * </P>
     *
     * @param address
     *            The 64-bit quantity to convert.
     * @return The dotted decimal IPv4 address string.
     * @throws java.net.UnknownHostException if any.
     */
    public static InetAddress toInetAddress(long address) throws UnknownHostException {
        StringBuffer buf = new StringBuffer();
        buf.append((int) ((address >>> 24) & 0xff)).append('.');
        buf.append((int) ((address >>> 16) & 0xff)).append('.');
        buf.append((int) ((address >>> 8) & 0xff)).append('.');
        buf.append((int) (address & 0xff));

        return InetAddressUtils.addr(buf.toString());
    }

    /**
     * <p>isInterfaceInDB</p>
     *
     * @param dbConn a {@link java.sql.Connection} object.
     * @param ifAddress a {@link java.net.InetAddress} object.
     * @return a boolean.
     * @throws java.sql.SQLException if any.
     */
    public static boolean isInterfaceInDB(Connection dbConn, InetAddress ifAddress) throws SQLException {
        boolean result = false;

        final String hostAddress = InetAddressUtils.str(ifAddress);
		LogUtils.debugf(VulnscandConfigFactory.class, "isInterfaceInDB: attempting to lookup interface %s in the database.", hostAddress);

        DBUtils d = new DBUtils(VulnscandConfigFactory.class);
        try {
            PreparedStatement s = dbConn.prepareStatement(RETRIEVE_IPADDR_SQL);
            d.watch(s);
            s.setString(1, hostAddress);
    
            ResultSet rs = s.executeQuery();
            d.watch(rs);
            if (rs.next())
                result = true;
        } finally {
            d.cleanUp();
        }

        return result;
    }

    /**
     * <p>getInterfaceDbNodeId</p>
     *
     * @param dbConn a {@link java.sql.Connection} object.
     * @param ifAddress a {@link java.net.InetAddress} object.
     * @return a int.
     * @throws java.sql.SQLException if any.
     */
    public static int getInterfaceDbNodeId(Connection dbConn, InetAddress ifAddress) throws SQLException {
        final String hostAddress = InetAddressUtils.str(ifAddress);
		LogUtils.debugf(VulnscandConfigFactory.class, "getInterfaceDbNodeId: attempting to lookup interface %s in the database.", hostAddress);

        // Set connection as read-only
        //
        // dbConn.setReadOnly(true);

        int nodeid = -1;
        DBUtils d = new DBUtils(VulnscandConfigFactory.class);
        try {
            PreparedStatement s = dbConn.prepareStatement(RETRIEVE_IPADDR_NODEID_SQL);
            d.watch(s);
            s.setString(1, hostAddress);
    
            ResultSet rs = s.executeQuery();
            d.watch(rs);
            if (rs.next()) {
                nodeid = rs.getInt(1);
            }
        } finally {
            d.cleanUp();
        }

        return nodeid;
    }

    private static ScanLevel getScanLevel(int level) {
        Enumeration<ScanLevel> scanLevels = m_config.enumerateScanLevel();

        while (scanLevels.hasMoreElements()) {
            ScanLevel scanLevel = scanLevels.nextElement();
            if (level == scanLevel.getLevel()) {
                return scanLevel;
            }
        }
        throw new ArrayIndexOutOfBoundsException("No scan level with that index could be located in the configuration file, index = " + level);
    }

    /**
     * <p>addSpecific</p>
     *
     * @param level a int.
     * @param specific a {@link java.net.InetAddress} object.
     */
    public void addSpecific(int level, InetAddress specific) {
        addSpecific(getScanLevel(level), specific);
    }

    /**
     * <p>addSpecific</p>
     *
     * @param level a {@link org.opennms.netmgt.config.vulnscand.ScanLevel} object.
     * @param specific a {@link java.net.InetAddress} object.
     */
    public void addSpecific(ScanLevel level, InetAddress specific) {
        level.addSpecific(InetAddressUtils.str(specific));
    }

    /**
     * <p>addRange</p>
     *
     * @param level a int.
     * @param begin a {@link java.net.InetAddress} object.
     * @param end a {@link java.net.InetAddress} object.
     */
    public void addRange(int level, InetAddress begin, InetAddress end) {
        addRange(getScanLevel(level), begin, end);
    }

    /**
     * <p>addRange</p>
     *
     * @param level a {@link org.opennms.netmgt.config.vulnscand.ScanLevel} object.
     * @param begin a {@link java.net.InetAddress} object.
     * @param end a {@link java.net.InetAddress} object.
     */
    public void addRange(ScanLevel level, InetAddress begin, InetAddress end) {
        Range addMe = new Range();
        addMe.setBegin(InetAddressUtils.str(begin));
        addMe.setEnd(InetAddressUtils.str(end));

        level.addRange(addMe);
    }

    /**
     * <p>removeSpecific</p>
     *
     * @param level a int.
     * @param specific a {@link java.net.InetAddress} object.
     */
    public void removeSpecific(int level, InetAddress specific) {
        removeSpecific(getScanLevel(level), specific);
    }

    /**
     * <p>removeSpecific</p>
     *
     * @param level a {@link org.opennms.netmgt.config.vulnscand.ScanLevel} object.
     * @param specific a {@link java.net.InetAddress} object.
     */
    public void removeSpecific(ScanLevel level, InetAddress specific) {
        level.removeSpecific(InetAddressUtils.str(specific));
    }

    /**
     * <p>removeRange</p>
     *
     * @param level a int.
     * @param begin a {@link java.net.InetAddress} object.
     * @param end a {@link java.net.InetAddress} object.
     */
    public void removeRange(int level, InetAddress begin, InetAddress end) {
        removeRange(getScanLevel(level), begin, end);
    }

    /**
     * <p>removeRange</p>
     *
     * @param level a {@link org.opennms.netmgt.config.vulnscand.ScanLevel} object.
     * @param begin a {@link java.net.InetAddress} object.
     * @param end a {@link java.net.InetAddress} object.
     */
    public void removeRange(ScanLevel level, InetAddress begin, InetAddress end) {
        Range removeMe = new Range();
        removeMe.setBegin(InetAddressUtils.str(begin));
        removeMe.setEnd(InetAddressUtils.str(end));

        level.removeRange(removeMe);
    }

    /**
     * <p>getAllIpAddresses</p>
     *
     * @param level a int.
     * @return a {@link java.util.Set} object.
     */
    public Set<InetAddress> getAllIpAddresses(int level) {
        return getAllIpAddresses(getScanLevel(level));
    }

    /**
     * <p>getAllIpAddresses</p>
     *
     * @param level a {@link org.opennms.netmgt.config.vulnscand.ScanLevel} object.
     * @return a {@link java.util.Set} object.
     */
    public Set<InetAddress> getAllIpAddresses(ScanLevel level) {
        Set<InetAddress> retval = new TreeSet<InetAddress>();

        Enumeration<Range> e = level.enumerateRange();
        while (e.hasMoreElements()) {
            Range ir = e.nextElement();

            try {
                for (long i = Long.parseLong(ir.getBegin()); i <= Long.parseLong(ir.getEnd()); i++) {
	              retval.add(toInetAddress(i));
                }
	    } catch (NumberFormatException wanker) {
            	ThreadCategory.getInstance(getClass()).warn("Failed to convert address range (" + ir.getBegin() + ", " + ir.getEnd() + ")", wanker);
            } catch (UnknownHostException uhE) {
                ThreadCategory.getInstance(getClass()).warn("Failed to convert address range (" + ir.getBegin() + ", " + ir.getEnd() + ")", uhE);
            }

        }

        Enumeration<String> specifics = level.enumerateSpecific();
        while (specifics.hasMoreElements()) {
            String current = specifics.nextElement();
            final InetAddress addr = InetAddressUtils.addr(current);
            if (addr == null) {
                ThreadCategory.getInstance().warn("Failed to convert address: " + current);
            }
			retval.add(addr);
        }

        return retval;
    }

    /**
     * <p>getAllExcludes</p>
     *
     * @return a {@link java.util.Set} object.
     */
    public Set<Serializable> getAllExcludes() {
	ThreadCategory log = ThreadCategory.getInstance(VulnscandConfigFactory.class);
        if (m_excludes == null) {
            m_excludes = new TreeSet<Serializable>();

            Excludes excludes = m_config.getExcludes();

            if (excludes != null) {
                if (excludes.getRangeCount() > 0) {
                    Enumeration<Range> e = excludes.enumerateRange();
                    while (e.hasMoreElements()) {
                        Range ir = e.nextElement();

                        try {
                            for (long i = Long.parseLong(ir.getBegin()); i <= Long.parseLong(ir.getEnd()); i++) {
                                m_excludes.add(toInetAddress(i));
                            }
                        } catch (UnknownHostException uhE) {
                            ThreadCategory.getInstance(getClass()).warn("Failed to convert address range (" + ir.getBegin() + ", " + ir.getEnd() + ")", uhE);
                        }
                    }
                }

                if (excludes.getSpecificCount() > 0) {
                    Enumeration<String> e = excludes.enumerateSpecific();
                    while (e.hasMoreElements()) {
                        String current = e.nextElement();
                        log.debug("excludes: Specific: " + current);
                        // try {
                            //m_excludes.add(InetAddressUtils.addr(current));
                            //JOHAN - The Scheduler expects a String
                            m_excludes.add(current);
                        //} catch (UnknownHostException uhE) {
                        //    ThreadCategory.getInstance().warn("Failed to convert address: " + current, uhE);
                        //}
                    }
                }
            }
        }
        return m_excludes;
    }

    /**
     * <p>removeExcludeRange</p>
     *
     * @param begin a {@link java.net.InetAddress} object.
     * @param end a {@link java.net.InetAddress} object.
     */
    public void removeExcludeRange(InetAddress begin, InetAddress end) {
        Range removeMe = new Range();
        removeMe.setBegin(InetAddressUtils.str(begin));
        removeMe.setEnd(InetAddressUtils.str(end));

        m_config.getExcludes().removeRange(removeMe);
    }

    /**
     * <p>removeExcludeSpecific</p>
     *
     * @param specific a {@link java.net.InetAddress} object.
     */
    public void removeExcludeSpecific(InetAddress specific) {
        m_config.getExcludes().removeSpecific(InetAddressUtils.str(specific));
    }

    /**
     * <p>getRescanFrequency</p>
     *
     * @return a long.
     */
    public long getRescanFrequency() {
        long frequency = -1;

        if (m_config.hasRescanFrequency())
            frequency = m_config.getRescanFrequency();
        else {
            ThreadCategory.getInstance(VulnscandConfigFactory.class).warn("Vulnscand configuration file is missing rescan interval, defaulting to 24 hour interval.");
            frequency = 86400000; // default is 24 hours
        }

        return frequency;
    }

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    public long getInitialSleepTime() {
        long sleep = -1;

        if (m_config.hasInitialSleepTime())
            sleep = m_config.getInitialSleepTime();
        else {
            ThreadCategory.getInstance(VulnscandConfigFactory.class).warn("Vulnscand configuration file is missing initial pause time, defaulting to 5 minutes.");
            sleep = 300000; // default is 5 minutes
        }

        return sleep;
    }

    /**
     * <p>getServerAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getServerAddress() {
        return (InetAddressUtils.addr(m_config.getServerAddress()));
    }

    /**
     * Gets the cached value of the plugin lists in the config file
     *
     * @return an array of {@link java.lang.String} objects.
     */
    public String[] getPluginLists() {
        if (m_pluginLists == null) {
            m_pluginLists = new String[5];

            // Dummy value
            m_pluginLists[0] = "";

            try {
                Enumeration<?> scanLevels = m_config.enumerateScanLevel();
                while (scanLevels.hasMoreElements()) {
                    ScanLevel scanLevel = (ScanLevel) scanLevels.nextElement();

                    String levelPluginList = scanLevel.getPluginList();

                    // Get rid of all of the carriage returns, tabs, and spaces
                    levelPluginList = levelPluginList.replace('\n', ' ');
                    levelPluginList = levelPluginList.replace('\t', ' ');
                    levelPluginList = m_space.subst(levelPluginList, "");

                    m_pluginLists[scanLevel.getLevel()] = levelPluginList;
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                ThreadCategory.getInstance(getClass()).error("Error while loading plugin lists from config file", ex);
            }
        }
        return m_pluginLists;
    }

    /**
     * Gets the cached value of the safe checks settings in the config file
     *
     * @return an array of boolean.
     */
    public boolean[] getSafeChecks() {
        if (m_safeChecks == null) {
            m_safeChecks = new boolean[5];

            // Dummy value
            m_safeChecks[0] = true;

            try {
                Enumeration<?> scanLevels = m_config.enumerateScanLevel();
                while (scanLevels.hasMoreElements()) {
                    ScanLevel scanLevel = (ScanLevel) scanLevels.nextElement();

                    m_safeChecks[scanLevel.getLevel()] = scanLevel.getSafeChecks();
                }
            } catch (ArrayIndexOutOfBoundsException ex) {
                ThreadCategory.getInstance(getClass()).error("Error while loading safe checks settings from config file", ex);
            }
        }
        return m_safeChecks;
    }

    /**
     * <p>getServerPort</p>
     *
     * @return a int.
     */
    public int getServerPort() {
        return m_config.getServerPort();
    }

    /**
     * <p>getServerUsername</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServerUsername() {
        return m_config.getServerUsername();
    }

    /**
     * <p>getServerPassword</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServerPassword() {
        return m_config.getServerPassword();
    }

    /**
     * <p>getStatus</p>
     *
     * @return a boolean.
     */
    public boolean getStatus() {
        return m_config.getStatus();
    }

    /**
     * <p>getManagedInterfacesStatus</p>
     *
     * @return a boolean.
     */
    public boolean getManagedInterfacesStatus() {
        return m_config.getManagedInterfaces().getStatus();
    }

    /**
     * <p>getManagedInterfacesScanLevel</p>
     *
     * @return a int.
     */
    public int getManagedInterfacesScanLevel() {
        return m_config.getManagedInterfaces().getScanLevel();
    }

    /**
     * <p>getMaxSuspectThreadPoolSize</p>
     *
     * @return a int.
     */
    public int getMaxSuspectThreadPoolSize() {
        return m_config.getMaxSuspectThreadPoolSize();
    }

    /**
     * <p>getMaxRescanThreadPoolSize</p>
     *
     * @return a int.
     */
    public int getMaxRescanThreadPoolSize() {
        return m_config.getMaxRescanThreadPoolSize();
    }
}
