/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.service.operations;

import org.opennms.netmgt.provision.service.ProvisionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NullUpdateOperation extends UpdateOperation {

    private static final Logger LOG = LoggerFactory.getLogger(NullUpdateOperation.class);

    public NullUpdateOperation(final Integer nodeId, final String foreignSource, final String foreignId, final String nodeLabel, final String location, final String building, final String city, final ProvisionService provisionService, final String rescanExisting) {
        super(nodeId, foreignSource, foreignId, nodeLabel, location, building, city, provisionService, rescanExisting);
    }

    @Override
    protected void doPersist() {
        LOG.debug("Skipping persist for node {}: rescanExisting is false", getNode());
    }
}