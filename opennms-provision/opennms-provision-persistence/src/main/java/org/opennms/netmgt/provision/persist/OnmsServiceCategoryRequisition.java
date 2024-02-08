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
package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.RequisitionCategory;

/**
 * OnmsCategoryRequisition
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsServiceCategoryRequisition {

    private RequisitionCategory m_category;

    /**
     * <p>Constructor for OnmsServiceCategoryRequisition.</p>
     *
     * @param category a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionCategory} object.
     */
    public OnmsServiceCategoryRequisition(RequisitionCategory category) {
        m_category = category;
    }

    /**
     * @return the category
     */
    RequisitionCategory getCategory() {
        return m_category;
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitServiceCategory(this);
        visitor.completeServiceCategory(this);
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_category.getName();
    }
    
    

}
