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
package org.opennms.reporting.jasperreports.compiler;

import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Logger;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.opennmsDataSources.DataSourceConfiguration;
import org.opennms.netmgt.config.opennmsDataSources.JdbcDataSource;
import org.opennms.netmgt.config.opennmsDataSources.Param;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;

/**
 * The Class ReportCompiler.
 * 
 * <p>This class was designed to compile all the JRXML files on $OPENNMS_HOME/etc/report-templates.</p>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class ReportCompiler {

    /**
     * The Class SimpleDataSource.
     * <p>From <code>org.opennms.netmgt.poller.MonitorTester</code></p>
     */
    public static class SimpleDataSource implements DataSource {

        /** The driver. */
        private String m_driver;

        /** The URL. */
        private String m_url;

        /** The properties. */
        private Properties m_properties = new Properties();

        /** The timeout. */
        private Integer m_timeout = null;

        /**
         * Instantiates a new simple data source.
         *
         * @param ds the datasource
         * @throws ClassNotFoundException the class not found exception
         */
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

        /* (non-Javadoc)
         * @see javax.sql.DataSource#getConnection()
         */
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

        /* (non-Javadoc)
         * @see javax.sql.DataSource#getConnection(java.lang.String, java.lang.String)
         */
        public Connection getConnection(String username, String password) throws SQLException {
            throw new UnsupportedOperationException("getConnection(String, String) not implemented");
        }

        /* (non-Javadoc)
         * @see javax.sql.CommonDataSource#getLogWriter()
         */
        public PrintWriter getLogWriter() throws SQLException {
            throw new UnsupportedOperationException("getLogWriter() not implemented");
        }

        /* (non-Javadoc)
         * @see javax.sql.CommonDataSource#getLoginTimeout()
         */
        public int getLoginTimeout() throws SQLException {
            return m_timeout == null ? -1 : m_timeout;
        }

        /* (non-Javadoc)
         * @see javax.sql.CommonDataSource#setLogWriter(java.io.PrintWriter)
         */
        public void setLogWriter(PrintWriter out) throws SQLException {
            throw new UnsupportedOperationException("setLogWriter(PrintWriter) not implemented");
        }

        /* (non-Javadoc)
         * @see javax.sql.CommonDataSource#setLoginTimeout(int)
         */
        public void setLoginTimeout(int seconds) throws SQLException {
            m_timeout = seconds;
        }

        /* (non-Javadoc)
         * @see java.sql.Wrapper#unwrap(java.lang.Class)
         */
        public <T> T unwrap(Class<T> iface) throws SQLException {
            return null;
        }

        /* (non-Javadoc)
         * @see java.sql.Wrapper#isWrapperFor(java.lang.Class)
         */
        public boolean isWrapperFor(Class<?> iface) throws SQLException {
            return false;
        }

        /* (non-Javadoc)
         * @see javax.sql.CommonDataSource#getParentLogger()
         */
        @Override
        public Logger getParentLogger() throws SQLFeatureNotSupportedException {
            throw new SQLFeatureNotSupportedException("getParentLogger not supported");
        }
    }

    /**
     * Initializes a single instance DB connection factory.
     * <p>From <code>org.opennms.netmgt.poller.MonitorTester</code></p>
     */
    private static void initializeSingleInstanceDatabase() {
        try {
            final File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.OPENNMS_DATASOURCE_CONFIG_FILE_NAME);
            final DataSourceConfiguration dsc = JaxbUtils.unmarshal(DataSourceConfiguration.class, cfgFile);

            boolean found = false;
            for (JdbcDataSource jds : dsc.getJdbcDataSourceCollection()) {
                if (jds.getName().equals("opennms")) {
                    System.out.printf("Initializing datatabase %s\n", jds.getUrl());
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

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(String[] args) {
        if (args.length > 1 && new File(args[0]).exists()) {
            System.setProperty("opennms.home", args[0]);
        }

        initializeSingleInstanceDatabase();
        BeanUtils.getBeanFactory("jasperReportContext"); // To trigger Spring Initialization

        File reportsDirectory = new File(ConfigFileConstants.getFilePathString(), "report-templates");
        System.out.printf("Analyzing jasper reports located at %s\n", reportsDirectory);
        for (File report : FileUtils.listFiles(reportsDirectory, new String[] { "jrxml" }, true)) {
            System.out.printf("Compiling report template %s\n", report.getAbsolutePath());
            try {
                JasperCompileManager.compileReportToFile(report.getAbsolutePath());
            } catch (JRException e) {
                System.err.println("Error: cannot compile report because: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

}
