/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller;

import java.io.File;
import java.io.FileInputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.IOUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.CastorUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * The Class MonitorTester.
 * <p>Execute a poller test from the command line using current settings from poller-configuration.xml</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public abstract class MonitorTester {

    private static final String CMD_SYNTAX = "poller-test [options]";

    public static class SimpleDataSource implements DataSource {
        private String m_driver;
        private String m_url;
        private Properties m_properties = new Properties();
        private Integer m_timeout = null;

        public SimpleDataSource(JdbcDataSource ds) throws ClassNotFoundException {
            m_driver = ds.getClassName();
            m_url = ds.getUrl();
            m_properties.put("user", ds.getUserName());
            m_properties.put("password", ds.getPassword());
            Class.forName(m_driver);
            for (Param param : ds.getParamCollection()) {
                m_properties.put(param.getName(), param.getValue());
            }
        }

        public Connection getConnection() throws SQLException {
            if (m_timeout == null) {
                return DriverManager.getConnection(m_url, m_properties);
            } else {
                int oldTimeout = DriverManager.getLoginTimeout();
                DriverManager.setLoginTimeout(m_timeout);
                Connection conn = DriverManager.getConnection(m_url, m_properties);
                DriverManager.setLoginTimeout(oldTimeout);
                return conn;
            }
        }

        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("getConnection(String, String) not implemented");
        }

        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("getLogWriter() not implemented");
        }

        public int getLoginTimeout() throws SQLException {
            return m_timeout == null ? -1 : m_timeout;
        }

        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
        }

        public void setLoginTimeout(int seconds) throws SQLException {
            m_timeout = seconds;
        }

        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("getParentLogger not supported");
        }
    }

    public static class SimpleMonitoredService implements MonitoredService {
        private InetAddress ipAddress;
        private int nodeId;
        private String nodeLabel;
        private String svcName;

        public SimpleMonitoredService(final InetAddress ipAddress, int nodeId, String nodeLabel, String svcName) {
            this.ipAddress = ipAddress;
            this.nodeId = nodeId;
            this.nodeLabel = nodeLabel;
            this.svcName = svcName;
        }

        public String getSvcUrl() {
            return null;
        }

        public String getSvcName() {
            return svcName;
        }

        public String getIpAddr() {
            return ipAddress.getHostAddress();
        }

        public int getNodeId() {
            return nodeId;
        }

        public String getNodeLabel() {
            return nodeLabel;
        }

        public NetworkInterface<InetAddress> getNetInterface() {
            return new InetNetworkInterface(getAddress());
        }

        public InetAddress getAddress() {
            return ipAddress;
        }
    }

    private static void registerProperties(Properties properties) {
        for (Object o : properties.keySet()) {
            String key = (String) o;
            System.setProperty(key, properties.getProperty(key));
        }
    }

    private static void loadProperties(Properties properties, String fileName) throws Exception {
        File propertiesFile = ConfigFileConstants.getConfigFileByName(fileName);
        properties.load(new FileInputStream(propertiesFile));
    }

    private static void initialize() {
        try {
            final Properties mainProperties = new Properties();
            loadProperties(mainProperties, "opennms.properties");
            registerProperties(mainProperties);

            final Properties rrdProperties = new Properties();
            loadProperties(rrdProperties, "rrd-configuration.properties");
            registerProperties(rrdProperties);

            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
            DataSourceConfiguration dsc = null;
            FileInputStream fileInputStream = null;
            try {
                fileInputStream = new FileInputStream(cfgFile);
                dsc = CastorUtils.unmarshal(DataSourceConfiguration.class, fileInputStream);
            } finally {
                IOUtils.closeQuietly(fileInputStream);
            } 
            boolean found = false;
            for (JdbcDataSource jds : dsc.getJdbcDataSourceCollection()) {
                if (jds.getName().equals("opennms")) {
                    DataSourceFactory.setInstance(new SimpleDataSource(jds));
                    found = true;
                }
            }
            if (!found) {
                System.err.printf("Error: Can't find OpenNMS database configuration.\n");
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.printf("Error: Can't initialize OpenNMS database connection factory. %s\n", e.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        Options options = new Options();
        Option oI = new Option("i", "ipaddress", true, "IP Address to test [required]");
        oI.setRequired(true);
        options.addOption(oI);
        Option oS = new Option("s", "service", true, "Service name [required]");
        oS.setRequired(true);
        options.addOption(oS);
        options.addOption("P", "package", true, "Poller Package");
        options.addOption("p", "param", true, "Service parameter ~ key=value");
        options.addOption("c", "class", true, "Monitor Class");

        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            new HelpFormatter().printHelp(80, CMD_SYNTAX, String.format("ERROR: %s%n", e.getMessage()), options, null);
            System.exit(1);
        }

        initialize();

        final String packageName = cmd.getOptionValue('P');
        final String monitorClass = cmd.getOptionValue('c');
        final String ipAddress = cmd.getOptionValue('i');
        final String serviceName = cmd.getOptionValue('s');

        Map<String,Object> parameters = new HashMap<String,Object>();
        if (cmd.hasOption('p')) {
            for (String parm : cmd.getOptionValues('p')) {
                String[] data = parm.split("=");
                if (data.length == 2 && data[0] != null && data[1] != null) {
                    parameters.put(data[0], data[1]);
                }
            }
        }

        final InetAddress addr = InetAddressUtils.addr(ipAddress);
        if (addr == null) {
            throw new IllegalStateException("Error getting InetAddress object for " + ipAddress);
        }

        final IpInterfaceDao dao = BeanUtils.getBean("daoContext", "ipInterfaceDao", IpInterfaceDao.class);
        final TransactionTemplate tt = BeanUtils.getBean("daoContext", "transactionTemplate", TransactionTemplate.class);
        MonitoredService monSvc = tt.execute(new TransactionCallback<MonitoredService>() {
            @Override
            public MonitoredService doInTransaction(TransactionStatus status) {
                final List<OnmsIpInterface> ips = dao.findByIpAddress(ipAddress);
                if (ips == null  || ips.size() == 0) {
                    System.err.printf("Error: Can't find the IP address %s on the database\n", ipAddress);
                    return null;
                }
                if (ips.size() > 1) {
                    System.out.printf("Warning: there are several IP interface objects associated with the IP address %s (picking the first one)\n", ipAddress);
                }
                OnmsNode n = ips.get(0).getNode();
                return new SimpleMonitoredService(addr, n.getId(), n.getLabel(), serviceName);
            }
        });
        if (monSvc == null) {
            System.exit(1);
        }

        try {
            PollerConfigFactory.init();
        } catch (Exception e) {
            System.err.printf("Error: Can't initialize poller-configuration.xml. %s%n", e.getMessage());
            System.exit(1);
        }
        PollerConfig config = PollerConfigFactory.getInstance();

        System.out.printf("Checking service %s on IP %s%n", serviceName, ipAddress);

        org.opennms.netmgt.config.poller.Package pkg = packageName == null ? config.getFirstLocalPackageMatch(ipAddress) : config.getPackage(packageName);
        if (pkg == null) {
            System.err.printf("Error: Package %s doesn't exist%n", packageName);
            System.exit(1);
        }
        System.out.printf("Package: %s%n", pkg.getName());

        Service svc = config.getServiceInPackage(serviceName, pkg);
        if (svc == null) {
            System.err.printf("Error: Service %s not defined on package %s%n", serviceName, packageName);
            System.exit(1);
        }

        ServiceMonitor monitor = null;
        if (monitorClass == null) {
            monitor = config.getServiceMonitor(serviceName);
            if (monitor == null) {
                System.err.printf("Error: Service %s doesn't have a monitor class defined%n", serviceName);
                System.exit(1);
            }
        } else {
            try {
                final Class<? extends ServiceMonitor> mc = Class.forName(monitorClass).asSubclass(ServiceMonitor.class);
                monitor = mc.newInstance();
            } catch (Exception e) {
                System.err.printf("Error: Can't instantiate %s because %s%n", monitorClass, e.getMessage());
                System.exit(1);
            }
        }
        System.out.printf("Monitor: %s%n", monitor.getClass().getName());

        if (config.isPolledLocally(ipAddress, serviceName)) {
            for (Parameter p : svc.getParameters()) {
                if (!parameters.containsKey(p.getKey())) {
                    String value = p.getValue();
                    if (value == null) {
                        try {
                            value = JaxbUtils.marshal(p.getAnyObject());
                        } catch (Exception e) {}
                    }
                    parameters.put(p.getKey(), value);
                }
            }
            for (Entry<String,Object> e : parameters.entrySet()) {
                System.out.printf("Parameter %s : %s%n", e.getKey(), e.getValue());
            }
            try {
                PollStatus status = monitor.poll(monSvc, parameters);
                System.out.printf("Available ? %s (status %s[%s])%n", status.isAvailable(), status.getStatusName(), status.getStatusCode());
                if (status.isAvailable()) {
                    System.out.printf("Response time: %s%n", status.getResponseTime());
                } else {
                    if (status.getReason() != null) {
                        System.out.printf("Reason: %s%n", status.getReason());
                    }
                }
            } catch (Exception e) {
                System.err.println("Error: Can't execute the monitor. " + e.getMessage());
                System.exit(1);
            }
        } else {
            System.err.printf("Error: Polling is not enabled for service %s using IP %s%n", serviceName, ipAddress);
        }

        System.exit(0);
    }

}
