/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

import javax.xml.bind.JAXB;

import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.requisition.RequisitionMerger;
import org.opennms.netmgt.provision.persist.requisition.Requisition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;

import com.google.common.base.MoreObjects;

public class ResourceRequisitionProvider implements RequisitionProvider {

    private static Logger LOG = LoggerFactory.getLogger(ResourceRequisitionProvider.class);

    private final Resource resource;

    private final RequisitionMerger requisitionMerger;

    public ResourceRequisitionProvider(String urlString, RequisitionMerger requisitionMerger) throws MalformedURLException, URISyntaxException {
        URL url = new URL(urlString);
        if ("file".equals(url.getProtocol())) {
            final File file = new File(url.toURI());
            LOG.debug("doImport: file = {}", file);
            if (file.exists()) {
                resource = new FileSystemResource(file);
            } else {
                final String filename = file.getName();
                if (filename.contains("%20")) {
                    resource = new FileSystemResource(new File(file.getParentFile(), filename.replace("%20", " ")));
                } else {
                    resource = new UrlResource(url);
                }
            }
        } else {
            resource = new UrlResource(url);
        }
        this.requisitionMerger = Objects.requireNonNull(requisitionMerger);
    }

    public ResourceRequisitionProvider(Resource resource, RequisitionMerger requisitionMerger) {
        this.resource = Objects.requireNonNull(resource);
        this.requisitionMerger = Objects.requireNonNull(requisitionMerger);
    }

    @Override
    public RequisitionEntity getRequisition() throws IOException {
        Requisition requisition = JAXB.unmarshal(this.resource.getInputStream(), Requisition.class);
        return requisitionMerger.mergeOrCreate(requisition);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("resource", resource)
                .toString();
    }
}
