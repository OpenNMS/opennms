/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.bsm.service.internal.edge;

import java.util.Collections;
import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.BusinessServiceChildEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.BusinessServiceImpl;
import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;

public class ChildEdgeImpl extends AbstractEdge<BusinessServiceChildEdgeEntity> implements ChildEdge {

    public ChildEdgeImpl(BusinessServiceManager manager, BusinessServiceChildEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public BusinessService getChild() {
        return new BusinessServiceImpl(getManager(), getEntity().getChild());
    }

    @Override
    public Set<String> getReductionKeys() {
        return Collections.unmodifiableSet(getEntity().getReductionKeys());
    }

    @Override
    public void setChild(BusinessService child) {
        getEntity().setChild(((BusinessServiceImpl) child).getEntity());
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("parent", super.toString())
                .add("child", getChild() == null ? null : getChild().getId())
                .toString();
    }

    /**
     * Method implementation for the friendly name used in the topology UI. Since this value is not
     * used for child Business Services this method always returns <code>null</code>.
     *
     * @return always null
     * @see AbstractEdge#getFriendlyName()
     */
    @Override
    public String getFriendlyName() {
        return null;
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
