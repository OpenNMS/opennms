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

import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.SingleReductionKeyEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;

public class ReductionKeyEdgeImpl extends AbstractEdge<SingleReductionKeyEdgeEntity> implements ReductionKeyEdge {

    public ReductionKeyEdgeImpl(BusinessServiceManager manager, SingleReductionKeyEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public Set<String> getReductionKeys() {
        return getEntity().getReductionKeys();
    }

    @Override
    public String getReductionKey() {
        return getEntity().getReductionKey();
    }

    @Override
    public void setReductionKey(String reductionKey) {
        getEntity().setReductionKey(reductionKey);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("parent", super.toString())
                .add("reductionKey", getReductionKey())
                .toString();
    }

    @Override
    public void setFriendlyName(String friendlyName) {
        getEntity().setFriendlyName(friendlyName);
    }

    @Override
    public String getFriendlyName() {
        return getEntity().getFriendlyName();
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
