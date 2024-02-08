/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.utils.url;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.HashMap;
import java.util.Map;

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
    private final Logger logger = LoggerFactory.getLogger(GenericURLFactory.class);

    /**
     * Map with protocol and URL connections
     */
    private Map<String, String> urlConnections = new HashMap<String, String>();

    /**
     * Map with protocol and URL default ports
     */
    private Map<String, Integer> urlDefaultPorts = new HashMap<String, Integer>();

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
        addURLConnection("requisition", "org.opennms.netmgt.provision.service.requisition.RequisitionUrlConnection", 443);
    }

    /**
     * <p>initialize</p>
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
     * Implement the GenericURLFactory as singleton
     *
     * @return a {org.opennms.core.utils.url.GenericURLFactory} object.
     */
    public static GenericURLFactory getInstance() {
        return genericUrlFactory;
    }

    /**
     * <p>addURLConnection</p>
     * Add protocol URL connection handler with specific class name
     *
     * @param protocol    name as {@link java.lang.String} object.
     * @param classname   full qualified classname as {@link java.lang.String} object.
     * @param defaultPort the default port for given protocol as {@link java.lang.Integer} object.
     */
    public void addURLConnection(String protocol, String classname, int defaultPort) {
        urlConnections.put(protocol, classname);
        urlDefaultPorts.put(protocol, defaultPort);
    }

    /**
     * <p>addURLConnection</p>
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
     * Get the map with protocol and implementation classes for customized URL connections
     *
     * @return a {@link java.util.HashMap} object
     */
    public Map<String, String> getURLConnections() {
        return urlConnections;
    }

    /**
     * <p>createURLStreamHandler</p>
     * Create stream handler
     *
     * @param protocol name as {@link java.lang.String} object.
     * @return a {@link java.net.URLStreamHandler} object.
     */
    @SuppressWarnings("unchecked")
    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        Class<? extends URLConnection> c = null;
        if (!urlConnections.containsKey(protocol)) {
            logger.info("No protocol mapping with '{}' found. Return null. Creating...", protocol);
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
