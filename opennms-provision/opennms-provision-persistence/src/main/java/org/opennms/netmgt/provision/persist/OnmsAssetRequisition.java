/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.persist;

import org.opennms.netmgt.provision.persist.requisition.RequisitionAsset;

/**
 * OnmsAssetRequisition
 *
 * @author brozow
 * @version $Id: $
 */
public class OnmsAssetRequisition {

    private RequisitionAsset m_asset;
    
    /**
     * <p>Constructor for OnmsAssetRequisition.</p>
     *
     * @param asset a {@link org.opennms.netmgt.provision.persist.requisition.RequisitionAsset} object.
     */
    public OnmsAssetRequisition(RequisitionAsset asset) {
        m_asset = asset;
    }
    
    RequisitionAsset getAsset() {
        return m_asset;
    }

    /**
     * <p>visit</p>
     *
     * @param visitor a {@link org.opennms.netmgt.provision.persist.RequisitionVisitor} object.
     */
    public void visit(RequisitionVisitor visitor) {
        visitor.visitAsset(this);
        visitor.completeAsset(this);
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_asset.getName();
    }

    /**
     * <p>getValue</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getValue() {
        return m_asset.getValue();
    }
    
    

}
