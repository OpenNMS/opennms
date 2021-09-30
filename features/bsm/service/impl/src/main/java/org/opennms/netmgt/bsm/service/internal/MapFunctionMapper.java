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

package org.opennms.netmgt.bsm.service.internal;

import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.DecreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IncreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.MapFunctionEntityVisitor;
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunction;
import org.opennms.netmgt.bsm.service.model.functions.map.MapFunctionVisitor;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;

public class MapFunctionMapper {

    private static final MapFunctionVisitor<AbstractMapFunctionEntity> serviceToPersistenceMapping = new MapFunctionVisitor<AbstractMapFunctionEntity>() {

        @Override
        public AbstractMapFunctionEntity visit(Decrease decrease) {
            return new DecreaseEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Identity identity) {
            return new IdentityEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Ignore ignore) {
            return new IgnoreEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(Increase increase) {
            return new IncreaseEntity();
        }

        @Override
        public AbstractMapFunctionEntity visit(SetTo setTo) {
            SetToEntity entity = new SetToEntity();
            entity.setSeverity(SeverityMapper.toSeverity(setTo.getStatus()));
            return entity;
        }
    };

    private static final MapFunctionEntityVisitor<MapFunction> persistenceToServiceMapping = new MapFunctionEntityVisitor<MapFunction>() {
        @Override
        public MapFunction visit(DecreaseEntity decreaseEntity) {
            return new Decrease();
        }

        @Override
        public MapFunction visit(IdentityEntity identityEntity) {
            return new Identity();
        }

        @Override
        public MapFunction visit(IgnoreEntity ignoreEntity) {
            return new Ignore();
        }

        @Override
        public MapFunction visit(IncreaseEntity increaseEntity) {
            return new Increase();
        }

        @Override
        public MapFunction visit(SetToEntity setToEntity) {
            SetTo result = new SetTo();
            result.setStatus(SeverityMapper.toStatus(setToEntity.getSeverity()));
            return result;
        }
    };

    public AbstractMapFunctionEntity toPersistenceFunction(MapFunction mapFunction) {
        AbstractMapFunctionEntity mapFunctionEntity = mapFunction.accept(serviceToPersistenceMapping);
        if (mapFunctionEntity == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapFunctionEntity;
    }

    public MapFunction toServiceFunction(AbstractMapFunctionEntity mapFunctionEntity) {
        if (mapFunctionEntity == null) {
            return null;
        }
        MapFunction mapFunction = mapFunctionEntity.accept(persistenceToServiceMapping);
        if (mapFunction == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapFunction;
    }
}
