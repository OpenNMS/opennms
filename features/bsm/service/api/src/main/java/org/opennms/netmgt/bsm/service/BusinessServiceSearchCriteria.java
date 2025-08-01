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
package org.opennms.netmgt.bsm.service;

import java.util.List;

import org.opennms.netmgt.bsm.service.model.BusinessService;

/**
 * Criteria for searching for business services
 *
 * @author Christian Pape <christian@opennms.org>
 */
public interface BusinessServiceSearchCriteria {
    /**
     * This will apply the criteria represented by an instance of this interface to a list of business services and will
     * return a subset of these business services.
     * @param businessServiceManager the business service manager (required to gather the operational status)
     * @param businessServices the list of business services
     * @return a subset of business services
     */
    List<BusinessService> apply(BusinessServiceManager businessServiceManager, List<BusinessService> businessServices);
}
