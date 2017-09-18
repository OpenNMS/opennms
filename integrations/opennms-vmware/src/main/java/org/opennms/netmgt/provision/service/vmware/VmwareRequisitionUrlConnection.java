/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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
