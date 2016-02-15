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

import org.opennms.netmgt.bsm.persistence.api.functions.reduce.AbstractReductionFunctionEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.HighestSeverityAboveEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.MostCriticalEntity;
import org.opennms.netmgt.bsm.persistence.api.functions.reduce.ThresholdEntity;
import org.opennms.netmgt.bsm.service.model.Status;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverityAbove;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.bsm.service.model.functions.reduce.Threshold;
import org.opennms.netmgt.bsm.service.model.functions.reduce.ReductionFunction;

import com.google.common.base.Function;
import com.google.common.collect.Maps;

public class ReduceFunctionMapper {

    private static final Map<Class<? extends ReductionFunction>, Function<ReductionFunction, AbstractReductionFunctionEntity>> serviceToPersistenceMapping = Maps.newHashMap();

    private static final Map<Class<? extends AbstractReductionFunctionEntity>, Function<AbstractReductionFunctionEntity, ReductionFunction>> persistenceToServiceMapping = Maps.newHashMap();

    static {
        serviceToPersistenceMapping.put(MostCritical.class, input -> new MostCriticalEntity());
        serviceToPersistenceMapping.put(Threshold.class, input -> {
            ThresholdEntity entity = new ThresholdEntity();
            entity.setThreshold(((Threshold) input).getThreshold());
            return entity;
        });
        serviceToPersistenceMapping.put(HighestSeverityAbove.class, input -> {
            HighestSeverityAboveEntity entity = new HighestSeverityAboveEntity();
            entity.setThreshold(((HighestSeverityAbove) input).getThreshold().ordinal());
            return entity;
        });

        persistenceToServiceMapping.put(MostCriticalEntity.class, input -> new MostCritical());
        persistenceToServiceMapping.put(ThresholdEntity.class, input -> {
            Threshold result = new Threshold();
            result.setThreshold(((ThresholdEntity) input).getThreshold());
            return result;
        });
        persistenceToServiceMapping.put(HighestSeverityAboveEntity.class, input -> {
            HighestSeverityAbove result = new HighestSeverityAbove();
            result.setThreshold(Status.get(((HighestSeverityAboveEntity) input).getThreshold()));
            return result;
        });
    }

    public ReductionFunction toServiceFunction(AbstractReductionFunctionEntity reductionFunctionEntity) {
        if (reductionFunctionEntity == null) {
            return null;
        }
        Function<AbstractReductionFunctionEntity, ReductionFunction> mapping = persistenceToServiceMapping.get(reductionFunctionEntity.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapping.apply(reductionFunctionEntity);
    }

    public AbstractReductionFunctionEntity toPersistenceFunction(ReductionFunction reductionFunction) {
        Function<ReductionFunction, AbstractReductionFunctionEntity> mapping = serviceToPersistenceMapping.get(reductionFunction.getClass());
        if (mapping == null) {
            throw new IllegalArgumentException("No mapping found");
        }
        return mapping.apply(reductionFunction);
    }
}
