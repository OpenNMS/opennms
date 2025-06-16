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
package org.opennms.features.topology.plugins.topo.bsm.browsers;

import java.util.NoSuchElementException;

import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.Status;

import com.vaadin.v7.ui.Table;

public class BusinessServicesTableCellStyleGenerator implements Table.CellStyleGenerator {
    private static final long serialVersionUID = -9103202434825185405L;

    private BusinessServiceManager businessServiceManager;

    @Override
    public String getStyle(Table source, Object itemId, Object propertyId) {
        if (propertyId == null) {
            Long serviceId = (Long) itemId;
            try {
                BusinessService businessService = businessServiceManager.getBusinessServiceById(serviceId);
                Status status = businessService.getOperationalStatus();
                return String.format("alarm-%s", status.name().toLowerCase());
            } catch (NoSuchElementException nse) {
                return null;
            }
        }
        return null;
    }

    public void setBusinessServiceManager(BusinessServiceManager businessServiceManager) {
        this.businessServiceManager = businessServiceManager;
    }

}
