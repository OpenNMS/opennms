package org.opennms.protocols.jmx.connectors;

import java.net.InetAddress;
import java.security.KeyStore;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import org.apache.log4j.Category;
import org.opennms.core.utils.ParameterMap;
import org.opennms.core.utils.ThreadCategory;

public class JMXSecureConnectionFactory
{
    static ThreadCategory log = ThreadCategory.getInstance(JMXSecureConnectionFactory.class);

    public static Jsr160ConnectionWrapper getMBeanServerConnection(Map propertiesMap, InetAddress address)
    {
        Jsr160ConnectionWrapper connectionWrapper = null;

        JMXServiceURL url = null;

        String factory = ParameterMap.getKeyedString(propertiesMap, "factory", "SASL");
        int port = ParameterMap.getKeyedInteger(propertiesMap, "port", 11162);
        String protocol = ParameterMap.getKeyedString(propertiesMap, "protocol", "jmxmp");
        String urlPath = ParameterMap.getKeyedString(propertiesMap, "urlPath", "");
        String sunCacao = ParameterMap.getKeyedString(propertiesMap, "sunCacao", "false");

        log.debug("JMX: " + factory + " - service:" + protocol + "//" + address.getHostAddress() + ":" + port + urlPath);

        if (factory.equals("SASL"))
        {
            try
            {
                String username = ParameterMap.getKeyedString(propertiesMap, "username", null);
                String password = ParameterMap.getKeyedString(propertiesMap, "password", null);

                HashMap<String, Object> env = new HashMap<String, Object>();

                while (true)
                {
                    AnyServerX509TrustManager tm;
                    KeyStore ks;

                    try
                    {
                        ks = KeyStore.getInstance(KeyStore.getDefaultType());
                        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                        tmf.init(ks);
                        X509TrustManager defaultTrustManager = (X509TrustManager) tmf.getTrustManagers()[0];
                        tm = new AnyServerX509TrustManager();
                        SSLContext ctx = SSLContext.getInstance("TLSv1");
                        ctx.init(null, new TrustManager[]{tm}, null);
                        SSLSocketFactory ssf = ctx.getSocketFactory();
                        env.put("jmx.remote.tls.socket.factory", ssf);
                    }
                    catch (Exception e)
                    {
                        log.error("Something bad occured: " + e.getMessage());
                        throw e;
                    }

                    Security.addProvider(new com.sun.security.sasl.Provider());
                    String[] creds;
                    if (sunCacao.equals("true"))
                        creds = new String[]{"com.sun.cacao.user\001" + username, password};
                    else
                        creds = new String[]{username, password};
                    env.put("jmx.remote.profiles", "TLS SASL/PLAIN");
                    env.put("jmx.remote.credentials", creds);

                    // Create an JMXMP connector client and
                    // connect it to the JMXMP connector server
                    //
                    url = new JMXServiceURL(protocol, address.getHostAddress(), port, urlPath);

                    JMXConnector connector = JMXConnectorFactory.newJMXConnector(url, null);

                    // Connect and invoke an operation on the remote MBeanServer
                    try
                    {
                        connector.connect(env);
                    }
                    catch (SSLException e)
                    {
                        continue;
                    }
                    catch (SecurityException x)
                    {
                        log.error("Security exception: bad credentials");
                        throw x;
                    }
                    MBeanServerConnection connection = connector.getMBeanServerConnection();
                    connectionWrapper = new Jsr160ConnectionWrapper(connector, connection);
                    break;
                }
            }
            catch (Exception e)
            {
                log.error("Unable to get MBeanServerConnection: " + url);
            }
        }
        return connectionWrapper;
    }

    private static class AnyServerX509TrustManager implements X509TrustManager
    {
        // Documented in X509TrustManager
        public X509Certificate[] getAcceptedIssuers()
        {
            // since client authentication is not supported by this
            // trust manager, there's no certicate authority trusted
            // for authenticating peers
            return new X509Certificate[0];
        }

        // Documented in X509TrustManager
        public void checkClientTrusted(X509Certificate[] certs, String authType)
                throws CertificateException
        {
            // this trust manager is dedicated to server authentication
            throw new CertificateException("not supported");
        }

        // Documented in X509TrustManager
        public void checkServerTrusted(X509Certificate[] certs, String authType)
                throws CertificateException
        {
            // any certificate sent by the server is automatically accepted
        }
    }

}
