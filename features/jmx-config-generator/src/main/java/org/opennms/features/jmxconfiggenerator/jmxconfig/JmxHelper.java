/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.jmxconfiggenerator.jmxconfig;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

/**
 * Helper class to handle create JMX connections.
 */
public class JmxHelper {

    /**
     * @deprecated Use {@link Jsr160ConnectionFactory#getUrl(InetAddress, int, String, String)} instead.
     * 
     * @param url
     * @param hostName
     * @param port
     * @param jmxmp
     * @return
     * @throws MalformedURLException
     */
    public static JMXServiceURL createJmxServiceUrl(String url, String hostName, String port, boolean jmxmp) throws MalformedURLException {
        if (url != null) {
            return new JMXServiceURL(url);
        }
        if (hostName != null && port != null) {
            if (jmxmp) {
                return new JMXServiceURL(String.format("service:jmx:jmxmp://%s:%s", hostName, port));
            }
            return new JMXServiceURL(String.format("service:jmx:rmi:///jndi/rmi://%s:%s/jmxrmi", hostName, port));
        }
        throw new IllegalStateException("Something went wrong. Was not able to determine JMX Connection URL.");
    }

    /**
     * @deprecated Use {@link DefaultJmxConnector#createConnection(InetAddress, Map<String, String>)} instead.
     * 
     * @param username
     * @param password
     * @param serviceUrl
     * @return
     * @throws IOException
     */
    public static JMXConnector createJmxConnector(String username, String password, JMXServiceURL serviceUrl) throws IOException {
        Objects.requireNonNull(serviceUrl, "You must specify a JMXServiceURL in order to create a JMXConnector.");
        HashMap<String, String[]> env = new HashMap<String, String[]>();
        if (username != null && password != null) {
            String[] credentials = new String[] { username, password };
            env.put("jmx.remote.credentials", credentials);
        }
        JMXConnector jmxConnector = JMXConnectorFactory.connect(serviceUrl, env);
        return jmxConnector;
    }

    public static Map<String, String> loadInternalDictionary() throws IOException {
        Map<String, String> internalDictionary = new HashMap<String, String>();
        Properties properties = new Properties();
        try (BufferedInputStream stream = new BufferedInputStream(JmxHelper.class.getResourceAsStream("/dictionary.properties"))) {
            properties.load(stream);
        } catch (IOException ex) {
            throw new IOException("Load dictionary entries from internal properties files error: '" + ex.getMessage() + "'", ex);
        }
        for (final Map.Entry<?,?> entry : properties.entrySet()) {
            final Object key = entry.getKey();
            final Object value = entry.getValue();
            internalDictionary.put(key.toString(), value == null? null : value.toString());
        }
        return internalDictionary;
    }
}
