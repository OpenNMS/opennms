//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.FilteringIterator;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.IteratorIterator;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;
import org.springframework.core.io.FileSystemResource;

/**
 * This is the singleton class used to load the configuration for the OpenNMS
 * Discovery service from the discovery-configuration xml file.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 */
public final class DiscoveryConfigFactory {
    public static final String COMMENT_STR = " #";

    public static final char COMMENT_CHAR = '#';

    /**
     * The singleton instance of this factory
     */
    private static DiscoveryConfigFactory m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private DiscoveryConfiguration m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private DiscoveryConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DiscoveryConfiguration.class, new FileSystemResource(configFile));
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
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DISCOVERY_CONFIG_FILE_NAME);

        ThreadCategory.getInstance(DiscoveryConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new DiscoveryConfigFactory(cfgFile.getPath());

        try {
            m_singleton.getInitialSleepTime();
            m_singleton.getRestartSleepTime();
            m_singleton.getIntraPacketDelay();
            m_singleton.getConfiguredAddresses();
        } catch (Exception e) {
            throw new ValidationException("An error occurred while validating the configuration: " + e, e);
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
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DiscoveryConfigFactory getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * Return the discovery configuration object.
     */
    public synchronized DiscoveryConfiguration getConfiguration() {
        return m_config;
    }
    
    /**
     * @param xml
     * @throws IOException
     */
    protected void saveXml(String xml) throws IOException {
        if (xml != null) {
            Writer fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.DISCOVERY_CONFIG_FILE_NAME)), "UTF-8");
            fileWriter.write(xml);
            fileWriter.flush();
            fileWriter.close();
        }
    }
    public synchronized void saveConfiguration(DiscoveryConfiguration configuration) throws MarshalException, ValidationException, IOException {
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(configuration, stringWriter);
        String xml = stringWriter.toString();
        ThreadCategory.getInstance(DiscoveryConfigFactory.class).debug("saving configuration... \n");
        saveXml(xml);
    }

    /**
     * <pre>
     * The file URL is read and a 'specific IP' is added for each entry
     *  in this file. Each line in the URL file can be one of -
     *  &lt;IP&gt;&lt;space&gt;#&lt;comments&gt;
     *  or
     *  &lt;IP&gt;
     *  or
     *  #&lt;comments&gt;
     * 
     *  Lines starting with a '#' are ignored and so are characters after
     *  a '&lt;space&gt;#' in a line.
     * </pre>
     * 
     * @param specifics
     *            the list to add to
     * @param url
     *            the URL file
     * @param timeout
     *            the timeout for all entries in this URL
     * @param retries
     *            the retries for all entries in this URL
     */
    public boolean addToSpecificsFromURL(List<IPPollAddress> specifics, String url, long timeout, int retries) {
        Category log = ThreadCategory.getInstance();
    
        boolean bRet = true;
    
        try {
            // open the file indicated by the URL
            URL fileURL = new URL(url);
    
            InputStream is = fileURL.openStream();
    
            // check to see if the file exists
            if (is != null) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(is, "UTF-8"));
    
                String ipLine = null;
                String specIP = null;
    
                // get each line of the file and turn it into a specific range
                while ((ipLine = buffer.readLine()) != null) {
                    ipLine = ipLine.trim();
                    if (ipLine.length() == 0 || ipLine.charAt(0) == DiscoveryConfigFactory.COMMENT_CHAR) {
                        // blank line or skip comment
                        continue;
                    }
    
                    // check for comments after IP
                    int comIndex = ipLine.indexOf(DiscoveryConfigFactory.COMMENT_STR);
                    if (comIndex == -1) {
                        specIP = ipLine;
                    } else {
                        specIP = ipLine.substring(0, comIndex);
                        ipLine = ipLine.trim();
                    }
    
                    try {
                        specifics.add(new IPPollAddress(specIP, timeout, retries));
                    } catch (UnknownHostException e) {
                        log.warn("Unknown host \'" + specIP + "\' read from URL \'" + url.toString() + "\': address ignored");
                    }
    
                    specIP = null;
                }
    
                buffer.close();
            } else {
                // log something
                log.warn("URL does not exist: " + url.toString());
                bRet = true;
            }
        } catch (MalformedURLException e) {
            log.error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
            bRet = false;
        } catch (FileNotFoundException e) {
            log.error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
            bRet = false;
        } catch (IOException e) {
            log.error("Error reading URL: " + url.toString() + ": " + e.getLocalizedMessage());
            bRet = false;
        }
    
        return bRet;
    }

    public List<IPPollAddress> getURLSpecifics() {
        List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();
    
        Enumeration<IncludeUrl> urlEntries = getConfiguration().enumerateIncludeUrl();
        while (urlEntries.hasMoreElements()) {
            IncludeUrl url = urlEntries.nextElement();
    
            long timeout = 800L;
            if (url.hasTimeout()) {
                timeout = url.getTimeout();
            } else if (getConfiguration().hasTimeout()) {
                timeout = getConfiguration().getTimeout();
            }
    
            int retries = 3;
            if (url.hasRetries()) {
                retries = url.getRetries();
            } else if (getConfiguration().hasRetries()) {
                retries = getConfiguration().getRetries();
            }
    
            addToSpecificsFromURL(specifics, url.getContent(), timeout, retries);
        }
    
        return specifics;
    }

    public List<IPPollRange> getRanges() {
        List<IPPollRange> includes = new LinkedList<IPPollRange>();
        Enumeration<IncludeRange> includeRangeEntries = getConfiguration().enumerateIncludeRange();
        while (includeRangeEntries.hasMoreElements()) {
            IncludeRange ir = includeRangeEntries.nextElement();
    
            long timeout = 800L;
            if (ir.hasTimeout()) {
                timeout = ir.getTimeout();
            } else if (getConfiguration().hasTimeout()) {
                timeout = getConfiguration().getTimeout();
            }
    
            int retries = 3;
            if (ir.hasRetries()) {
                retries = ir.getRetries();
            } else if (getConfiguration().hasRetries()) {
                retries = getConfiguration().getRetries();
            }
    
            try {
                includes.add(new IPPollRange(ir.getBegin(), ir.getEnd(), timeout, retries));
            } catch (UnknownHostException uhE) {
                ThreadCategory.getInstance(getClass()).warn("Failed to convert address range (" + ir.getBegin() + ", " + ir.getEnd() + ")", uhE);
            }
        }
    
        return includes;
    }

    public List<IPPollAddress> getSpecifics() {
        List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();
    
        Enumeration<Specific> specificEntries = getConfiguration().enumerateSpecific();
        while (specificEntries.hasMoreElements()) {
            Specific s = specificEntries.nextElement();
    
            long timeout = 800L;
            if (s.hasTimeout()) {
                timeout = s.getTimeout();
            } else if (getConfiguration().hasTimeout()) {
                timeout = getConfiguration().getTimeout();
            }
    
            int retries = 3;
            if (s.hasRetries()) {
                retries = s.getRetries();
            } else if (getConfiguration().hasRetries()) {
                retries = getConfiguration().getRetries();
            }
    
            try {
                specifics.add(new IPPollAddress(s.getContent(), timeout, retries));
            } catch (UnknownHostException uhE) {
                ThreadCategory.getInstance(getClass()).warn("Failed to convert address " + s.getContent(), uhE);
            }
        }
        return specifics;
    }

    public boolean isExcluded(InetAddress address) {
        if (getConfiguration().getExcludeRangeCollection() != null) {
            long laddr = IPSorter.convertToLong(address.getAddress());
    
            for (ExcludeRange range : getConfiguration().getExcludeRangeCollection()) {
                try {
                    long begin = IPSorter.convertToLong(InetAddress.getByName(range.getBegin()).getAddress());
                    long end = IPSorter.convertToLong(InetAddress.getByName(range.getEnd()).getAddress());
                    if (begin <= laddr && laddr <= end) {
                        return true;
                    }
                } catch (UnknownHostException ex) {
                    ThreadCategory.getInstance(getClass()).debug("isExcluded: failed to convert exclusion address to InetAddress", ex);
                }
    
            }
        }
        return false;
    }

    public int getIntraPacketDelay() {
        return 1000 / getConfiguration().getPacketsPerSecond();
    }

    public Iterator<IPPollAddress> getExcludingInterator(
            Iterator<IPPollAddress> it) {
        return new FilteringIterator<IPPollAddress>(it) {
    
            @Override
            protected boolean matches(IPPollAddress item) {
                return !isExcluded(item.getAddress());
            }
            
        };
    }

    public Iterable<IPPollAddress> getConfiguredAddresses() {
        List<IPPollAddress> specifics = getSpecifics();
        specifics.addAll(getURLSpecifics());
        List<IPPollRange> ranges = getRanges();
    
        List<Iterator<IPPollAddress>> iters = new ArrayList<Iterator<IPPollAddress>>();
        iters.add(specifics.iterator());
        
        for(IPPollRange range : ranges) {
            iters.add(getExcludingInterator(range.iterator()));
        }
    
        Iterable<IPPollAddress> addrs = new IteratorIterator<IPPollAddress>(iters);
        return addrs;
    }

    public long getRestartSleepTime() {
        return getConfiguration().getRestartSleepTime();
    }

    public long getInitialSleepTime() {
        return getConfiguration().getInitialSleepTime();
    }
}
