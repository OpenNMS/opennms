/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Spliterators;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.StreamSupport;

import org.apache.commons.io.IOUtils;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.IteratorUtils;
import org.opennms.core.utils.LocationUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.api.DiscoveryConfigurationFactory;
import org.opennms.netmgt.config.discovery.Definition;
import org.opennms.netmgt.config.discovery.Detector;
import org.opennms.netmgt.config.discovery.DiscoveryConfiguration;
import org.opennms.netmgt.config.discovery.ExcludeRange;
import org.opennms.netmgt.config.discovery.IncludeRange;
import org.opennms.netmgt.config.discovery.IncludeUrl;
import org.opennms.netmgt.config.discovery.Specific;
import org.opennms.netmgt.model.discovery.IPPollAddress;
import org.opennms.netmgt.model.discovery.IPPollRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;

import com.google.common.base.Strings;

/**
 * This class is used to load the configuration for the OpenNMS
 * Discovery service from the discovery-configuration.xml file.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson </a>
 */
public class DiscoveryConfigFactory implements DiscoveryConfigurationFactory {
    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryConfigFactory.class);
    private final ReadWriteLock m_globalLock = new ReentrantReadWriteLock();
    private final Lock m_readLock = m_globalLock.readLock();
    private final Lock m_writeLock = m_globalLock.writeLock();
    private Map<String, List<String>> m_urlSpecifics = new HashMap<>();
    private List<ExcludeRange> m_excludeRanges = new ArrayList<>();

    public static final String COMMENT_STR = "#";
    public static final char COMMENT_CHAR = '#';

    /*
     * These values match the defaults in discovery-configuration.xml
     */
    public static final double DEFAULT_PACKETS_PER_SECOND = 1.0;
    public static final int DEFAULT_INITIAL_SLEEP_TIME = 30000;
    public static final int DEFAULT_RESTART_SLEEP_TIME = 86400000;
    public static final int DEFAULT_RETRIES = 1;
    public static final long DEFAULT_TIMEOUT = 2000;
    public static final int DEFAULT_CHUNK_SIZE = 100;

    /**
     * The config class loaded from the config file
     */
    private DiscoveryConfiguration m_config;

    /**
     * @deprecated Inject this value instead of using singleton access.
     */
    public static DiscoveryConfigFactory getInstance() {
        return BeanUtils.getBean("commonContext", "discoveryFactory", DiscoveryConfigFactory.class);
    }

    public DiscoveryConfigFactory() throws IOException {
        reload();
    }

    public DiscoveryConfigFactory (DiscoveryConfiguration config) {
        m_config = config;
    }

    public Lock getReadLock() {
        return m_readLock;
    }

    public Lock getWriteLock() {
        return m_writeLock;
    }

    /**
     * Reload the config from the default config file.
     * @throws IOException 
     *
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @throws java.io.IOException if any.
     */
    public synchronized void reload() throws IOException {
        try {
            File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DISCOVERY_CONFIG_FILE_NAME);
            LOG.debug("reload: config file path {}", cfgFile.getPath());
            final FileSystemResource resource = new FileSystemResource(cfgFile);
            m_config = JaxbUtils.unmarshal(DiscoveryConfiguration.class, resource);

            try {
                clearUrlSpecifics();
                clearExcludeRanges();
                getInitialSleepTime();
                getRestartSleepTime();
                getIntraPacketDelay();
                getConfiguredAddresses();
            } catch (final Throwable e) {
                throw new IOException("An error occurred while validating the configuration: " + e.getMessage(), e);
            }
        } catch (IOException e) {
            LOG.error("Could not unmarshal configuration file: " + ConfigFileConstants.getFileName(ConfigFileConstants.DISCOVERY_CONFIG_FILE_NAME), e);
            throw e;
        }
    }

    /**
     * Return the discovery configuration object.
     *
     * @return a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
     */
    @Override
    public synchronized DiscoveryConfiguration getConfiguration() {
        return m_config;
    }

    /**
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected void saveXml(final String xml) throws IOException {
        if (xml != null) {
            Writer fileWriter = null;
            getWriteLock().lock();
            try {
                fileWriter = new OutputStreamWriter(new FileOutputStream(ConfigFileConstants.getFile(ConfigFileConstants.DISCOVERY_CONFIG_FILE_NAME)), StandardCharsets.UTF_8);
                fileWriter.write(xml);
                fileWriter.flush();
            } finally {
                if (fileWriter != null ) IOUtils.closeQuietly(fileWriter);
                getWriteLock().unlock();
            }
        }
    }
    /**
     * <p>saveConfiguration</p>
     *
     * @param configuration a {@link org.opennms.netmgt.config.discovery.DiscoveryConfiguration} object.
     * @throws java.io.IOException if any.
     */
    public void saveConfiguration(final DiscoveryConfiguration configuration) throws IOException {
        getWriteLock().lock();
        try {
            // marshal to a string first, then write the string to the file. This
            // way the original config
            // isn't lost if the XML from the marshal is hosed.
            final StringWriter stringWriter = new StringWriter();
            JaxbUtils.marshal(configuration, stringWriter);
            final String xml = stringWriter.toString();
            LOG.debug("saving configuration...");
            saveXml(xml);
        } finally {
            getWriteLock().unlock();
        }
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
     * @return a list of IP Addresses retrieved from the URL.
     */
    public static List<String> addToSpecificsFromURL(final List<IPPollAddress> specifics, final String url, final String foreignSource, final String location, final long timeout, final int retries) {
        // open the file indicated by the URL
        InputStream is = null;
        List<String> ipAddressList = new ArrayList<>();
        try {
            final URL fileURL = new URL(url);
            is = fileURL.openStream();
            // check to see if the file exists
            if (is == null) {
                // log something
                LOG.warn("URL does not exist: {}", url);
                return ipAddressList;
            } else {
                return addToSpecificsFromURL(specifics, fileURL.openStream(), foreignSource, location, timeout, retries, url);
            }
        } catch (final IOException e) {
            LOG.error("Error reading URL: {}", url);
            return ipAddressList;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    /**
     * <p>addToSpecificsFromURL</p>
     *
     * @param specifics a {@link List} object.
     * @param is a {@link InputStream} object.
     * @param timeout a long.
     * @param retries a int.
     * @param url
     * @return a List of IP Addresses retrieved from URL.
     * @throws java.io.IOException if any.
     */
    public static List<String> addToSpecificsFromURL(final List<IPPollAddress> specifics, final InputStream is, final String foreignSource, final String location, final long timeout, final int retries, String url) throws IOException {
        boolean bRet = true;
        List<String> ipAddressList = new ArrayList<>();
        try {
            final BufferedReader buffer = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

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
                final int comIndex = ipLine.indexOf(DiscoveryConfigFactory.COMMENT_STR);
                if (comIndex == -1) {
                    specIP = ipLine;
                } else {
                    specIP = ipLine.substring(0, comIndex);
                    specIP = specIP.trim();
                }

                try {
                    InetAddress specific = InetAddressUtils.addr(specIP);
                    specifics.add(new IPPollAddress(foreignSource, location, specific, timeout, retries));
                    String ipAddress = InetAddressUtils.toIpAddrString(specific);
                    ipAddressList.add(ipAddress);
                } catch (final IllegalArgumentException e) {
                    LOG.warn("Unknown host \'{}\' inside discovery include file: address ignored", specIP);
                }

                specIP = null;
            }
        } catch (final UnsupportedEncodingException e) {
            LOG.error("Your JVM doesn't support UTF-8");
        }
        return ipAddressList;
    }

    /**
     * <p>getURLSpecifics</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<IPPollAddress> getURLSpecifics() {
        final List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();

        getReadLock().lock();
        try {
            Long defaultTimeout = getConfiguration().getTimeout().orElse(DEFAULT_TIMEOUT);
            Integer defaultRetries = getConfiguration().getRetries().orElse(DEFAULT_RETRIES);

            for (final IncludeUrl url : getConfiguration().getIncludeUrls()) {

                long timeout = url.getTimeout().orElse(defaultTimeout);
                int retries = url.getRetries().orElse(defaultRetries);
                if(url.getUrl().isPresent()) {
                    String urlString = url.getUrl().get();
                    List<String> ipAddressList = addToSpecificsFromURL(specifics, urlString, url.getForeignSource().orElse(null), url.getLocation().orElse(null), timeout, retries);
                    m_urlSpecifics.put(urlString, ipAddressList);
                }
            }

            // Add URL specifics from definitions to the specifics list.
            getConfiguration().getDefinitions().forEach(def -> {

                long timeoutFromDef = def.getTimeout().orElse(defaultTimeout);
                int retriesFromDef = def.getRetries().orElse(defaultRetries);

                for (final IncludeUrl url : def.getIncludeUrls()) {

                    long timeout = url.getTimeout().orElse(timeoutFromDef);
                    int retries = url.getRetries().orElse(retriesFromDef);
                    if (url.getUrl().isPresent()) {
                        String urlString = url.getUrl().get();
                        List<String> ipAddressList = addToSpecificsFromURL(specifics,
                                                urlString,
                                                url.getForeignSource().orElse(def.getForeignSource()),
                                                url.getLocation().orElse(def.getLocationName()),
                                                timeout,
                                                retries);
                        m_urlSpecifics.put(urlString, ipAddressList);
                    }
                }
            });

            return specifics;
        } finally {
            getReadLock().unlock();
        }
    }

    private void clearUrlSpecifics() {
        m_urlSpecifics.clear();
    }
    private void clearExcludeRanges() {
        m_excludeRanges.clear();
    }

    /**
     * <p>getRanges</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<IPPollRange> getRanges() {
        final List<IPPollRange> includes = new LinkedList<IPPollRange>();

        getReadLock().lock();
        try {
            Long defaultTimeout = getConfiguration().getTimeout().orElse(DEFAULT_TIMEOUT);
            Integer defaultRetries =getConfiguration().getRetries().orElse(DEFAULT_RETRIES);

            for (final IncludeRange ir : getConfiguration().getIncludeRanges()) {

                // Validate IP range; if invalid, then log and discard this range
                try {
                    InetAddressUtils.toIpAddrBytes(ir.getBegin());
                } catch (Throwable e) {
                    LOG.warn("Begin address of discovery range is invalid, discarding: {}", ir.getBegin());
                    continue;
                } 

                try {
                    InetAddressUtils.toIpAddrBytes(ir.getEnd());
                } catch (Throwable e) {
                    LOG.warn("End address of discovery range is invalid, discarding: {}", ir.getEnd());
                    continue;
                }

                long timeout = ir.getTimeout().orElse(defaultTimeout);
                int retries = ir.getRetries().orElse(defaultRetries);

                try {
                    includes.add(new IPPollRange(ir.getForeignSource().orElse(null), ir.getLocation().orElse(null), ir.getBegin(), ir.getEnd(), timeout, retries));
                } catch (final UnknownHostException uhE) {
                    LOG.warn("Failed to convert address range ({}, {})", ir.getBegin(), ir.getEnd(), uhE);
                }
            }

            // Add ranges from definitions to the includes list.
            getConfiguration().getDefinitions().forEach(def -> {

                long timeoutFromDef = def.getTimeout().orElse(defaultTimeout);
                int retriesFromDef = def.getRetries().orElse(defaultRetries);

                for (final IncludeRange ir : def.getIncludeRanges()) {

                    // Validate IP range; if invalid, then log and discard this range
                    try {
                        InetAddressUtils.toIpAddrBytes(ir.getBegin());
                    } catch (Throwable e) {
                        LOG.warn("Definition: Begin address of discovery range is invalid, discarding: {}", ir.getBegin());
                        continue;
                    }

                    try {
                        InetAddressUtils.toIpAddrBytes(ir.getEnd());
                    } catch (Throwable e) {
                        LOG.warn("Definition : End address of discovery range is invalid, discarding: {}", ir.getEnd());
                        continue;
                    }

                    long timeout = ir.getTimeout().orElse(timeoutFromDef);
                    int retries = ir.getRetries().orElse(retriesFromDef);

                    try {
                        includes.add(new IPPollRange(ir.getForeignSource().orElse(def.getForeignSource()),
                                                    ir.getLocation().orElse(def.getLocationName()),
                                                    ir.getBegin(),
                                                    ir.getEnd(),
                                                    timeout,
                                                    retries));
                    } catch (final UnknownHostException uhE) {
                        LOG.warn("Definition: Failed to convert address range ({}, {})", ir.getBegin(), ir.getEnd(), uhE);
                    }
                }
            });

            return includes;
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getSpecifics</p>
     *
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<IPPollAddress> getSpecifics() {
        final List<IPPollAddress> specifics = new LinkedList<IPPollAddress>();

        getReadLock().lock();
        try {
            Long defaultTimeout = getConfiguration().getTimeout().orElse(DEFAULT_TIMEOUT);
            Integer defaultRetries = getConfiguration().getRetries().orElse(DEFAULT_RETRIES);
            for (final Specific s : getConfiguration().getSpecifics()) {

                long timeout = s.getTimeout().orElse(defaultTimeout);
                int retries = s.getRetries().orElse(defaultRetries);

                final String address = s.getAddress();

                try {
                    specifics.add(new IPPollAddress(s.getForeignSource().orElse(null), s.getLocation().orElse(null), InetAddressUtils.addr(address), timeout, retries));
                } catch (final IllegalArgumentException e) {
                    LOG.warn("Failed to convert address {}", address, e);
                }
            }

            // Map specifics from definitions to IPPollAddress and add them to specifics list.
            getConfiguration().getDefinitions().forEach(def -> {

                long timeoutFromDef = def.getTimeout().orElse(defaultTimeout);
                int retriesFromDef = def.getRetries().orElse(defaultRetries);

                for (final Specific s : def.getSpecifics()) {

                    long timeout = s.getTimeout().orElse(timeoutFromDef);
                    int retries = s.getRetries().orElse(retriesFromDef);

                    final String address = s.getAddress();

                    try {
                        specifics.add(new IPPollAddress(s.getForeignSource().orElse(def.getForeignSource()),
                                                        s.getLocation().orElse(def.getLocationName()),
                                                        InetAddressUtils.addr(address),
                                                        timeout,
                                                        retries));
                    } catch (final IllegalArgumentException e) {
                        LOG.warn("Definition: Specific: Failed to convert address {}", address, e);
                    }
                }

            });

            return specifics;
        } finally {
            getReadLock().unlock();
        }
    }


    /**
     * <p>isExcluded</p>
     *
     * @param address a {@link InetAddress} object.
     * @param location
     * @return a boolean.
     */
    @Override
    public boolean isExcluded(final InetAddress address, String location) {

        String defaultLocation = LocationUtils.DEFAULT_LOCATION_NAME;

        if (!m_excludeRanges.isEmpty()) {

            final byte[] laddr = address.getAddress();
            for (final ExcludeRange range : m_excludeRanges) {

                if (!isLocationMatched(range.getLocation().orElse(defaultLocation), location)) {
                    continue;
                }

                if (InetAddressUtils.isInetAddressInRange(laddr, range.getBegin(), range.getEnd())) {
                    return true;
                }
            }
        }
        return false;
    }

    void initializeExcludeRanges() {
        getReadLock().lock();
        try {
            m_excludeRanges.addAll(getConfiguration().getExcludeRanges());
            // Add exclude ranges from definitions. Assign location from definition.
            getConfiguration().getDefinitions().forEach(def -> {
                setLocationFromDefinition(def);
                m_excludeRanges.addAll(def.getExcludeRanges());
            });
        } finally {
            getReadLock().unlock();
        }
    }

    private void setLocationFromDefinition(Definition definition) {
        List<ExcludeRange> excludeRangesFromDef = definition.getExcludeRanges();
        excludeRangesFromDef.forEach(exRange -> {
            if (!exRange.getLocation().isPresent()) {
                exRange.setLocation(definition.getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME));
            }
        });
    }

    private boolean isLocationMatched(String locationFromRange, String location) {
        if (Strings.isNullOrEmpty(location)) {
            location = LocationUtils.DEFAULT_LOCATION_NAME;
        }
        return locationFromRange.equals(location);
    }

    @Override
    public String getForeignSource(InetAddress address) {
        getReadLock().lock();
        try {
            LOG.debug("Looking for matching foreign source specific IP or IP range with address: {}...", address);
    
            List<Specific> specificCollection = getConfiguration().getSpecifics();
            for (Specific specific : specificCollection) {
                String ipAddr = specific.getAddress();
    
                if (ipAddr.equals(InetAddressUtils.str(address))) {
    
                    String foreignSource = specific.getForeignSource().orElse(null);
                    LOG.debug("Matched foreign source {} matching address: {} against specific {}.", foreignSource, address, ipAddr);
                    return foreignSource;
                }
            }
    
            final byte[] laddr = address.getAddress();
    
            List<IncludeRange> includeRangeCollection = getConfiguration().getIncludeRanges();
            for (IncludeRange range : includeRangeCollection) {
    
                if (InetAddressUtils.isInetAddressInRange(laddr, range.getBegin(), range.getEnd())) {
    
                    String foreignSource = range.getForeignSource().orElse(null);
                    LOG.debug("Found foreign source {} with address {} in the range begin: {} and end: {}.", foreignSource, address, range.getBegin(), range.getEnd());
                    return foreignSource;
                }
            }
    
            List<IncludeUrl> includeUrlCollection = getConfiguration().getIncludeUrls();
            for (IncludeUrl includeUrl : includeUrlCollection) {
                String ipAddr = includeUrl.getUrl().orElse("");
                if (ipAddr.equals(InetAddressUtils.str(address))) {
    
                    String foreignSource = includeUrl.getForeignSource().orElse(null);
                    LOG.debug("Matched foreign source {} matching address: {} in specified URL.", foreignSource, address);
                    return foreignSource;
                }
            }
    
            return getConfiguration().getForeignSource().orElse(null);
        } finally {
            getReadLock().unlock();
        }
    }

    public List<Detector> getListOfDetectors(InetAddress inetAddress, String location) {

        String ipAddress = InetAddressUtils.toIpAddrString(inetAddress);
        if (Strings.isNullOrEmpty(location)) {
            location = LocationUtils.DEFAULT_LOCATION_NAME;
        }
        final String sourceLocation = location;
        final String defaultLocation = getConfiguration().getLocation().orElse(LocationUtils.DEFAULT_LOCATION_NAME);

        // Find the definition that has the IP Address and return the detectors for that definition.
        Optional<Definition> definition = getConfiguration().getDefinitions().stream().filter(def -> {

            //First match should be enough to pass filter.
            boolean specificMatch = def.getSpecifics().stream().anyMatch(specific -> {
                return specific.getLocation().orElse(def.getLocation().orElse(defaultLocation)).equals(sourceLocation)
                        && specific.getAddress().equals(ipAddress);
            });
            if(specificMatch) {
                // IP Address found in specifics.
                return true;
            }
            // Find match in url specifics
            boolean urlSpecficMatch = def.getIncludeUrls().stream().anyMatch(includeUrl -> {
                boolean urlLocationMatch = includeUrl.getUrl().isPresent() &&
                        includeUrl.getLocation().orElse(def.getLocation().orElse(defaultLocation)).equals(sourceLocation);
                if (urlLocationMatch) {
                    List<String> urlSpecifics = m_urlSpecifics.get(includeUrl.getUrl().get());
                    if (urlSpecifics != null) {
                        return urlSpecifics.contains(ipAddress);
                    }
                }
                return false;
            });
            if (urlSpecficMatch) {
                //IP Address found in URL specifics.
                return true;
            }
            // Find match in ranges.
            return def.getIncludeRanges().stream().anyMatch(includeRange ->
                    includeRange.getLocation().orElse(def.getLocation().orElse(defaultLocation)).equals(sourceLocation) &&
                            InetAddressUtils.isInetAddressInRange(ipAddress, includeRange.getBegin(), includeRange.getEnd()));

        }).findFirst();

        if (definition.isPresent()) {
            return definition.get().getDetectors();
        } else {
            return new ArrayList<>();
        }
    }


    /**
     * <p>getPacketsPerSecond</p>
     *
     * @return a int.
     */
    @Override
    public double getPacketsPerSecond() {
        getReadLock().lock();
        try {
            return getConfiguration().getPacketsPerSecond().orElse(DEFAULT_PACKETS_PER_SECOND);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getIntraPacketDelay</p>
     *
     * @return a long.
     */
    @Override
    public long getIntraPacketDelay() {
        getReadLock().lock();
        try {
            return Math.round(1000.0 / getConfiguration().getPacketsPerSecond().orElse(DEFAULT_PACKETS_PER_SECOND));
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getExcludingInterator</p>
     *
     * @param it a {@link java.util.Iterator} object.
     * @return a {@link java.util.Iterator} object.
     */
    @Override
    public Iterator<IPPollAddress> getExcludingIterator(final Iterator<IPPollAddress> it) {
        return StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(it, ORDERED | IMMUTABLE), false
            )
            // Filter out excluded addresses
            .filter(item -> !isExcluded(item.getAddress(), item.getLocation()))
            .iterator();
    }

    /**
     * <p>getConfiguredAddresses</p>
     * 
     * TODO: This function is inefficient. It has O(n^2) complexity based on the
     * product of the include ranges and exclude ranges. This might cause problems
     * if users are using a large number of excluded ranges.
     *
     * @return a {@link java.lang.Iterable} object.
     */
    @Override
    public Iterable<IPPollAddress> getConfiguredAddresses() {
        getReadLock().lock();
        try {
            final List<IPPollAddress> specifics = getSpecifics();
            final List<IPPollRange> ranges = getRanges();
            specifics.addAll(getURLSpecifics());
            initializeExcludeRanges();

            final List<Iterator<IPPollAddress>> iters = new ArrayList<Iterator<IPPollAddress>>();
            iters.add(specifics.iterator());

            for(final IPPollRange range : ranges) {
                iters.add(getExcludingIterator(range.iterator()));
            }

            return IteratorUtils.concatIterators(iters);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getRestartSleepTime</p>
     *
     * @return a long.
     */
    @Override
    public long getRestartSleepTime() {
        getReadLock().lock();
        try {
            return getConfiguration().getRestartSleepTime().orElse(86400000L);
        } finally {
            getReadLock().unlock();
        }
    }

    /**
     * <p>getInitialSleepTime</p>
     *
     * @return a long.
     */
    @Override
    public long getInitialSleepTime() {
        getReadLock().lock();
        try {
            return getConfiguration().getInitialSleepTime().orElse(30000L);
        } finally {
            getReadLock().unlock();
        }
    }
}
