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
