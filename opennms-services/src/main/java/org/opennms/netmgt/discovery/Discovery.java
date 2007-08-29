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
// Modifications:
//
// 2003 Oct 21: Fixed typo in variable name.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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

package org.opennms.netmgt.discovery;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Category;
import org.opennms.core.utils.IPSorter;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.DataSourceFactory;
import org.opennms.netmgt.config.DiscoveryConfigFactory;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.ping.PingResponseCallback;
import org.opennms.netmgt.ping.Pinger;
import org.opennms.netmgt.xml.event.Event;

/**
 * This class is the main interface to the OpenNMS discovery service. The class
 * implements the <em>singleton</em> design pattern, in that there is only one
 * instance in any given virtual machine. The service delays the reading of
 * configuration information until the service is started.
 * 
 * @author <a href="mailto:weave@oculan.com">Brian Weaver </a>
 * @author <a href="http://www.opennms.org/">OpenNMS.org </a>
 * 
 */
public final class Discovery extends AbstractServiceDaemon implements EventListener {

    /**
     * The string indicating the start of the comments in a line containing the
     * IP address in a file URL
     */
    private final static String COMMENT_STR = " #";

    /**
     * This character at the start of a line indicates a comment line in a URL
     * file
     */
    private final static char COMMENT_CHAR = '#';

    /**
     * The singular instance of the discovery service.
     */
    private static final Discovery m_singleton = new Discovery();

    /**
     * a set of devices to skip discovery on
     */
    private Set<String> m_alreadyDiscovered;
    private Collection<ExcludeRange> m_excluded;

    private DiscoveryConfigFactory m_discoveryFactory;
    private DiscoveryConfiguration m_discoveryConfiguration;
    private Timer m_timer;
    private long initialSleepTime;
    private long discoveryInterval;

    /**
     * Constructs a new discovery instance.
     */
    private Discovery() {
        super("OpenNMS.Discovery");
    }

    protected void onInit() {

        try {
            DataSourceFactory.init();
        } catch (Exception e) {
            log().fatal("Unable to initialize the database factory", e);
            throw new UndeclaredThrowableException(e);
        }

        reloadConfiguration();
        initialSleepTime = m_discoveryConfiguration.getInitialSleepTime();
        discoveryInterval = m_discoveryConfiguration.getRestartSleepTime();

        EventIpcManagerFactory.init();

        List<String> ueiList = new ArrayList<String>();
        ueiList.add(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI);
        ueiList.add(EventConstants.DISC_PAUSE_EVENT_UEI);
        ueiList.add(EventConstants.DISC_RESUME_EVENT_UEI);
        ueiList.add(EventConstants.INTERFACE_DELETED_EVENT_UEI);
        ueiList.add(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI);

        EventIpcManagerFactory.getIpcManager().addEventListener(this, ueiList);
    }

    private void reloadConfiguration() {
        try {
            DiscoveryConfigFactory.reload();
            m_discoveryFactory = DiscoveryConfigFactory.getInstance();
            DiscoveryConfiguration config = m_discoveryFactory.getConfiguration();
            m_discoveryConfiguration = config;

            m_excluded = Collections.synchronizedCollection(config.getExcludeRangeCollection());
            m_alreadyDiscovered = Collections.synchronizedSet(new HashSet<String>());

        } catch (Exception e) {
            log().fatal("Unable to initialize the discovery configuration factory", e);
            throw new UndeclaredThrowableException(e);
        }
    }

