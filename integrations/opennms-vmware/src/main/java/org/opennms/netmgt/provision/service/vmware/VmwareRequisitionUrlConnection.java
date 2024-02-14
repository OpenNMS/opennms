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
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOExceptionWithCause;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.utils.url.GenericURLConnection;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.ForeignSourceRepository;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private final VmwareImportRequest importRequest;

    /**
     * Constructor for creating an instance of this class.
     *
     * @param url the URL to use
     * @throws MalformedURLException
     * @throws RemoteException
     */
    public VmwareRequisitionUrlConnection(URL url) throws MalformedURLException, RemoteException {
        super(url);

        logger.debug("Initializing URL Connection for host {}", url.getHost());

        Map<String, String> requestParameters = new HashMap<>();
        requestParameters.put("host", url.getHost());
        requestParameters.put("path", url.getPath());

        // Old or new user credentials handling scheme
        if (url.getUserInfo() != null && !url.getUserInfo().isEmpty()) {
            logger.warn("Old user credentials handling scheme detected. I'm gonna use it but you'd better adapt your URL to the new query parameter scheme 'vmware://<vcenter_server_fqdn>?username=<username>;password=<password>;....'");
            requestParameters.put("username", getUsername());
            requestParameters.put("password", getPassword());
        }
        requestParameters.putAll(getQueryArgs());

        importRequest = new VmwareImportRequest(requestParameters);
    }

    protected Requisition getExistingRequisition(String foreignSource) {
        Requisition curReq = null;
        try {
            ForeignSourceRepository repository = BeanUtils.getBean("daoContext", "deployedForeignSourceRepository", ForeignSourceRepository.class);
            if (repository != null) {
                curReq = repository.getRequisition(foreignSource);
            }
        } catch (Exception e) {
            logger.warn("Can't retrieve requisition {}", foreignSource, e);
        }
        return curReq;
    }

    @Override
    public void connect() {
        // pass
    }

    /**
     * {@inheritDoc}
     * <p/>
     * Creates a ByteArrayInputStream implementation of InputStream of the XML
     * marshaled version of the Requisition class. Calling close on this stream
     * is safe.
     */
    @Override
    public InputStream getInputStream() throws IOException {

        InputStream stream = null;
        try {
            final Requisition existingRequisition = getExistingRequisition(importRequest.getForeignSource());
            importRequest.setExistingRequisition(existingRequisition);
            final VmwareImporter importer = new VmwareImporter(importRequest);
            stream = new ByteArrayInputStream(jaxBMarshal(importer.getRequisition()).getBytes());
        } catch (Throwable e) {
            logger.warn("Problem getting input stream: '{}'", e);
            throw new IOExceptionWithCause("Problem getting input stream: " + e, e);
        }

        return stream;
    }

    /**
     * Utility to marshal the Requisition class into XML.
     *
     * @param r the requisition object
     * @return a String of XML encoding the Requisition class
     * @throws javax.xml.bind.JAXBException
     */
    private String jaxBMarshal(Requisition r) throws JAXBException {
        return JaxbUtils.marshal(r);
    }

    public VmwareImportRequest getImportRequest() {
        return importRequest;
    }
}
