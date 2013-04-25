/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

package org.opennms.core.utils.url;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;

/**
 * Convenience factory class to generate URL connections with customized protocol handler.
 *
 * @author <a href="mailto:christian.pape@informatik.hs-fulda.de">Christian Pape</a>
 * @author <a href="mailto:ronny@opennms.org">Ronny Trommer</a>
 */
public class GenericURLFactory implements URLStreamHandlerFactory {

    /**
     * Logging to output.log
     */
    private final Logger logger = LoggerFactory.getLogger("OpenNMS.Output." + GenericURLFactory.class.getName());

    /**
     * Map with protocol and URL connections
     */
    private HashMap<String, String> urlConnections = new HashMap<String, String>();

    /**
     * Map with protocol and URL default ports
     */
    private HashMap<String, Integer> urlDefaultPorts = new HashMap<String, Integer>();

    /**
     * URL factory
     */
    private static GenericURLFactory genericUrlFactory = new GenericURLFactory();

    /**
     * Default constructor to initialize URL connections
     */
    private GenericURLFactory() {
        // Map the protocol dns against the DNS implementation
        addURLConnection("dns", "org.opennms.netmgt.provision.service.dns.DnsRequisitionUrlConnection", 53);
        addURLConnection("vmware", "org.opennms.netmgt.provision.service.vmware.VmwareRequisitionUrlConnection", 443);
    }

    /**
     * <p>initialize</p>
     * <p/>
     * Initializing the URL Factory
     */
    public static void initialize() {
        try {
            URL.setURLStreamHandlerFactory(genericUrlFactory);
        } catch (Error exception) {
            // ignore error concerning resetting the URLStreamHandlerFactory
        }
    }

    /**
     * <p>getInstance</p>
     * <p/>
     * Implement the GenericURLFactory as singleton
     *
     * @return a {org.opennms.core.utils.url.GenericURLFactory} object.
     */
    public static GenericURLFactory getInstance() {
        return genericUrlFactory;
    }

    /**
     * <p>addURLConnection</p>
     * <p/>
     * Add protocol URL connection handler with specific class name
     *
     * @param protocol    name as {@link java.lang.String} object.
     * @param classname   full qualified classname as {@link java.lang.String} object.
     * @param defaultPort the default port for given protocol as {@java.lang.int} object.
     */
    public void addURLConnection(String protocol, String classname, int defaultPort) {
        urlConnections.put(protocol, classname);
        urlDefaultPorts.put(protocol, defaultPort);
    }

    /**
     * <p>addURLConnection</p>
     * <p/>
     * Add protocol URL connection handler with specific class name
     *
     * @param protocol  name as {@link java.lang.String} object.
     * @param classname full qualified classname as {@link java.lang.String} object.
     */
    public void addURLConnection(String protocol, String classname) {
        addURLConnection(protocol, classname, -1);
    }

    /**
     * <p>removeURLConnection</p>
     * <p/>
     * Remove a protocol URL connection handler
     *
     * @param protocol name as {@link java.lang.String} object.
     */
    public void removeURLConnection(String protocol) {
        if (urlConnections.containsKey(protocol)) {
            urlConnections.remove(protocol); // remove protocol class mapping
            logger.debug("Remove existing protocol: '{}'", protocol);
        }
        // else nothing to do
    }

    /**
     * <p>getURLConnections</p>
     * <p/>
     * Get the map with protocol and implementation classes for customized URL connections
     *
     * @return a {@link java.util.HashMap} object
     */
    public HashMap<String, String> getURLConnections() {
        return urlConnections;
    }

    /**
     * <p>createURLStreamHandler</p>
     * <p/>
     * Create stream handler
     *
     * @param protocol name as {@link java.lang.String} object.
     * @return a {@java.net.URLStreamHandler} object.
     */
    @SuppressWarnings("unchecked")
    public URLStreamHandler createURLStreamHandler(String protocol) {
        Class<? extends URLConnection> c = null;
        if (!urlConnections.containsKey(protocol)) {
            logger.warn("No protocol mapping with '{}' found. Return null", protocol);
            return null; // No existing protocol mapping
        }

        try {
            c = (Class<? extends URLConnection>) Class.forName(urlConnections.get(protocol));
        } catch (ClassNotFoundException e) {
            logger.warn("Class not found for protocol '{}' and return null. Error message: '{}'", protocol, e.getMessage());
            return null; // We couldn't load a class for the protocol
        }
        return new GenericURLStreamHandler(c, urlDefaultPorts.get(protocol)); // Return the stream handler for the customized protocol
    }
}
