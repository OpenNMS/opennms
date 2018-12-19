/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.bsm.service.internal.edge;

import java.util.Set;

import org.opennms.netmgt.bsm.persistence.api.ApplicationEdgeEntity;
import org.opennms.netmgt.bsm.service.BusinessServiceManager;
import org.opennms.netmgt.bsm.service.internal.ApplicationImpl;
import org.opennms.netmgt.bsm.service.model.Application;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;

import com.google.common.base.MoreObjects;

public class ApplicationEdgeImpl extends AbstractEdge<ApplicationEdgeEntity> implements ApplicationEdge {

    public ApplicationEdgeImpl(BusinessServiceManager manager, ApplicationEdgeEntity entity) {
        super(manager, entity);
    }

    @Override
    public Application getApplication() {
        return new ApplicationImpl(getManager(), getEntity().getApplication());
    }

    @Override
    public void setApplication(Application application) {
        getEntity().setApplication(((ApplicationImpl)application).getEntity());
    }

    @Override
    public Set<String> getReductionKeys() {
        return getEntity().getReductionKeys();
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .toString();
    }

    @Override
    public String getFriendlyName() {
        return getEntity().getApplication().getName();
    }

    @Override
    public <T> T accept(EdgeVisitor<T> visitor) {
        return visitor.visit(this);
    }
}