    protected void doPings() {
        reloadConfiguration();

        DiscoveryPingResponseCallback cb = new DiscoveryPingResponseCallback();

        int pps = m_discoveryConfiguration.getPacketsPerSecond();

        List<IPPollAddress> specifics = getSpecifics();
        specifics.addAll(getUrlSpecifics());
        List<IPPollRange> ranges = getRanges();

        for (IPPollAddress address : specifics) {
            pingAddress(address.getAddress(), address.getTimeout(), address.getRetries(), cb);
        }

        for (IPPollRange range : ranges) {
            for (InetAddress address : range.getAddressRange()) {
                // only check isExcluded for ranges since specifics would
                // override
                if (!isExcluded(address)) {
                    pingAddress(address, range.getTimeout(), range.getRetries(), cb);
                    try {
                        Thread.sleep(1000 / pps);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
        }
    }

    private boolean isExcluded(InetAddress address) {
        if (m_excluded != null) {
            long laddr = IPSorter.convertToLong(address.getAddress());

            for (ExcludeRange range : m_excluded) {
                try {
                    long begin = IPSorter.convertToLong(InetAddress.getByName(range.getBegin()).getAddress());
                    long end = IPSorter.convertToLong(InetAddress.getByName(range.getEnd()).getAddress());
                    if (begin <= laddr && laddr <= end) {
                        return true;
                    }
                } catch (UnknownHostException ex) {
                    log().debug("isExcluded: failed to convert exclusion address to InetAddress", ex);
                }

            }
        }
        return false;
    }

    private boolean isAlreadyDiscovered(InetAddress address) {
        if (m_alreadyDiscovered.contains(address.getHostAddress())) {
            return true;
        }
        return false;
    }

    protected void pingAddress(InetAddress address, long timeout, int retries, PingResponseCallback cb) {
        if (address != null) {
            if (!isAlreadyDiscovered(address)) {
                try {
                    Pinger.ping(address, timeout, retries, (short) 1, cb);
                } catch (IOException e) {
                    log().debug("error pinging " + address.getAddress(), e);
                }
            }
        }
    }

    /**
     * Returns the singular instance of the discovery process
     */
    public static Discovery getInstance() {
        return m_singleton;
    }

    private void startTimer() {
        if (m_timer != null) {
            m_timer.cancel();
        }
        m_timer = new Timer("Discovery.Pinger", true);

        TimerTask task = new TimerTask() {

            @Override
            public void run() {
                doPings();
            }

        };
        m_timer.scheduleAtFixedRate(task, initialSleepTime, discoveryInterval);

    }

    private void stopTimer() {
        if (m_timer != null) {
            m_timer.cancel();
            m_timer = null;
        }
    }

    protected void onStart() {
        startTimer();
    }

    protected void onStop() {
        stopTimer();
    }

    protected void onPause() {
        stopTimer();
    }

    protected void onResume() {
        startTimer();
    }

    private List<IPPollAddress> getUrlSpecifics() {
        List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();

        Enumeration<IncludeUrl> urlEntries = m_discoveryConfiguration.enumerateIncludeUrl();
        while (urlEntries.hasMoreElements()) {
            IncludeUrl url = urlEntries.nextElement();

            long timeout = 800L;
            if (url.hasTimeout()) {
                timeout = url.getTimeout();
            } else if (m_discoveryConfiguration.hasTimeout()) {
                timeout = m_discoveryConfiguration.getTimeout();
            }

            int retries = 3;
            if (url.hasRetries()) {
                retries = url.getRetries();
            } else if (m_discoveryConfiguration.hasRetries()) {
                retries = m_discoveryConfiguration.getRetries();
            }

            addToSpecificsFromURL(specifics, url.getContent(), timeout, retries);
        }

        return specifics;
    }

    private List<IPPollRange> getRanges() {
        List<IPPollRange> includes = new LinkedList<IPPollRange>();
        Enumeration<IncludeRange> includeRangeEntries = m_discoveryConfiguration.enumerateIncludeRange();
        while (includeRangeEntries.hasMoreElements()) {
            IncludeRange ir = includeRangeEntries.nextElement();

            long timeout = 800L;
            if (ir.hasTimeout()) {
                timeout = ir.getTimeout();
            } else if (m_discoveryConfiguration.hasTimeout()) {
                timeout = m_discoveryConfiguration.getTimeout();
            }

            int retries = 3;
            if (ir.hasRetries()) {
                retries = ir.getRetries();
            } else if (m_discoveryConfiguration.hasRetries()) {
                retries = m_discoveryConfiguration.getRetries();
            }

            try {
                includes.add(new IPPollRange(ir.getBegin(), ir.getEnd(), timeout, retries));
            } catch (UnknownHostException uhE) {
                log().warn("Failed to convert address range (" + ir.getBegin() + ", " + ir.getEnd() + ")", uhE);
            }
        }

        return includes;
    }

    private List<IPPollAddress> getSpecifics() {
        List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();

        Enumeration<Specific> specificEntries = m_discoveryConfiguration.enumerateSpecific();
        while (specificEntries.hasMoreElements()) {
            Specific s = specificEntries.nextElement();

            long timeout = 800L;
            if (s.hasTimeout()) {
                timeout = s.getTimeout();
            } else if (m_discoveryConfiguration.hasTimeout()) {
                timeout = m_discoveryConfiguration.getTimeout();
            }

            int retries = 3;
            if (s.hasRetries()) {
                retries = s.getRetries();
            } else if (m_discoveryConfiguration.hasRetries()) {
                retries = m_discoveryConfiguration.getRetries();
            }

            try {
                specifics.add(new IPPollAddress(s.getContent(), timeout, retries));
            } catch (UnknownHostException uhE) {
                log().warn("Failed to convert address " + s.getContent(), uhE);
            }
        }
        return specifics;
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
    private boolean addToSpecificsFromURL(List<IPPollAddress> specifics, String url, long timeout, int retries) {
        Category log = ThreadCategory.getInstance();

        boolean bRet = true;

        try {
            // open the file indicated by the URL
            URL fileURL = new URL(url);

            InputStream is = fileURL.openStream();

            // check to see if the file exists
            if (is != null) {
                BufferedReader buffer = new BufferedReader(new InputStreamReader(is));

                String ipLine = null;
                String specIP = null;

                // get each line of the file and turn it into a specific range
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

    public void onEvent(Event event) {
        Category log = ThreadCategory.getInstance(getClass());

        String eventUei = event.getUei();
        if (eventUei == null)
            return;

        if (log.isDebugEnabled())
            log.debug("Received event: " + eventUei);

        if (eventUei.equals(EventConstants.NODE_GAINED_INTERFACE_EVENT_UEI)) {
            // add to known nodes
            m_alreadyDiscovered.add(event.getInterface());

            if (log.isDebugEnabled())
                log.debug("Added " + event.getInterface() + " as discovered");
        } else if (eventUei.equals(EventConstants.DISC_PAUSE_EVENT_UEI)) {
            try {
                Discovery.getInstance().pause();
            } catch (IllegalStateException ex) {
            }
        } else if (eventUei.equals(EventConstants.DISC_RESUME_EVENT_UEI)) {
            try {
                Discovery.getInstance().resume();
            } catch (IllegalStateException ex) {
            }
        } else if (eventUei.equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
            // remove from known nodes
            m_alreadyDiscovered.remove(event.getInterface());

            if (log.isDebugEnabled())
                log.debug("Removed " + event.getInterface() + " from known node list");
        } else if (eventUei.equals(EventConstants.DISCOVERYCONFIG_CHANGED_EVENT_UEI)) {

        }
    }

}
