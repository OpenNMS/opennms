/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified 
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 *
 * 2007 May 06: Moved plugin management and database synchronization
 *              out of this class.  Use Java 5 generics, eliminate
 *              warnings, and do lots of code formatting. - dj@opennms.org
 * 2007 Apr 20: Throw a ValidationException if we can't instantiate a plugin. - dj@opennms.org
 *
 * Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.config;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.capsd.CapsdConfiguration;
import org.opennms.netmgt.config.capsd.IpManagement;
import org.opennms.netmgt.config.capsd.Property;
import org.opennms.netmgt.config.capsd.ProtocolConfiguration;
import org.opennms.netmgt.config.capsd.ProtocolPlugin;
import org.opennms.netmgt.config.capsd.SmbAuth;
import org.opennms.netmgt.config.capsd.SmbConfig;
import org.opennms.netmgt.config.common.Range;
import org.opennms.netmgt.dao.castor.CastorUtils;

public abstract class CapsdConfigManager implements CapsdConfig {
    /**
     * The string indicating the start of the comments in a line containing the
     * IP address in a file URL
     */
    private static final String COMMENT_STR = " #";
    
    /**
     * This character at the start of a line indicates a comment line in a URL
     * file
     */
    private static final char COMMENT_CHAR = '#';

//
//    /**
//     * The SQL statement used to mark all ifservices table entries which refer
//     * to the specified serviceId as deleted.
//     */
//    private static final String DELETE_IFSERVICES_SQL = 
//            "update ifservices " + 
//            "   set status = 'D' " + 
//            " where serviceid = ?" +
//            "   and id in (" + 
//            "   select svc.id" + 
//            "     from ifservices as svc" + 
//            "     join ipinterface as ip" + 
//            "       on (ip.id = svc.ipinterfaceid)" + 
//            "     join node as n" + 
//            "       on (n.nodeid = ip.nodeid)" + 
//            "    where n.foreignsource is null)"; 

    /**
     * The config class loaded from the config file
     */
    private CapsdConfiguration m_config;
    
    /**
     * Maps url names to a list of addresses specified by the url. Initialized
     * at factory construction.
     */
    private Map<String, List<String>> m_urlMap;

    public CapsdConfigManager() {
    }

    @Deprecated
    public CapsdConfigManager(Reader rdr) throws MarshalException, ValidationException {
        loadXml(rdr);
    }

