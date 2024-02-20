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
package org.opennms.netmgt.provision.service.vmware;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;

import org.opennms.core.mate.api.Interpolator;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.mate.api.SecureCredentialsVaultScope;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.url.GenericURLConnection;
import org.opennms.core.xml.XmlHandler;
import org.opennms.features.scv.api.SecureCredentialsVault;
import org.opennms.netmgt.config.vmware.VmwareServer;
import org.opennms.netmgt.dao.vmware.VmwareConfigDao;
import org.opennms.netmgt.provision.persist.LocationAwareRequisitionClient;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;

/**
 * The Class VmwareRequisitionUrlConnection
 * 
 * <p>This class is used for the automatic requisition of Vmware related entities.</p>
 *
 * @deprecated Use the {@link VmwareRequisitionProvider} instead.
 * @author Christian Pape <Christian.Pape@informatik.hs-fulda.de>
 * @author Alejandro Galue <agalue@opennms.org>
 */
public class VmwareRequisitionUrlConnection extends GenericURLConnection {
    /**
     * the logger
     */
    private static final Logger logger = LoggerFactory.getLogger(VmwareRequisitionUrlConnection.class);
    private static LocationAwareRequisitionClient s_requisitionProviderClient;
    private static SecureCredentialsVault s_secureCredentialsVault;
    private static VmwareConfigDao s_vmwareConfigDao;
    private final Map<String, String> parameters = new TreeMap<>();
    private static final XmlHandler<Requisition> s_xmlHandler = new XmlHandler<>(Requisition.class);

    /**
     * Constructor for creating an instance of this class.
     *
     * @param url the URL to use
     */
    public VmwareRequisitionUrlConnection(final URL url) {
        super(url);

        logger.debug("Initializing URL Connection for host {}", url.getHost());

        parameters.put("host", url.getHost());
        parameters.put("path", url.getPath());

        // Old or new user credentials handling scheme
        if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
            logger.warn("Old user credentials handling scheme detected. I'm gonna use it but you'd better adapt your URL to the new query parameter scheme 'vmware://<vcenter_server_fqdn>?username=<username>;password=<password>;....'");
            parameters.put("username", getUsername());
            parameters.put("password", getPassword());
        }

        parameters.putAll(getQueryArgs());

        if (getVmwareConfigDao() == null) {
            logger.error("vmwareConfigDao should be a non-null value.");
        } else {
            Map<String, VmwareServer> serverMap = getVmwareConfigDao().getServerMap();
            if (serverMap == null) {
                logger.error("Error getting vmware-config.xml's server map.");
            } else {
                VmwareServer vmwareServer = serverMap.get(parameters.get("host"));

                if (vmwareServer == null) {
                    logger.error("Error getting credentials for VMware management server '{}'.", parameters.get("host"));
                } else {
                    final Scope scvScope = new SecureCredentialsVaultScope(getSecureCredentialsVault());
                    parameters.put("username", Interpolator.interpolate(vmwareServer.getUsername(), scvScope).output);
                    parameters.put("password", Interpolator.interpolate(vmwareServer.getPassword(), scvScope).output);
                }
            }
        }

        if (Strings.isNullOrEmpty(parameters.get("username")) || Strings.isNullOrEmpty(parameters.get("password"))) {
            logger.error("Error getting username/password for VMware management server '{}'.", parameters.get("host"));
            throw new IllegalArgumentException("Username and password must not be empty or null");
        }
    }

    @Override
    public void connect() {
        // pass
    }

    @Override
    public InputStream getInputStream() throws IOException {
        try {
            final Requisition requisition = getClient().requisition()
                    .withRequisitionProviderType(VmwareRequisitionProvider.TYPE_NAME)
                    .withParameters(parameters)
                    .execute()
                    .get();

            if (requisition == null) {
                throw new IOException(String.format("Invalid (null) requisition was returned by the provider for type '%s'", VmwareRequisitionProvider.TYPE_NAME));
            }

            // The XmlHandler is not thread safe
            // Marshaling is quick, so we opt to use a single instance of the handler
            // instead of using thread-local variables
            final String requisitionXml;
            synchronized(s_xmlHandler) {
                requisitionXml = s_xmlHandler.marshal(requisition);
            }

            return new ByteArrayInputStream(requisitionXml.getBytes());
        } catch (ExecutionException | InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static LocationAwareRequisitionClient getClient() {
        if (s_requisitionProviderClient == null) {
            s_requisitionProviderClient = BeanUtils.getBean("daoContext", "locationAwareRequisitionClient", LocationAwareRequisitionClient.class);
        }
        return s_requisitionProviderClient;
    }

    private static VmwareConfigDao getVmwareConfigDao() {
        if (s_vmwareConfigDao == null) {
            s_vmwareConfigDao = BeanUtils.getBean("daoContext", "vmwareConfigDao", VmwareConfigDao.class);
        }
        return s_vmwareConfigDao;
    }

    private static SecureCredentialsVault getSecureCredentialsVault() {
        if (s_secureCredentialsVault == null) {
            s_secureCredentialsVault = BeanUtils.getBean("jceksScvContext", "jceksSecureCredentialsVault", SecureCredentialsVault.class);
        }
        return s_secureCredentialsVault;
    }

    public static void setRequisitionProviderClient(LocationAwareRequisitionClient s_requisitionProviderClient) {
        VmwareRequisitionUrlConnection.s_requisitionProviderClient = s_requisitionProviderClient;
    }

    public static void setSecureCredentialsVault(SecureCredentialsVault s_secureCredentialsVault) {
        VmwareRequisitionUrlConnection.s_secureCredentialsVault = s_secureCredentialsVault;
    }

    public static void setVmwareConfigDao(VmwareConfigDao s_vmwareConfigDao) {
        VmwareRequisitionUrlConnection.s_vmwareConfigDao = s_vmwareConfigDao;
    }

    public VmwareImportRequest getImportRequest() {
        return new VmwareImportRequest(parameters);
    }
}
