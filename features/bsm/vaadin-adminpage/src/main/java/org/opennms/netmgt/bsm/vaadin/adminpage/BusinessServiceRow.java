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
package org.opennms.netmgt.bsm.vaadin.adminpage;

import java.util.Objects;

import org.opennms.netmgt.bsm.service.model.BusinessService;

/**
 * Used to list {@link BusinessService}s in a {@link com.vaadin.ui.TreeTable}.
 *
 * This allows the rows to have alternate IDs, since the business services may have
 * multiple parents.
 *
 * @author jwhite
 */
public class BusinessServiceRow {

    private final long rowId;
    private final BusinessService businessService;
    private final Long parentBusinessServiceId;

    public BusinessServiceRow(long rowId, BusinessService businessService, Long parentBusinessServiceId) {
        this.rowId = rowId;
        this.businessService = Objects.requireNonNull(businessService);
        this.parentBusinessServiceId = parentBusinessServiceId;
    }

    public long getRowId() {
        return rowId;
    }

    public String getName() {
        return businessService.getName();
    }

    public BusinessService getBusinessService() {
        return businessService;   
    }

    public Long getParentBusinessServiceId() {
        return parentBusinessServiceId;
    }
}
