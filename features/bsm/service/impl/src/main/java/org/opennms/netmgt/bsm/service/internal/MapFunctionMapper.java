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

import java.util.Map;

import org.opennms.netmgt.bsm.persistence.api.functions.map.AbstractMapFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.DecreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IdentityEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IgnoreEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.IncreaseEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.map.SetToEntity;
import org.opennms.netmgt.bsm.service.model.functions.map.Decrease;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.map.Ignore;
import org.opennms.netmgt.bsm.service.model.functions.map.Increase;
import org.opennms.netmgt.bsm.service.model.functions.map.SetTo;
import org.opennms.netmgt.bsm.service.model.mapreduce.MapFunction;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class MapFunctionMapper {

    private static final Map<Class<? extends MapFunction>, Function<MapFunction, AbstractMapFunctionEntity>> serviceToPersistenceMapping = Maps.newHashMap();

    private static final Map<Class<? extends AbstractMapFunctionEntity>, Function<AbstractMapFunctionEntity, MapFunction>> persistenceToServiceMapping = Maps.newHashMap();

    static {
        serviceToPersistenceMapping.put(Decrease.class, input -> new DecreaseEntity());
        serviceToPersistenceMapping.put(Increase.class, input -> new IncreaseEntity());
        serviceToPersistenceMapping.put(Identity.class, input -> new IdentityEntity());
        serviceToPersistenceMapping.put(Ignore.class, input -> new IgnoreEntity());
        serviceToPersistenceMapping.put(SetTo.class, input -> {
            SetToEntity entity = new SetToEntity();
            entity.setSeverity(SeverityMapper.toSeverity(((SetTo) input).getStatus()));
            return entity;
        });

        persistenceToServiceMapping.put(DecreaseEntity.class, input -> new Decrease());
        persistenceToServiceMapping.put(IncreaseEntity.class, input -> new Increase());
        persistenceToServiceMapping.put(IdentityEntity.class, input -> new Identity());
        persistenceToServiceMapping.put(IgnoreEntity.class, input -> new Ignore());
        persistenceToServiceMapping.put(SetToEntity.class, input -> {
            SetTo result = new SetTo();
            result.setStatus(SeverityMapper.toStatus(((SetToEntity) input).getSeverity()));
            return result;
        });
    }

    public AbstractMapFunctionEntity toPersistenceFunction(MapFunction mapFunction) {
        Function<MapFunction, AbstractMapFunctionEntity> mapping = serviceToPersistenceMapping.get(mapFunction.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapping.apply(mapFunction);
    }

    public MapFunction toServiceFunction(AbstractMapFunctionEntity mapFunction) {
        if (mapFunction == null) {
            return null;
        }
        Function<AbstractMapFunctionEntity, MapFunction> mapping = persistenceToServiceMapping.get(mapFunction.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapping.apply(mapFunction);
    }
}
