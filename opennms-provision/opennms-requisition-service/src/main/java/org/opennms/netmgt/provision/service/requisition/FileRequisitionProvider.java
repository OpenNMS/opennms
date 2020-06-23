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

package org.opennms.netmgt.provision.service.requisition;

import java.io.File;
import java.util.Map;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.provision.persist.AbstractRequisitionProvider;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class FileRequisitionProvider extends AbstractRequisitionProvider<FileRequisitionRequest> {

    public static final String TYPE_NAME = "file";

    public FileRequisitionProvider() {
        super(FileRequisitionRequest.class);
    }

    @Override
    public String getType() {
        return TYPE_NAME;
    }

    @Override
    public FileRequisitionRequest getRequest(Map<String, String> parameters) {
        final FileRequisitionRequest request = new FileRequisitionRequest();
        request.setPath(parameters.get("path"));
        if (request.getPath() == null || request.getPath().isEmpty()) {
            throw new IllegalArgumentException("Path arguments is required.");
        }
        return request;
    }

    @Override
    public Requisition getRequisitionFor(FileRequisitionRequest request) {
        final File file = new File(request.getPath());
        return JaxbUtils.unmarshal(Requisition.class, file);
    }

}
