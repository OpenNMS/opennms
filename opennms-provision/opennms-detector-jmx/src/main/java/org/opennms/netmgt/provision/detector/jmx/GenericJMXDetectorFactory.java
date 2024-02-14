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
package org.opennms.netmgt.provision.detector.jmx;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;

import javax.management.remote.JMXServiceURL;

import org.opennms.netmgt.config.jmx.MBeanServer;
import org.opennms.netmgt.dao.jmx.JmxConfigDao;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfig;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfigBuilder;
import org.opennms.netmgt.provision.DetectRequest;
import org.opennms.netmgt.provision.support.DetectRequestImpl;
import org.opennms.netmgt.provision.support.GenericServiceDetectorFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class GenericJMXDetectorFactory<T extends JMXDetector> extends GenericServiceDetectorFactory<JMXDetector> {

    @Autowired(required=false)
    protected JmxConfigDao jmxConfigDao;

    @SuppressWarnings("unchecked")
    public GenericJMXDetectorFactory(Class<T> clazz) {
        super((Class<JMXDetector>) clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T createDetector(Map<String, String> properties) {
        return (T)super.createDetector(properties);
    }

    @Override
    public DetectRequest buildRequest(String location, InetAddress address, Integer port, Map<String, String> attributes) {
        // in case port is null, but url is provided, the port is extracted from the url
        if (port == null && attributes.containsKey("url")) {
            try {
                final JmxConnectionConfig config = JmxConnectionConfigBuilder.buildFrom(address, attributes).build();
                port = new JMXServiceURL(config.getUrl()).getPort();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("url is not valid", e);
            }
        }
        return new DetectRequestImpl(address, port, getRuntimeAttributes(address, port));
    }

    private Map<String, String> getRuntimeAttributes(InetAddress address, Integer port) {
        String ipAddress = address.getHostAddress();
        if (port == null) {
            throw new IllegalArgumentException(" Port number needs to be specified in the form of port=number ");
        }

        if (jmxConfigDao == null) {
            return Collections.emptyMap();
        } else {
            MBeanServer serverConfig = jmxConfigDao.getConfig().lookupMBeanServer(ipAddress, port);
            if (serverConfig == null) {
                return Collections.emptyMap();
            } else {
                return serverConfig.getParameterMap();
            }
        }
    }

    public void setJmxConfigDao(JmxConfigDao jmxConfigDao) {
        this.jmxConfigDao = jmxConfigDao;
    }
}

