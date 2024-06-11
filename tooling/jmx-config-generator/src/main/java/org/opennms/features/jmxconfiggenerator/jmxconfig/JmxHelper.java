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
