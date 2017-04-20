/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

import java.util.Objects;

import org.opennms.netmgt.model.requisition.RequisitionEntity;
import org.opennms.netmgt.provision.persist.requisition.ImportRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequisitionImportContext {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImportContext.class);
    private Throwable m_throwable;
    private ImportRequest importRequest;
    private RequisitionEntity requisition;

    public void setImportRequest(ImportRequest importRequest) {
        this.importRequest = importRequest;
    }

    public ImportRequest getImportRequest() {
        return importRequest;
    }

    public Throwable getError() {
        return m_throwable;
    }

    public void abort(final Throwable t) {
        if (m_throwable == null) {
            m_throwable = t;
        } else {
            LOG.warn("Requisition {} has already been aborted, but we received another abort message.  Ignoring.", requisition.getForeignSource(), t);
        }
    }

    public boolean isAborted() {
        if (m_throwable != null) return true;
        return false;
    }

    public boolean isRescanExisting() {
        return importRequest.getRescanExisting() == null || Boolean.valueOf(importRequest.getRescanExisting()) || "dbonly".equals(importRequest.getRescanExisting());
    }

    public void setRequisition(RequisitionEntity requisition) {
        this.requisition = Objects.requireNonNull(requisition);

        // enforce hibernate to load entity graph
        requisition.getNodes().forEach(n -> {
            n.getInterfaces().forEach(i -> {
                i.getCategories();
                i.getMonitoredServices().forEach(m -> m.getCategories());
            });
            n.getAssets().entrySet();
            n.getCategories().iterator();
        });

    }

    public RequisitionEntity getRequisition() {
        return requisition;
    }
}
