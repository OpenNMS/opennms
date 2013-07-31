/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision.service.operations;

import javax.xml.bind.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.provision.persist.requisition.Requisition;

public class RequisitionImport {
    private static final Logger LOG = LoggerFactory.getLogger(RequisitionImport.class);
    private Requisition m_requisition;
    private Throwable m_throwable;

    public Requisition getRequisition() {
        return m_requisition;
    }

    public void setRequisition(final Requisition requisition) {
        m_requisition = requisition;
        try {
            requisition.validate();
        } catch (final ValidationException e) {
            if (m_throwable == null) {
                m_throwable = e;
            } else {
                LOG.debug("Requisition {} did not validate, but we'll ignore the exception because we've previously aborted with: {}", requisition, m_throwable, e);
            }
        }
    }

    public Throwable getError() {
        return m_throwable;
    }

    public void abort(final Throwable t) {
        if (m_throwable == null) {
            m_throwable = t;
        } else {
            LOG.warn("Requisition {} has already been aborted, but we received another abort message.  Ignoring.", m_requisition, t);
        }
    }

    public boolean isAborted() {
        if (m_throwable != null) return true;
        return false;
    }

}
