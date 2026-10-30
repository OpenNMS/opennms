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
package org.opennms.web.rest.v2;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.Date;
import java.util.function.Supplier;

import javax.servlet.ServletContext;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.ipc.sink.common.SinkStrategy;
import org.opennms.core.rpc.common.RpcStrategy;
import org.opennms.core.resource.Vault;
import org.opennms.core.utils.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Backs the About page: product version plus the runtime environment details the
 * legacy about/index.jsp rendered server-side.
 */
@Component
@Path("about")
@Produces(MediaType.APPLICATION_JSON)
public class AboutRestService {

    private static final Logger LOG = LoggerFactory.getLogger(AboutRestService.class);

    private static final String UNKNOWN = "Unknown";

    private Supplier<DataSource> m_dataSource = DataSourceFactory::getInstance;

    void setDataSource(final Supplier<DataSource> dataSource) {
        m_dataSource = dataSource;
    }

    @GET
    public AboutDTO getAbout(@Context final ServletContext servletContext) {
        final AboutDTO about = new AboutDTO();
        about.displayVersion = Vault.getProperty("version.display");
        about.product = Vault.getProperty("opennms.product");
        about.docsVersion = Vault.getProperty("opennms.docs.version");
        // formatted server-side so it shows the server's own time zone
        about.serverTime = new Date().toString();

        about.javaVersion = describe("java.version", "java.vendor");
        about.javaRuntime = describe("java.runtime.name", "java.runtime.version");
        about.javaSpecification = describe("java.specification.name", "java.specification.vendor", "java.specification.version");
        about.javaVirtualMachine = describe("java.vm.name", "java.vm.vendor", "java.vm.version");
        about.javaVirtualMachineSpecification = describe("java.vm.specification.name", "java.vm.specification.vendor", "java.vm.specification.version");
        about.operatingSystem = System.getProperty("os.name") + " " + System.getProperty("os.version") + " (" + System.getProperty("os.arch") + ")";
        about.osgiContainer = "Apache Karaf " + System.getProperty("karaf.version");
        if (servletContext != null) {
            about.servletContainer = servletContext.getServerInfo()
                    + " (Servlet Spec " + servletContext.getMajorVersion() + "." + servletContext.getMinorVersion() + ")";
        }

        about.databaseType = UNKNOWN;
        about.databaseVersion = UNKNOWN;
        try (Connection conn = m_dataSource.get().getConnection()) {
            final DatabaseMetaData meta = conn.getMetaData();
            about.databaseType = meta.getDatabaseProductName();
            about.databaseVersion = meta.getDatabaseProductVersion();
        } catch (final SQLException | RuntimeException e) {
            LOG.warn("Unable to read the database product details.", e);
        }

        about.timeSeriesStrategy = TimeSeries.getTimeseriesStrategy().getDescr();
        about.ipcStrategy = "Sink: " + SinkStrategy.getSinkStrategy().getName() + "; RPC: " + RpcStrategy.getRpcStrategy().getName();
        return about;
    }

    // "name (detail, detail)" from system properties, matching the legacy page's layout
    private static String describe(final String nameProperty, final String... detailProperties) {
        final StringBuilder sb = new StringBuilder(String.valueOf(System.getProperty(nameProperty)));
        if (detailProperties.length > 0) {
            sb.append(" (");
            for (int i = 0; i < detailProperties.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append(System.getProperty(detailProperties[i]));
            }
            sb.append(')');
        }
        return sb.toString();
    }

    @XmlRootElement(name = "about")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class AboutDTO {
        @XmlElement private String displayVersion;
        @XmlElement private String product;
        @XmlElement private String docsVersion;
        @XmlElement private String serverTime;
        @XmlElement private String javaVersion;
        @XmlElement private String javaRuntime;
        @XmlElement private String javaSpecification;
        @XmlElement private String javaVirtualMachine;
        @XmlElement private String javaVirtualMachineSpecification;
        @XmlElement private String operatingSystem;
        @XmlElement private String osgiContainer;
        @XmlElement private String servletContainer;
        @XmlElement private String databaseType;
        @XmlElement private String databaseVersion;
        @XmlElement private String timeSeriesStrategy;
        @XmlElement private String ipcStrategy;

        public String getDisplayVersion() { return displayVersion; }
        public String getProduct() { return product; }
        public String getDocsVersion() { return docsVersion; }
        public String getServerTime() { return serverTime; }
        public String getJavaVersion() { return javaVersion; }
        public String getJavaRuntime() { return javaRuntime; }
        public String getJavaSpecification() { return javaSpecification; }
        public String getJavaVirtualMachine() { return javaVirtualMachine; }
        public String getJavaVirtualMachineSpecification() { return javaVirtualMachineSpecification; }
        public String getOperatingSystem() { return operatingSystem; }
        public String getOsgiContainer() { return osgiContainer; }
        public String getServletContainer() { return servletContainer; }
        public String getDatabaseType() { return databaseType; }
        public String getDatabaseVersion() { return databaseVersion; }
        public String getTimeSeriesStrategy() { return timeSeriesStrategy; }
        public String getIpcStrategy() { return ipcStrategy; }
    }
}