    public CapsdConfigManager(InputStream is) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(CapsdConfiguration.class, is);
    }

    protected abstract void saveXml(String xml) throws IOException;

    protected abstract void update() throws IOException, FileNotFoundException, MarshalException, ValidationException;

    protected void loadXml(InputStream is) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(CapsdConfiguration.class, is);
        loadIncludeUrls();
    }

    @SuppressWarnings("deprecation")
    protected void loadXml(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(CapsdConfiguration.class, rdr);
        loadIncludeUrls();
    }

    /**
     * Load addresses from any urls which have been specified.
     */
    private void loadIncludeUrls() {
        m_urlMap = new HashMap<String, List<String>>();

        for (IpManagement mgt : getIpManagements()) {
            for (String url : getIncludeUrls(mgt)) {
                if (!m_urlMap.containsKey(url)) {
                    m_urlMap.put(url, getAddressesFromURL(url));
                }
            }
        }
    }

    /**
     * Saves the current in-memory configuration to disk and reloads
     */
    public synchronized void save() throws MarshalException, IOException, ValidationException {
        log().debug("Saving capsd configuration");
        
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(m_config, stringWriter);
        saveXml(stringWriter.toString());
    
        log().info("Saved capsd configuration");

        update();
    }

    /**
     * Return the Capsd configuration object.
     */
    public CapsdConfiguration getConfiguration() {
        return m_config;
    }

    public ProtocolPlugin getProtocolPlugin(String svcName) {
        for (ProtocolPlugin plugin : getProtocolPlugins()) {
            if (plugin.getProtocol().equals(svcName)) {
                return plugin;
            }
        }
        return null;
    }
    
    public void addProtocolPlugin(ProtocolPlugin plugin) {
        m_config.addProtocolPlugin(plugin);
    }

    /**
     * Finds the SMB authentication object using the netbios name.
     * 
     * The target of the search.
     */
    public SmbAuth getSmbAuth(String target) {
        SmbConfig cfg = m_config.getSmbConfig();
        if (cfg == null) {
            return null;
        }
        
        List<SmbAuth> smbAuths = getSmbAuths(cfg);
        for (SmbAuth a : smbAuths) {
            if (target.equalsIgnoreCase(a.getContent())) {
                return a;
            }
        }
        
        return null;
    }

    /**
     * Checks the configuration to determine if the target is managed or
     * unmanaged.
     * 
     * @param target
     *            The target to check against.
     */
    public boolean isAddressUnmanaged(InetAddress target) {
        String managementPolicy = m_config.getManagementPolicy();
        boolean managedByDefault = (managementPolicy == null || managementPolicy.equalsIgnoreCase("managed"));
    
        boolean found_denial = false;
        boolean found_accept = false;
        List<IpManagement> ipManagements = getIpManagements();
        Iterator<IpManagement> ipIter = ipManagements.iterator();
        while (ipIter.hasNext() && !found_denial) {
            IpManagement mgt = ipIter.next();
            for (String saddr : getSpecifics(mgt)) {
                InetAddress addr;
                try {
                    addr = InetAddress.getByName(saddr);
                } catch (UnknownHostException e) {
                    log().info("Failed to convert specific address '" + saddr + "' to an InetAddress: " + e, e);
                    continue;
                }
                
                if (addr.equals(target)) {
                    if (mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed")) {
                        found_accept = true;
                    } else {
                        found_denial = true;
                    }

                    break;
                }
            }
    
            // now check the ranges
            List<Range> ranges = getRanges(mgt);
            Iterator<Range> rangeIter = ranges.iterator();
            while (!found_denial && rangeIter.hasNext()) {
                Range range = rangeIter.next();

                InetAddress saddr;
                try {
                    saddr = InetAddress.getByName(range.getBegin());
                } catch (UnknownHostException e) {
                    log().info("Failed to convert begin address '" + range.getBegin() + "' to an InetAddress: " + e, e);
                    continue;
                }

                InetAddress eaddr;
                try {
                    eaddr = InetAddress.getByName(range.getEnd());
                } catch (UnknownHostException e) {
                    log().info("Failed to convert end address '" + range.getEnd() + "' to an InetAddress: " + e, e);
                    continue;
                }

                long start = toLong(saddr);
                long stop = toLong(eaddr);
                long tgt = toLong(target);
                if (start <= tgt && tgt <= stop) {
                    if (mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed")) {
                        found_accept = true;
                    } else {
                        found_denial = true;
                    }

                    break;
                }
            }
    
            // now check urls
            List<String> includeUrls = getIncludeUrls(mgt);
            Iterator<String> includeUrlIter = includeUrls.iterator();
            boolean match = false;
            while (!found_denial && !match && includeUrlIter.hasNext()) {
                String url = includeUrlIter.next();
    
                /*
                 * Retrieve address list from url map.
                 * Iterate over addresses looking for target match.
                 */
                for (String saddr : m_urlMap.get(url)) {
                    InetAddress addr;
                    try {
                        addr = InetAddress.getByName(saddr);
                    } catch (UnknownHostException e) {
                        log().info("Failed to convert address '" + saddr + "' from include URL '" + url + "' to an InetAddress: " + e, e);
                        continue;
                    }

                    if (addr.equals(target)) {
                        if (mgt.getPolicy() == null || mgt.getPolicy().equalsIgnoreCase("managed")) {
                            found_accept = true;
                        } else {
                            found_denial = true;
                        }

                        match = true;
                        break;
                    }
                }
            }
    
        }
    
        boolean result = !managedByDefault;
        if (found_denial) {
            result = true;
        } else if (found_accept) {
            result = false;
        }
    
        return result;
    }

    /**
     * The file URL is read and a 'specific IP' is added for each entry in this
     * file. Each line in the URL file can be one of -<IP><space># <comments>
     * or <IP>or #<comments>
     * 
     * Lines starting with a '#' are ignored and so are characters after a '
     * <space>#' in a line.
     * 
     * @param url
     *            the URL file
     * 
     * @return List of addresses retrieved from the URL
     */
    private List<String> getAddressesFromURL(String url) {
        List<String> addrList = new ArrayList<String>();
    
        try {
            // open the file indicated by the url
            URL fileURL = new URL(url);
    
            InputStream file = fileURL.openStream();
    
            // check to see if the file exists
            if (file != null) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(file, "UTF-8"));
    
                String ipLine = null;
                String specIP = null;
    
                // get each line of the file and turn it into a specific address
                while ((ipLine = buffer.readLine()) != null) {
                    ipLine = ipLine.trim();
                    if (ipLine.length() == 0 || ipLine.charAt(0) == COMMENT_CHAR) {
                        // blank line or skip comment
                        continue;
                    }
    
                    // check for comments after IP
                    int comIndex = ipLine.indexOf(COMMENT_STR);
                    if (comIndex == -1) {
                        specIP = ipLine;
                    } else {
                        specIP = ipLine.substring(0, comIndex);
                        ipLine = ipLine.trim();
                    }
    
                    addrList.add(specIP);
    
                    specIP = null;
                }
    
                buffer.close();
            } else {
                log().warn("URL does not exist: " + url.toString());
            }
        } catch (MalformedURLException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        } catch (FileNotFoundException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        } catch (IOException e) {
            log().error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
        }
    
        return addrList;
    }

    /**
     * 
     */
    public long getRescanFrequency() {
        long frequency = -1;
    
        if (m_config.hasRescanFrequency()) {
            frequency = m_config.getRescanFrequency();
        } else {
            log().warn("Capsd configuration file is missing rescan interval, defaulting to 24 hour interval.");
            frequency = 86400000; // default is 24 hours
        }
    
        return frequency;
    }

    /**
     * 
     */
    public long getInitialSleepTime() {
        long sleep = -1;
    
        if (m_config.hasInitialSleepTime()) {
            sleep = m_config.getInitialSleepTime();
        } else {
            log().warn("Capsd configuration file is missing rescan interval, defaulting to 24 hour interval.");
            sleep = 300000; // default is 5 minutes
        }
    
        return sleep;
    }

    /**
     * 
     */
    public int getMaxSuspectThreadPoolSize() {
        return m_config.getMaxSuspectThreadPoolSize();
    }

    /**
     * 
     */
    public int getMaxRescanThreadPoolSize() {
        return m_config.getMaxRescanThreadPoolSize();
    }

    /**
     * Defines Capsd's behavior when, during a protocol scan, it gets a
     * java.net.NoRouteToHostException exception. If abort rescan property is
     * set to "true" then Capsd will not perform any additional protocol scans.
     */
    public boolean getAbortProtocolScansFlag() {
        boolean abortFlag = false;
    
        String abortProperty = m_config.getAbortProtocolScansIfNoRoute();
        if (abortProperty != null && abortProperty.equals("true")) {
            abortFlag = true;
        }
    
        return abortFlag;
    }

    public boolean getDeletePropagationEnabled() {
        boolean propagationEnabled = true;
        
       String propagationProperty = m_config.getDeletePropagationEnabled();
       if (propagationProperty != null && propagationProperty.equals("false")) {
           propagationEnabled = false;
       }
    
       return propagationEnabled;
    }

    /**
     * Return the boolean xmlrpc as string to indicate if notification to
     * external xmlrpc server is needed.
     * 
     * @return boolean flag as a string value
     */
    public String getXmlrpc() {
        return m_config.getXmlrpc();
    }
    
    public boolean isXmlRpcEnabled() {
        return "true".equalsIgnoreCase(getXmlrpc());
    }

    /**
	 * Utility method which compares two InetAddress objects based on the
	 * provided method (MIN/MAX) and returns the InetAddress which is to be
	 * considered the primary interface.
	 * 
	 * NOTE: In order for an interface to be considered primary, if strict is
	 * true, it must be included by a Collectd package which supports the
	 * specified service. This method will return null if the 'oldPrimary'
	 * address is null and the 'currentIf' address does not pass the Collectd
	 * package check, if strict is true..
     * @param svcName
	 *            Service name
     * @param currentIf
	 *            Interface with which to compare the 'oldPrimary' address.
     * @param oldPrimary
	 *            Primary interface to be compared against the 'currentIf'
	 *            address.
     * @param method
	 *            Comparison method to be used (either "min" or "max")
     * @param strict
	 *            require interface to be part of a Collectd package
	 * 
	 * @return InetAddress object of the primary interface based on the provided
	 *         method or null if neither address is eligible to be primary.
	 */
	private InetAddress compareAndSelectPrimaryCollectionInterface(String svcName, InetAddress currentIf, InetAddress oldPrimary, String method, boolean strict) {
		InetAddress newPrimary = null;
		CollectdConfigFactory factory = CollectdConfigFactory.getInstance();
	
		if (oldPrimary == null && strict) {
			if (factory.isServiceCollectionEnabled(currentIf.getHostAddress(), svcName)) {
				return currentIf;
            } else {
				return oldPrimary;
            }
		}
	
		if (oldPrimary == null) {
			return currentIf;
        }
	
		long current = IPSorter.convertToLong(currentIf.getAddress());
		long primary = IPSorter.convertToLong(oldPrimary.getAddress());
	
		if (method.equals(CollectdConfigFactory.SELECT_METHOD_MIN)) {
			// Smallest address wins
			if (current < primary) {
                /*
				 * Replace the primary interface with the current
				 * interface only if the current interface is managed!
                 */
				if (strict) {
					if (factory.isServiceCollectionEnabled(currentIf.getHostAddress(),
							svcName)) {
						newPrimary = currentIf;
                    }
				} else {
					newPrimary = currentIf;
				}
			}
		} else {
			// Largest address wins
			if (current > primary) {
                /*
				 * Replace the primary interface with the current
				 * interface only if the current interface is managed!
                 */
				if (strict) {
					if (factory.isServiceCollectionEnabled(currentIf.getHostAddress(),
							svcName)) {
						newPrimary = currentIf;
                    }
				} else {
					newPrimary = currentIf;
				}
			}
		}
	
		if (newPrimary != null) {
			return newPrimary;
        } else {
			return oldPrimary;
        }
	}

	/**
	 * This method is responsbile for determining the node's primary SNMP
	 * interface from the specified list of InetAddress objects.
	 * @param addressList
	 *            List of InetAddress objects representing all the interfaces
	 *            belonging to a particular node which support the "SNMP"
	 *            service and have a valid ifIndex.
	 * @param strict
	 *            Boolean variable which requires an interface to be part of a
	 *            Collectd package to be eligible as a primary SNMP interface
	 * 
	 * @return InetAddress object of the primary SNMP interface or null if none
	 *         of the node's interfaces are eligible.
	 */
	public InetAddress determinePrimarySnmpInterface(List<InetAddress> addressList, boolean strict) {
		InetAddress primaryIf = null;
	
		// For now hard-coding primary interface address selection method to MIN
		String method = CollectdConfigFactory.SELECT_METHOD_MIN;
	
        /*
		 * To be selected as the the primary SNMP interface for a node
		 * the interface must be included by a Collectd package if strict
		 * is true, and that package must include the SNMP service and
		 * the service must be enabled.
		 *
		 * Iterate over interface list and test each interface
		 */
		for (InetAddress ipAddr : addressList) {
			if (log().isDebugEnabled()) {
				log().debug("determinePrimarySnmpIf: checking interface "
						+ ipAddr.getHostAddress());
            }
			primaryIf = compareAndSelectPrimaryCollectionInterface("SNMP", ipAddr, primaryIf, method, strict);
		}
	
		if (log().isDebugEnabled()) {
			if (primaryIf != null) {
				log().debug("determinePrimarySnmpInterface: candidate primary SNMP interface: "
								+ primaryIf.getHostAddress());
            } else {
				log().debug("determinePrimarySnmpInterface: no candidate primary SNMP interface found");
            }
		}
		return primaryIf;
	}

	/**
     * This method is used to convert the passed IP address to a
     * <code>long</code> value. The address is converted in network byte order
     * (big endin). This is compatable with the number format of the JVM, and
     * thus the return longs can be compared with other converted IP Addresses
     * to determine inclusion.
     * 
     * @param addr
     *            The IP address to convert.
     * 
     * @return The converted IP address.
     * 
     * @deprecated Use org.opennms.netmgt.utils.IPSorter.convertToLong()
     *             instead.
     */
    public long toLong(InetAddress addr) {
        byte[] baddr = addr.getAddress();
    
        return ((((long) baddr[0] & 0xffL) << 24) | (((long) baddr[1] & 0xffL) << 16) | (((long) baddr[2] & 0xffL) << 8) | ((long) baddr[3] & 0xffL));
    }

    /**
     * Return a list of configured protocols from the loaded configuration.
     */
    public List<String> getConfiguredProtocols() {
        List<String> protocols = new ArrayList<String>();
        for (ProtocolPlugin plugin : getProtocolPlugins()) {
            protocols.add(plugin.getProtocol());
        }
        return protocols;
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    public List<ProtocolPlugin> getProtocolPlugins() {
        return m_config.getProtocolPluginCollection();
    }

    private List<IpManagement> getIpManagements() {
        return m_config.getIpManagementCollection();
    }

    private List<Range> getRanges(IpManagement mgt) {
        return mgt.getRangeCollection();
    }

    private List<String> getSpecifics(IpManagement mgt) {
        return mgt.getSpecificCollection();
    }

    private List<String> getIncludeUrls(IpManagement mgt) {
        return mgt.getIncludeUrlCollection();
    }

    public List<ProtocolConfiguration> getProtocolConfigurations(ProtocolPlugin plugin) {
        return plugin.getProtocolConfigurationCollection();
    }

    public List<Property> getPluginProperties(ProtocolPlugin plugin) {
        return plugin.getPropertyCollection();
    }

    public List<Property> getProtocolConfigurationProperties(ProtocolConfiguration pluginConf) {
        return pluginConf.getPropertyCollection();
    }

    public List<Range> getRanges(ProtocolConfiguration pluginConf) {
        return pluginConf.getRangeCollection();
    }
    
    public List<String> getSpecifics(ProtocolConfiguration pluginConf) {
        return pluginConf.getSpecificCollection();
    }

    private List<SmbAuth> getSmbAuths(SmbConfig cfg) {
        return cfg.getSmbAuthCollection();
    }
}
