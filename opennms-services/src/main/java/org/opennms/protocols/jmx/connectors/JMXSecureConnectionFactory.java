/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.protocols.jmx.connectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.ParameterMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

/**
 * <p>JMXSecureConnectionFactory class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class JMXSecureConnectionFactory {
	
	private static final Logger LOG = LoggerFactory.getLogger(JMXSecureConnectionFactory.class);

    /**
     * <p>getMBeanServerConnection</p>
     *
     * @param propertiesMap a {@link java.util.Map} object.
     * @param address       a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.protocols.jmx.connectors.Jsr160ConnectionWrapper} object.
     */
    public static Jsr160ConnectionWrapper getMBeanServerConnection(Map<?, ?> propertiesMap, InetAddress address) {
        Jsr160ConnectionWrapper connectionWrapper = null;

        JMXServiceURL url = null;

        String factory = ParameterMap.getKeyedString(propertiesMap, "factory", "SASL");
        int port = ParameterMap.getKeyedInteger(propertiesMap, "port", 11162);
        String protocol = ParameterMap.getKeyedString(propertiesMap, "protocol", "jmxmp");
        String urlPath = ParameterMap.getKeyedString(propertiesMap, "urlPath", "");
        String sunCacao = ParameterMap.getKeyedString(propertiesMap, "sunCacao", "false");

        // RMI and JMXMP use different URL schemes
        try {
            if (protocol.equalsIgnoreCase("jmxmp")) {

                // Create an JMXMP connector client and
                // connect it to the JMXMP connector server
                //
                url = new JMXServiceURL(protocol, InetAddressUtils.str(address), port, urlPath);
            } else {
                // Fallback, building a URL for RMI
                url = new JMXServiceURL("service:jmx:" + protocol + ":///jndi/" + protocol + "://" + InetAddressUtils.str(address) + ":" + port + urlPath);
            }
        } catch (MalformedURLException e) {
            LOG.error("JMXServiceURL exception: {}. Error message: {}", url, e.getMessage());
        }
        LOG.debug("Set JMXServiceURL: {}", url);

        // configure and create Simple Authentication and Security Layer
        if (factory.equals("SASL")) {
            try {
                String username = ParameterMap.getKeyedString(propertiesMap, "username", null);
                String password = ParameterMap.getKeyedString(propertiesMap, "password", null);

                HashMap<String, Object> env = new HashMap<String, Object>();

                // TODO: We can have a deadlock here
                while (true) {
                    AnyServerX509TrustManager tm;
                    KeyStore ks;

                    try {
                        ks = KeyStore.getInstance(KeyStore.getDefaultType());
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        tmf.init(ks);
                        // X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
                        tm = new AnyServerX509TrustManager();
                        SSLContext ctx = SSLContext.getInstance("TLSv1");
                        ctx.init(null, new TrustManager[]{tm}, null);
                        SSLSocketFactory ssf = ctx.getSocketFactory();
                        env.put("jmx.remote.tls.socket.factory", ssf);
                    } catch (Throwable e) {
                    	LOG.error("Something bad occured: {}", e.getMessage());
                        throw e;
                    }

                    // We don't need to add this provider manually... it is included in the JVM
                    // by default in Java5+
                    //
                    // @see $JAVA_HOME/jre/lib/security/java.security
                    //
                    //Security.addProvider(new com.sun.security.sasl.Provider());

                    String[] creds;
                    if (sunCacao.equals("true"))
                        creds = new String[]{"com.sun.cacao.user\001" + username, password};
                    else
                        creds = new String[]{username, password};
                    env.put("jmx.remote.profiles", "TLS SASL/PLAIN");
                    env.put("jmx.remote.credentials", creds);

                    JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);

                    // Connect and invoke an operation on the remote MBeanServer
                    try {
                        connector.connect(env);
                    } catch (SSLException e) {
                        LOG.warn("SSLException occured. Error message: {}", e.getMessage());
                    } catch (SecurityException x) {
                        LOG.error("Security exception: bad credentials. Error message: {}", x.getMessage());
                    }
                    MBeanServerConnection connection = connector.getMBeanServerConnection();
                    connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
                    break;
                }
            } catch (Throwable e) {
                LOG.error("Unable to get MBeanServerConnection: {}. Error message: {}", url, e.getMessage());
            }
        }
        return connectionWrapper;
    }

    private static class AnyServerX509TrustManager implements X509TrustManager {
        // Documented in X509TrustManager
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            // since client authentication is not supported by this
            // trust manager, there's no certificate authority trusted
            // for authenticating peers
            return new X509Certificate[0];
        }

        // Documented in X509TrustManager
        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            // this trust manager is dedicated to server authentication
            throw new CertificateException("not supported");
        }

        // Documented in X509TrustManager
        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException {
            // any certificate sent by the server is automatically accepted
        }
    }
}
